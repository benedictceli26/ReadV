package com.mnhs.readventures.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Firebase Auth (Email/Password) with a local sample-mode fallback.
 *
 * If google-services.json is present and Firebase initialises, real
 * Firebase Auth is used. If not (the usual case on first run), the app
 * runs in sample mode: any valid email + password of 6+ characters signs
 * in locally, and a "Continue as demo student" button signs in as the
 * sample student Juan Dela Cruz. Nothing here crashes when Firebase
 * is missing — that is deliberate, so the demo flow always works.
 */
class AuthRepository(private val progressStore: ProgressStore) {

    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    val isFirebaseAvailable: Boolean get() = firebaseAuth != null

    private fun demoUser(email: String): AppUser {
        val savedName = progressStore.savedDisplayName()
        val name = when {
            savedName.isNotBlank() -> savedName
            email.startsWith("juan", ignoreCase = true) -> "Juan Dela Cruz"
            else -> email.substringBefore("@")
                .replace(".", " ").replace("_", " ")
                .split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                .ifBlank { "Student" }
        }
        return AppUser(uid = "sample-${email.hashCode()}", name = name, email = email, role = "student")
    }

    fun currentFirebaseUser(): FirebaseUser? = try {
        firebaseAuth?.currentUser
    } catch (e: Exception) {
        null
    }

    /** Returns Result<AppUser>. Runs the callback on the main thread. */
    fun signIn(email: String, password: String, onResult: (Result<AppUser>) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            // Sample mode: any valid email + 6+ char password signs in locally.
            onResult(Result.success(demoUser(email)))
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val fb = result.user
                val user = AppUser(
                    uid = fb?.uid ?: "",
                    name = fb?.displayName ?: email.substringBefore("@"),
                    email = fb?.email ?: email,
                    role = "student"
                )
                onResult(Result.success(user))
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
            }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String = "student",
        onResult: (Result<AppUser>) -> Unit
    ) {
        val auth = firebaseAuth
        if (auth == null) {
            progressStore.saveDisplayName(name)
            onResult(Result.success(AppUser(uid = "sample-${email.hashCode()}", name = name, email = email, role = role)))
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val fb = result.user
                onResult(
                    Result.success(
                        AppUser(uid = fb?.uid ?: "", name = name, email = fb?.email ?: email, role = role)
                    )
                )
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun signInAsDemo(onResult: (AppUser) -> Unit) {
        val user = AppUser(
            uid = "sample-juan",
            name = progressStore.savedDisplayName().ifBlank { "Juan Dela Cruz" },
            email = "juan.delacruz@mnhs.edu.ph",
            role = "student"
        )
        onResult(user)
    }

    fun signInAsDemoTeacher(onResult: (AppUser) -> Unit) {
        val user = AppUser(
            uid = "teacher-santos",
            name = "Ms. Santos",
            email = "teacher.santos@mnhs.edu.ph",
            role = "teacher"
        )
        onResult(user)
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // Sample mode: nothing to sign out of in Firebase.
        }
        AppState.logOut()
    }

    /**
     * Change password. In Firebase mode this re-authenticates with the
     * current password and then updates it. In sample mode it checks the
     * locally stored sample password and updates it.
     * Returns Result<Unit> with a user-readable error on failure.
     */
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val auth = firebaseAuth
        val fbUser = auth?.currentUser
        if (auth == null || fbUser == null || fbUser.email == null) {
            if (currentPassword != progressStore.samplePassword()) {
                onResult(Result.failure(IllegalArgumentException("Current password is incorrect.")))
            } else {
                progressStore.setSamplePassword(newPassword)
                onResult(Result.success(Unit))
            }
            return
        }
        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(fbUser.email!!, currentPassword)
        fbUser.reauthenticate(credential)
            .addOnSuccessListener {
                fbUser.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    companion object {
        /** New-password strength rules, shown in the Change Password screen. */
        fun passwordStrength(password: String): Int {
            var score = 0
            if (password.length >= 8) score++
            if (password.any { it.isLetter() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++
            return score // 0..4
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
