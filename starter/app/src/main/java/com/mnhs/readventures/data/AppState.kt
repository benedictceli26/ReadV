package com.mnhs.readventures.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

/**
 * In-memory app state, shared across screens.
 *
 * Progress is persisted to SharedPreferences by ProgressStore; this object
 * is the single source of truth while the app is running so every screen
 * shows the same numbers without re-reading storage.
 */
object AppState {
    var currentUser = mutableStateOf(AppUser())
    var isLoggedIn = mutableStateOf(false)
    var isSampleMode = mutableStateOf(true)

    /** Solo Mode vs Class Mode switch on Home screen. */
    var isClassMode = mutableStateOf(false)

    /** Reading materials and questions (reactive, includes teacher-created). */
    val materials = mutableStateListOf<ReadingMaterial>().apply {
        addAll(SampleData.materials)
    }

    val questions = mutableStateListOf<Question>().apply {
        addAll(SampleData.questions)
    }

    val dictionaryWords = mutableStateListOf<DictionaryWord>().apply {
        addAll(SampleData.dictionaryWords)
    }

    val levels = mutableStateListOf<Level>().apply {
        addAll(SampleData.levels)
    }

    val classes = mutableStateListOf<SchoolClass>().apply {
        add(SampleData.demoClass)
    }

    val classMembers = mutableStateListOf<ClassMember>().apply {
        addAll(SampleData.demoMembers)
    }

    val classAssignments = mutableStateListOf<ClassAssignment>().apply {
        addAll(SampleData.demoAssignments)
    }

    /** Codes of classes the currently logged-in student has joined. */
    val enrolledClassCodes = mutableStateListOf<String>().apply {
        add("READ-7A1") // Default enrolled in demo class for Juan Dela Cruz
    }

    /** materialId -> reading progress 0..100 (save-and-continue). */
    val readingProgress = mutableStateMapOf<String, Int>()

    /** materialIds the student finished reading. */
    val finishedReading = mutableStateListOf<String>()

    /** Quiz scores, newest first. */
    val scores = mutableStateListOf<Score>()

    /** materialId -> best quiz score, for the Library / Progress screens. */
    val bestScores = mutableStateMapOf<String, Int>()

    var notificationsEnabled = mutableStateOf(true)

    fun resetForLogin(user: AppUser, sampleMode: Boolean) {
        currentUser.value = user
        isLoggedIn.value = true
        isSampleMode.value = sampleMode
        if (user.role == "teacher") {
            isClassMode.value = false
        }
    }

    fun logOut() {
        currentUser.value = AppUser()
        isLoggedIn.value = false
        isClassMode.value = false
    }

    fun recordReadingProgress(materialId: String, percent: Int) {
        readingProgress[materialId] = percent.coerceIn(0, 100)
        if (percent >= 100 && !finishedReading.contains(materialId)) {
            finishedReading.add(materialId)
        }
    }

    fun recordScore(score: Score) {
        scores.add(0, score)
        val best = bestScores[score.materialId] ?: 0
        if (score.score > best) bestScores[score.materialId] = score.score
    }

    fun readCount(): Int = finishedReading.size

    fun quizzesTaken(): Int = scores.size

    fun averageScorePercent(): Int {
        if (scores.isEmpty()) return 0
        val totalScored = scores.sumOf { it.score }
        val totalPossible = scores.sumOf { it.total }
        if (totalPossible == 0) return 0
        return (totalScored * 100) / totalPossible
    }

    fun continueReadingMaterial(): ReadingMaterial? {
        // First started-but-unfinished material, else the first unlocked material.
        val started = materials.firstOrNull {
            (readingProgress[it.id] ?: 0) in 1..99 && isLevelUnlocked(it.levelId)
        }
        return started ?: materials.firstOrNull { isLevelUnlocked(it.levelId) } ?: materials.firstOrNull()
    }

    // --- FEATURE 2: Level Progression & Gating ---

