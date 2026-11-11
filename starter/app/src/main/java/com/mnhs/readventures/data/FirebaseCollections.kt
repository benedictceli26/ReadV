package com.mnhs.readventures.data

// Single source of truth for Firestore collection names.
// Use these everywhere instead of hard-coding strings.
object FirebaseCollections {
    const val USERS = "users"
    const val READING_MATERIALS = "readingMaterials"
    const val QUESTIONS = "questions"
    const val QUESTION_ANSWER_KEYS = "questionAnswerKeys"
    const val SCORES = "scores"
    const val DICTIONARY_WORDS = "dictionary_words"
    const val CLASSES = "classes"
    const val CLASS_MEMBERS = "class_members"
    const val CLASS_ASSIGNMENTS = "class_assignments"
}
