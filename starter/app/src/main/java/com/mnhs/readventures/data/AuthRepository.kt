package com.mnhs.readventures.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class AuthRepository {

    private val firebaseAuth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    val isFirebaseAuthAvailable: Boolean get() = firebaseAuth != null

    fun currentFirebaseUser(): FirebaseUser? = firebaseAuth?.currentUser

    fun signIn(email: String, password: String, onResult: (Result<AppUser>) -> Unit) {
        val auth = firebaseAuth
            ?: return onResult(Result.failure(firebaseUnavailable("Authentication")))

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                resolveTrustedProfile(result.user, email, onResult)
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(authenticationError(error)))
            }
    }

    fun register(name: String, email: String, password: String, onResult: (Result<AppUser>) -> Unit) {
        val auth = firebaseAuth
            ?: return onResult(Result.failure(firebaseUnavailable("Authentication")))
        val db = firestore
            ?: return onResult(Result.failure(firebaseUnavailable("Firestore")))

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    onResult(Result.failure(IllegalStateException("Firebase did not return the new account.")))
                    return@addOnSuccessListener
                }

                val profile = mapOf(
                    "name" to name,
                    "email" to (firebaseUser.email ?: email),
                    "role" to "student"
                )
                db.collection(FirebaseCollections.USERS).document(firebaseUser.uid)
                    .set(profile)
                    .addOnSuccessListener {
                        onResult(
                            Result.success(
                                AppUser(
                                    uid = firebaseUser.uid,
                                    name = name,
                                    email = firebaseUser.email ?: email,
                                    role = "student"
                                )
                            )
                        )
                    }
                    .addOnFailureListener { error ->
                        auth.signOut()
                        onResult(
                            Result.failure(
                                IllegalStateException(
                                    "Your Firebase account was created, but its student profile could not be provisioned. " +
                                        "Contact your administrator before trying to sign in.",
                                    error
                                )
                            )
                        )
                    }
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(authenticationError(error)))
            }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Result<Unit>) -> Unit) {
        val auth = firebaseAuth
            ?: return onResult(Result.failure(firebaseUnavailable("Authentication")))
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { error ->
                val message = if (error is FirebaseNetworkException) {
                    "Could not reach Firebase Authentication. Check your internet connection and try again."
                } else {
                    error.localizedMessage ?: "Firebase could not send a password reset email."
                }
                onResult(Result.failure(IllegalStateException(message, error)))
            }
    }

    fun updateProfile(name: String, grade: String, onResult: (Result<Unit>) -> Unit) {
        val authUser = firebaseAuth?.currentUser
            ?: return onResult(Result.failure(IllegalStateException("Sign in with Firebase to update your profile.")))
        val db = firestore
            ?: return onResult(Result.failure(firebaseUnavailable("Firestore")))

        db.collection(FirebaseCollections.USERS).document(authUser.uid)
            .update(mapOf("name" to name, "grade" to grade))
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { error ->
                onResult(Result.failure(firestoreError(error)))
            }
    }

    fun signOut(): Result<Unit> {
        val auth = firebaseAuth
            ?: return Result.failure(firebaseUnavailable("Authentication"))
        return try {
            auth.signOut()
            AppState.logOut()
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(IllegalStateException("Could not sign out of Firebase. Please try again.", error))
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Result<Unit>) -> Unit) {
        val auth = firebaseAuth
            ?: return onResult(Result.failure(firebaseUnavailable("Authentication")))
        val firebaseUser = auth.currentUser
            ?: return onResult(Result.failure(IllegalStateException("Sign in with Firebase before changing your password.")))
        val email = firebaseUser.email
            ?: return onResult(Result.failure(IllegalStateException("The signed-in Firebase account has no email address.")))

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        firebaseUser.reauthenticate(credential)
            .addOnSuccessListener {
                firebaseUser.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { onResult(Result.failure(authenticationError(it))) }
            }
            .addOnFailureListener { onResult(Result.failure(authenticationError(it))) }
    }

    private fun resolveTrustedProfile(
        firebaseUser: FirebaseUser?,
        fallbackEmail: String,
        onResult: (Result<AppUser>) -> Unit
    ) {
        val user = firebaseUser
            ?: return onResult(Result.failure(IllegalStateException("Firebase Authentication returned no signed-in user.")))
        val db = firestore
        if (db == null) {
            firebaseAuth?.signOut()
            return onResult(Result.failure(firebaseUnavailable("Firestore")))
        }

        db.collection(FirebaseCollections.USERS).document(user.uid).get(Source.SERVER)
            .addOnSuccessListener { profile ->
                if (!profile.exists()) {
                    firebaseAuth?.signOut()
                    onResult(
                        Result.failure(
                            IllegalStateException(
                                "Your Firebase account is authenticated, but its user profile has not been provisioned. " +
                                    "Contact your administrator."
                            )
                        )
                    )
                    return@addOnSuccessListener
                }

                val role = profile.getString("role")
                if (role != "student" && role != "teacher") {
                    firebaseAuth?.signOut()
                    onResult(
                        Result.failure(
                            IllegalStateException(
                                "Your account profile has a missing or invalid role. Contact your administrator."
                            )
                        )
                    )
                    return@addOnSuccessListener
                }

                onResult(
                    Result.success(
                        AppUser(
                            uid = user.uid,
                            name = profile.getString("name")
                                ?: user.displayName
                                ?: fallbackEmail.substringBefore("@"),
                            email = user.email ?: fallbackEmail,
                            role = role,
                            grade = profile.getString("grade").orEmpty()
                        )
                    )
                )
            }
            .addOnFailureListener { error ->
                firebaseAuth?.signOut()
                onResult(Result.failure(firestoreError(error)))
            }
    }

    private fun authenticationError(error: Exception): Exception = when (error) {
        is FirebaseNetworkException -> IllegalStateException(
            "Could not reach Firebase Authentication. Check your internet connection and try again.",
            error
        )
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
            IllegalArgumentException("The email or password is incorrect.", error)
        else -> IllegalStateException(error.localizedMessage ?: "Firebase Authentication failed. Please try again.", error)
    }

    private fun firestoreError(error: Exception): Exception =
        if (error is FirebaseNetworkException ||
            (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.UNAVAILABLE)
        ) {
            IllegalStateException("Could not reach Firestore. Check your internet connection and try again.", error)
        } else {
            IllegalStateException(error.localizedMessage ?: "Could not access your Firestore profile.", error)
        }

    private fun firebaseUnavailable(service: String) =
        IllegalStateException("Firebase $service is unavailable. Check the Firebase app configuration and try again.")

    companion object {
        fun passwordStrength(password: String): Int {
            var score = 0
            if (password.length >= 8) score++
            if (password.any { it.isLetter() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++
            return score
        }

        fun passwordIssues(password: String): List<String> {
            val issues = mutableListOf<String>()
            if (password.length < 8) issues.add("At least 8 characters")
            if (!password.any { it.isLetter() }) issues.add("At least one letter")
            if (!password.any { it.isDigit() }) issues.add("At least one number")
            return issues
        }
    }
}