    /**
     * Unlock rule: finish all materials in a level AND pass their quizzes
     * with >= 80% (4 out of 5) to unlock the next level.
     */
    fun isLevelUnlocked(levelId: String): Boolean {
        if (levelId.isBlank() || levelId == "level-1") return true

        if (levelId == "level-2") {
            val level1Materials = materials.filter { it.levelId == "level-1" }
            if (level1Materials.isEmpty()) return true
            return level1Materials.all { mat ->
                finishedReading.contains(mat.id) && (bestScores[mat.id] ?: 0) >= 4
            }
        }

        if (levelId == "level-3") {
            if (!isLevelUnlocked("level-2")) return false
            val level2Materials = materials.filter { it.levelId == "level-2" }
            if (level2Materials.isEmpty()) return true
            return level2Materials.all { mat ->
                finishedReading.contains(mat.id) && (bestScores[mat.id] ?: 0) >= 4
            }
        }

        return true
    }

    fun levelUnlockRequirement(levelId: String): String = when (levelId) {
        "level-1" -> "Open by default"
        "level-2" -> "Finish all Level 1 stories and pass their quizzes with 80%+ (4/5) to unlock."
        "level-3" -> "Finish all Level 2 stories and pass their quizzes with 80%+ (4/5) to unlock."
        else -> "Complete prior levels to unlock."
    }

    fun currentLevel(): Level {
        return when {
            isLevelUnlocked("level-3") -> levels.firstOrNull { it.id == "level-3" } ?: SampleData.levels[2]
            isLevelUnlocked("level-2") -> levels.firstOrNull { it.id == "level-2" } ?: SampleData.levels[1]
            else -> levels.firstOrNull { it.id == "level-1" } ?: SampleData.levels[0]
        }
    }

    // --- FEATURE 3: Class Code + Enrolment ---

    fun joinClass(code: String, studentId: String, studentName: String): Result<SchoolClass> {
        val trimmed = code.trim().uppercase()
        val found = classes.firstOrNull { it.classCode.uppercase() == trimmed }
            ?: return Result.failure(IllegalArgumentException("Invalid class code: $trimmed. Please check with your teacher."))

        if (!enrolledClassCodes.contains(found.classCode)) {
            enrolledClassCodes.add(found.classCode)
        }
        if (classMembers.none { it.classId == found.classId && it.studentId == studentId }) {
            classMembers.add(ClassMember(found.classId, studentId, studentName))
        }
        return Result.success(found)
    }

    fun createClass(name: String, subject: String, schoolYear: String, teacherId: String, teacherName: String): SchoolClass {
        val codeSuffix = ('A'..'Z').random().toString() + (10..99).random().toString()
        val generatedCode = "READ-7$codeSuffix"
        val newClass = SchoolClass(
            classId = "class-${System.currentTimeMillis()}",
            classCode = generatedCode,
            className = name.trim(),
            teacherId = teacherId,
            teacherName = teacherName.ifBlank { "Teacher" },
            subject = subject.trim(),
            schoolYear = schoolYear.trim()
        )
        classes.add(0, newClass)
        return newClass
    }

    fun assignReading(classId: String, materialId: String, title: String, dueDate: String, level: String, assignedBy: String): ClassAssignment {
        val assignment = ClassAssignment(
            id = "assign-${System.currentTimeMillis()}",
            classId = classId,
            materialId = materialId,
            title = title,
            dueDate = dueDate.ifBlank { "Next week" },
            level = level,
            assignedBy = assignedBy
        )
        classAssignments.add(0, assignment)
        return assignment
    }

    // --- FEATURE 4: Teacher Content Management ---

    fun addOrUpdateMaterial(material: ReadingMaterial, materialQuestions: List<Question>) {
        val existingIndex = materials.indexOfFirst { it.id == material.id }
        if (existingIndex >= 0) {
            materials[existingIndex] = material
        } else {
            materials.add(0, material)
        }
        questions.removeAll { it.materialId == material.id }
        questions.addAll(materialQuestions)
    }

    fun deleteMaterial(materialId: String) {
        materials.removeAll { it.id == materialId }
        questions.removeAll { it.materialId == materialId }
        classAssignments.removeAll { it.materialId == materialId }
    }
}
