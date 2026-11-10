package com.mnhs.readventures.data

// Data models — match the Firebase collections in README.md.
// Field names are kept simple so they map 1:1 to Firestore documents.

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student" // "student" or "teacher"
)

data class ReadingMaterial(
    val id: String = "",
    val title: String = "",
    val level: String = "", // e.g. "Level 1 (Grade 7)", "Level 2 (Intermediate)", etc.
    val topic: String = "", // e.g. "Life Lessons", "Science"
    val category: String = "", // Fiction / Non-Fiction / Science / Classic
    val kind: String = "Stories", // "Stories" or "Informational" (Library filter)
    val body: String = "",
    val createdBy: String = "",
    val isTeacherMade: Boolean = false,
    val levelId: String = "level-1" // "level-1", "level-2", "level-3"
)

data class Question(
    val id: String = "",
    val materialId: String = "",
    val question: String = "",
    val choices: List<String> = emptyList(), // multiple-choice, 4 options
    val correctIndex: Int = 0,
    val createdBy: String = "",
    val isTeacherMade: Boolean = false
)

data class Score(
    val id: String = "",
    val userId: String = "",
    val materialId: String = "",
    val materialTitle: String = "",
    val score: Int = 0,
    val total: Int = 0,
    val date: Long = 0L // epoch millis
)

data class DictionaryWord(
    val word: String = "",
    val definition: String = "",
    val example: String = "",
    val partOfSpeech: String = "", // noun, verb, adjective, etc.
    val materialId: String = "",
    val level: String = "Grade 7"
)

data class Level(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val order: Int = 1
)

data class SchoolClass(
    val classId: String = "",
    val classCode: String = "", // e.g. READ-7A1
    val className: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val schoolYear: String = ""
)

data class ClassMember(
    val classId: String = "",
    val studentId: String = "",
    val studentName: String = ""
)

data class ClassAssignment(
    val id: String = "",
    val classId: String = "",
    val materialId: String = "",
    val title: String = "",
    val dueDate: String = "",
    val level: String = "",
    val assignedBy: String = ""
)
