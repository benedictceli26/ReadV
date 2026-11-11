package com.mnhs.readventures.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

/**
 * In-memory app state, shared across screens.
 *
 * Reading progress is cached by ProgressStore; Firebase provides account
 * profiles and quiz scores. This object holds the reactive UI state.
 */
object AppState {
    var currentUser = mutableStateOf(AppUser())
    var isLoggedIn = mutableStateOf(false)
    var scoreHistoryError = mutableStateOf<String?>(null)
    var contentLoadError = mutableStateOf<String?>(null)

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

    val classes = mutableStateListOf<SchoolClass>()
    val classMembers = mutableStateListOf<ClassMember>()
    val classAssignments = mutableStateListOf<ClassAssignment>()

    /** Codes of classes the currently logged-in student has joined. */
    val enrolledClassCodes = mutableStateListOf<String>()

    /** materialId -> reading progress 0..100 (save-and-continue). */
    val readingProgress = mutableStateMapOf<String, Int>()

    /** materialIds the student finished reading. */
    val finishedReading = mutableStateListOf<String>()

    /** Quiz scores, newest first. */
    val scores = mutableStateListOf<Score>()

    /** materialId -> best quiz score, for the Library / Progress screens. */
    val bestScores = mutableStateMapOf<String, Int>()

    var notificationsEnabled = mutableStateOf(true)

    fun resetForLogin(user: AppUser) {
        currentUser.value = user
        isLoggedIn.value = true
        scores.clear()
        bestScores.clear()
        scoreHistoryError.value = null
        contentLoadError.value = null
        if (user.role == "teacher") {
            isClassMode.value = false
        }
    }

    fun logOut() {
        currentUser.value = AppUser()
        isLoggedIn.value = false
        isClassMode.value = false
        scores.clear()
        bestScores.clear()
        scoreHistoryError.value = null
        contentLoadError.value = null
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

    fun replaceScores(userScores: List<Score>) {
        val mergedScores = (userScores + scores).distinctBy { it.id }
        scores.clear()
        bestScores.clear()
        val orderedScores = mergedScores.sortedByDescending { it.date }
        scores.addAll(orderedScores)
        orderedScores.forEach { score ->
            bestScores[score.materialId] = maxOf(bestScores[score.materialId] ?: 0, score.score)
        }
        scoreHistoryError.value = null
    }

    fun deleteMaterial(materialId: String) {
        materials.removeAll { it.id == materialId }
        questions.removeAll { it.materialId == materialId }
        classAssignments.removeAll { it.materialId == materialId }
    }
}
