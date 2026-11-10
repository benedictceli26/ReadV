package com.mnhs.readventures.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

/**
 * Reading materials + quiz questions.
 *
 * Loads the bundled sample data first (instant, works offline), then
 * tries Firestore when it is configured. If Firestore returns documents,
 * they replace the sample list; if anything fails, the sample data stays.
 * Scores are saved to Firestore when available and always saved on-device.
 */
class ReadingRepository(private val progressStore: ProgressStore) {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    val isFirestoreAvailable: Boolean get() = firestore != null

    fun loadMaterials(onResult: (List<ReadingMaterial>) -> Unit) {
        // Sample data first — the UI never waits on the network.
        onResult(AppState.materials.toList())
        val db = firestore ?: return
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
                            levelId = doc.getString("levelId") ?: "level-1"
                        )
                    }.getOrNull()
                }.filter { it.title.isNotBlank() && it.body.isNotBlank() }
                if (docs.isNotEmpty()) {
                    onResult(docs)
                }
            }
            .addOnFailureListener {
                // Keep sample data; Firestore is optional for the demo.
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
            .whereEqualTo("materialId", materialId).get()
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
                            correctIndex = (doc.getLong("correctIndex") ?: 0L).toInt()
                        )
                    }.getOrNull()
                }.filter { it.question.isNotBlank() && it.choices.size == 4 }
                if (docs.isNotEmpty()) onResult(docs)
            }
            .addOnFailureListener { /* keep sample questions */ }
    }

    fun saveScore(score: Score) {
        progressStore.saveScore(score.materialId, score.materialTitle, score.score, score.total)
        AppState.recordScore(score)
        val db = firestore ?: return
        val data = mapOf(
            "userId" to score.userId,
            "materialId" to score.materialId,
            "materialTitle" to score.materialTitle,
            "score" to score.score,
            "total" to score.total,
            "date" to score.date
        )
        db.collection(FirebaseCollections.SCORES).add(data)
            .addOnFailureListener { /* on-device score is already saved */ }
    }
}
