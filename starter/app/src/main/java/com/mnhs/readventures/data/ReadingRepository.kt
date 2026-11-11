package com.mnhs.readventures.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException

/**
 * Reading materials + quiz questions.
 *
 * Bundled reading content remains available offline while Firebase-backed
 * materials and quiz scores are loaded from Firestore. Firebase quiz scores
 * are written only by the grading function.
 */
class ReadingRepository {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    val isFirestoreAvailable: Boolean get() = firestore != null
    private val currentFirebaseUid: String?
        get() = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }

    fun loadMaterials(onResult: (Result<List<ReadingMaterial>>) -> Unit) {
        val db = firestore
            ?: return onResult(Result.failure(IllegalStateException("Firestore is unavailable.")))
        if (currentFirebaseUid.isNullOrBlank()) {
            return onResult(Result.failure(IllegalStateException("Sign in with Firebase to load reading materials.")))
        }
        db.collection(FirebaseCollections.READING_MATERIALS).get()
            .addOnSuccessListener { snap: QuerySnapshot ->
                val docs = snap.documents.mapNotNull { doc ->
                    runCatching {
                        ReadingMaterial(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            level = doc.getString("level") ?: "",
                            topic = doc.getString("topic") ?: "",
                            category = doc.getString("category") ?: "",
                            kind = doc.getString("kind") ?: "Stories",
                            body = doc.getString("body") ?: "",
                            createdBy = doc.getString("createdBy") ?: "",
                            isTeacherMade = doc.getBoolean("isTeacherMade") ?: false,
                            levelId = doc.getString("levelId") ?: "level-1",
                            coverImageUrl = doc.getString("coverImageUrl") ?: "",
                            illustrationImageUrl = doc.getString("illustrationImageUrl") ?: ""
                        )
                    }.getOrNull()
                }.filter { it.title.isNotBlank() && it.body.isNotBlank() }
                onResult(Result.success(docs))
            }
            .addOnFailureListener { error ->
                val message = if (error is FirebaseNetworkException ||
                    (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.UNAVAILABLE)
                ) {
                    "Could not reach Firestore to load reading materials. Check your internet connection."
                } else {
                    error.localizedMessage ?: "Could not load reading materials from Firestore."
                }
                onResult(Result.failure(IllegalStateException(message, error)))
            }
    }

    fun loadDictionaryWords(onResult: (List<DictionaryWord>) -> Unit) {
        onResult(AppState.dictionaryWords.toList())
        val db = firestore ?: return
        db.collection(FirebaseCollections.DICTIONARY_WORDS).get()
            .addOnSuccessListener { snap: QuerySnapshot ->
                val docs = snap.documents.mapNotNull { doc ->
                    runCatching {
                        DictionaryWord(
                            word = doc.getString("word") ?: "",
                            definition = doc.getString("definition") ?: "",
                            example = doc.getString("example") ?: "",
                            partOfSpeech = doc.getString("partOfSpeech") ?: "",
                            materialId = doc.getString("materialId") ?: "",
                            level = doc.getString("level") ?: "Grade 7"
                        )
                    }.getOrNull()
                }.filter { it.word.isNotBlank() && it.definition.isNotBlank() }
                if (docs.isNotEmpty()) onResult(docs)
            }
            .addOnFailureListener { /* keep local words */ }
    }

    fun loadQuestions(materialId: String, onResult: (List<Question>) -> Unit) {
        onResult(SampleData.questionsFor(materialId))
        val db = firestore ?: return
        db.collection(FirebaseCollections.QUESTIONS)
            .whereEqualTo("materialId", materialId)
            .whereEqualTo("answerKeySeparated", true)
            .get()
            .addOnSuccessListener { snap: QuerySnapshot ->
                val docs = snap.documents.mapNotNull { doc ->
                    runCatching {
                        @Suppress("UNCHECKED_CAST")
                        val choices = (doc.get("choices") as? List<*>)?.map { it.toString() } ?: emptyList()
                        Question(
                            id = doc.id,
                            materialId = doc.getString("materialId") ?: materialId,
                            question = doc.getString("question") ?: "",
                            choices = choices,
                            correctIndex = -1,
                            questionOrder = (doc.getLong("questionOrder") ?: 0L).toInt(),
                            createdBy = doc.getString("createdBy") ?: "",
                            isTeacherMade = doc.getBoolean("isTeacherMade") ?: false
                        )
                    }.getOrNull()
                }.filter { it.question.isNotBlank() && it.choices.size == 4 }
                    .sortedBy { it.questionOrder }
                if (docs.isNotEmpty()) onResult(docs)
            }
            .addOnFailureListener { /* keep sample questions */ }
    }

    fun recordTrustedQuizScore(score: Score) {
        AppState.recordScore(score)
    }

    fun loadScores(onResult: (Result<List<Score>>) -> Unit) {
        val uid = currentFirebaseUid
            ?: return onResult(Result.failure(IllegalStateException("Sign in with Firebase to load quiz scores.")))
        val db = firestore
            ?: return onResult(Result.failure(IllegalStateException("Firestore is unavailable.")))

        db.collection(FirebaseCollections.SCORES)
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val scores = snapshot.documents.mapNotNull { document ->
                    val score = document.getLong("score")?.toInt()
                    val total = document.getLong("total")?.toInt()
                    val materialId = document.getString("materialId")
                    if (score == null || total == null || total <= 0 || materialId.isNullOrBlank()) {
                        null
                    } else {
                        Score(
                            id = document.id,
                            userId = uid,
                            materialId = materialId,
                            materialTitle = document.getString("materialTitle").orEmpty(),
                            score = score,
                            total = total,
                            date = document.getLong("date") ?: 0L
                        )
                    }
                }.sortedByDescending { it.date }
                onResult(Result.success(scores))
            }
            .addOnFailureListener { error ->
                val message = if (error is FirebaseNetworkException ||
                    (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.UNAVAILABLE)
                ) {
                    "Could not reach Firestore to load quiz scores. Check your internet connection."
                } else {
                    error.localizedMessage ?: "Could not load quiz scores from Firestore."
                }
                onResult(Result.failure(IllegalStateException(message, error)))
            }
    }

    fun submitQuiz(
        materialId: String,
        answers: Map<String, Int>,
        attemptId: String,
        onResult: (Result<QuizSubmissionResult>) -> Unit
    ) {
        if (currentFirebaseUid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Sign in with Firebase before submitting this quiz.")))
            return
        }

        val answerPayload = answers.map { (questionId, selectedIndex) ->
            mapOf("questionId" to questionId, "selectedIndex" to selectedIndex)
        }
        try {
            FirebaseFunctions.getInstance().getHttpsCallable("submitQuiz")
                .call(
                    mapOf(
                        "materialId" to materialId,
                        "answers" to answerPayload,
                        "attemptId" to attemptId
                    )
                )
                .addOnSuccessListener { response ->
                    val data = response.data as? Map<*, *>
                    val score = (data?.get("score") as? Number)?.toInt()
                    val total = (data?.get("total") as? Number)?.toInt()
                    val percent = (data?.get("percentage") as? Number)?.toInt()
                    val passed = data?.get("passed") as? Boolean
                    val feedback = data?.get("feedback") as? String
                    val resultAttemptId = data?.get("attemptId") as? String
                    val scoreId = data?.get("scoreId") as? String
                    if (score == null || total == null || percent == null || passed == null ||
                        feedback == null || resultAttemptId == null || scoreId == null
                    ) {
                        onResult(Result.failure(IllegalStateException("The grading service returned an invalid result.")))
                    } else {
                        onResult(
                            Result.success(
                                QuizSubmissionResult(
                                    score = score,
                                    total = total,
                                    percentage = percent,
                                    passed = passed,
                                    feedback = feedback,
                                    attemptId = resultAttemptId,
                                    scoreId = scoreId
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { onResult(Result.failure(callableError(it))) }
        } catch (e: Exception) {
            onResult(Result.failure(callableError(e)))
        }
    }

    private fun callableError(error: Exception): Exception = when (error) {
        is FirebaseNetworkException -> IllegalStateException(
            "Could not reach the quiz grading service. Check your internet connection and try again.",
            error
        )
        is FirebaseFunctionsException -> when (error.code) {
            FirebaseFunctionsException.Code.UNAVAILABLE,
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                IllegalStateException("The quiz grading service is unavailable. Try again later.", error)
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                IllegalStateException("Your Firebase session expired. Sign in again to submit this quiz.", error)
            else -> IllegalStateException(error.message ?: "Quiz submission failed. Please try again.", error)
        }
        else -> IllegalStateException(error.localizedMessage ?: "Quiz submission failed. Please try again.", error)
    }

    fun loadTeacherQuestions(materialId: String, onResult: (Result<List<Question>>) -> Unit) {
        val db = firestore
            ?: return onResult(Result.failure(IllegalStateException("Firestore is unavailable.")))
        val uid = currentFirebaseUid
            ?: return onResult(Result.failure(IllegalStateException("Sign in with Firebase to edit this quiz.")))

        db.collection(FirebaseCollections.QUESTIONS)
            .whereEqualTo("materialId", materialId)
            .whereEqualTo("createdBy", uid)
            .get()
            .addOnSuccessListener { questionsSnapshot ->
                db.collection(FirebaseCollections.QUESTION_ANSWER_KEYS)
                    .whereEqualTo("materialId", materialId)
                    .whereEqualTo("createdBy", uid)
                    .get()
                    .addOnSuccessListener { keysSnapshot ->
                        val keysById = keysSnapshot.documents.associateBy { it.id }
                        val questions = questionsSnapshot.documents.mapNotNull { doc ->
                            runCatching {
                                @Suppress("UNCHECKED_CAST")
                                val choices = (doc.get("choices") as? List<*>)?.map { it.toString() } ?: emptyList()
                                val keyIndex = keysById[doc.id]?.getLong("correctIndex")
                                Question(
                                    id = doc.id,
                                    materialId = doc.getString("materialId") ?: materialId,
                                    question = doc.getString("question") ?: "",
                                    choices = choices,
                                    correctIndex = (keyIndex ?: doc.getLong("correctIndex") ?: -1L).toInt(),
                                    questionOrder = (doc.getLong("questionOrder") ?: 0L).toInt(),
                                    createdBy = doc.getString("createdBy") ?: uid,
                                    isTeacherMade = doc.getBoolean("isTeacherMade") ?: true
                                )
                            }.getOrNull()
                        }.sortedBy { it.questionOrder }
                        onResult(Result.success(questions))
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun saveTeacherMaterial(material: ReadingMaterial, questions: List<Question>, onResult: (Result<Unit>) -> Unit) {
        val db = firestore ?: return onResult(Result.failure(IllegalStateException("Firestore is unavailable. Material was not saved.")))
        val uid = currentFirebaseUid
            ?: return onResult(Result.failure(IllegalStateException("Sign in with Firebase before saving teacher materials.")))
        if (material.createdBy != uid
            || questions.isEmpty()
            || questions.size > 5
            || questions.map { it.id }.toSet().size != questions.size
            || questions.any {
                it.id.isBlank()
                    || it.materialId != material.id
                    || it.createdBy != uid
                    || it.question.isBlank()
                    || it.choices.size != 4
                    || it.choices.any(String::isBlank)
                    || it.correctIndex !in it.choices.indices
            }
        ) {
            return onResult(Result.failure(IllegalArgumentException("The signed-in teacher and quiz ownership must match.")))
        }

        db.collection(FirebaseCollections.QUESTIONS)
            .whereEqualTo("materialId", material.id)
            .whereEqualTo("createdBy", uid)
            .get()
            .addOnSuccessListener { existingQuestions ->
                db.collection(FirebaseCollections.QUESTION_ANSWER_KEYS)
                    .whereEqualTo("materialId", material.id)
                    .whereEqualTo("createdBy", uid)
                    .get()
                    .addOnSuccessListener { existingKeys ->
                        val batch = db.batch()
                        val materialRef = db.collection(FirebaseCollections.READING_MATERIALS).document(material.id)
                        val materialData = mutableMapOf<String, Any>(
                            "title" to material.title,
                            "level" to material.level,
                            "topic" to material.topic,
                            "category" to material.category,
                            "kind" to material.kind,
                            "body" to material.body,
                            "createdBy" to material.createdBy,
                            "isTeacherMade" to true,
                            "levelId" to material.levelId,
                            "coverImageUrl" to material.coverImageUrl,
                            "illustrationImageUrl" to material.illustrationImageUrl,
                            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                        if (existingQuestions.isEmpty && existingKeys.isEmpty()) {
                            materialData["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                        }
                        batch.set(materialRef, materialData)

                        val retainedQuestionIds = questions.map { it.id }.toSet()
                        questions.forEachIndexed { index, question ->
                            val questionRef = db.collection(FirebaseCollections.QUESTIONS).document(question.id)
                            batch.set(
                                questionRef,
                                mapOf(
                                    "materialId" to material.id,
                                    "question" to question.question,
                                    "choices" to question.choices,
                                    "questionOrder" to index,
                                    "createdBy" to uid,
                                    "isTeacherMade" to true,
                                    "answerKeySeparated" to true
                                )
                            )
                            batch.set(
                                db.collection(FirebaseCollections.QUESTION_ANSWER_KEYS).document(question.id),
                                mapOf(
                                    "materialId" to material.id,
                                    "correctIndex" to question.correctIndex,
                                    "createdBy" to uid
                                )
                            )
                        }

                        existingQuestions.documents
                            .filterNot { it.id in retainedQuestionIds }
                            .forEach { batch.delete(it.reference) }
                        existingKeys.documents
                            .filterNot { it.id in retainedQuestionIds }
                            .forEach { batch.delete(it.reference) }

                        batch.commit()
                            .addOnSuccessListener { onResult(Result.success(Unit)) }
                            .addOnFailureListener { onResult(Result.failure(it)) }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteTeacherMaterial(material: ReadingMaterial, requesterUid: String, onResult: (Result<Unit>) -> Unit) {
        if (!material.isTeacherMade || material.createdBy != requesterUid) return onResult(Result.failure(IllegalAccessException("You can only delete your own teacher materials.")))
        if (currentFirebaseUid != requesterUid) return onResult(Result.failure(IllegalAccessException("The signed-in teacher does not own this material.")))
        val db = firestore ?: return onResult(Result.failure(IllegalStateException("Firestore is unavailable.")))
        val batch = db.batch()
        batch.delete(db.collection(FirebaseCollections.READING_MATERIALS).document(material.id))
        db.collection(FirebaseCollections.QUESTIONS)
            .whereEqualTo("materialId", material.id)
            .whereEqualTo("createdBy", requesterUid)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { batch.delete(it.reference) }
                db.collection(FirebaseCollections.QUESTION_ANSWER_KEYS)
                    .whereEqualTo("materialId", material.id)
                    .whereEqualTo("createdBy", requesterUid)
                    .get()
                    .addOnSuccessListener { keysSnapshot ->
                        keysSnapshot.documents.forEach { batch.delete(it.reference) }
                        batch.commit()
                            .addOnSuccessListener { onResult(Result.success(Unit)) }
                            .addOnFailureListener { onResult(Result.failure(it)) }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }.addOnFailureListener { onResult(Result.failure(it)) }
    }
}

data class QuizSubmissionResult(
    val score: Int,
    val total: Int,
    val percentage: Int,
    val passed: Boolean,
    val feedback: String,
    val attemptId: String,
    val scoreId: String
)
