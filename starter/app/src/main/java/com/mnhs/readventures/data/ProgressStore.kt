package com.mnhs.readventures.data

import android.content.Context
import android.content.SharedPreferences

/**
 * On-device progress storage (SharedPreferences).
 *
 * Saves reading progress per material (save-and-continue), finished
 * materials, and quiz scores. The app works fully offline for progress;
 * Firebase is only needed for login and for syncing content/scores.
 */
class ProgressStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("readventures_progress", Context.MODE_PRIVATE)

    fun saveReadingProgress(materialId: String, percent: Int) {
        prefs.edit().putInt("progress_$materialId", percent.coerceIn(0, 100)).apply()
        if (percent >= 100) {
            prefs.edit().putBoolean("read_$materialId", true).apply()
        }
    }

    fun readingProgress(materialId: String): Int =
        prefs.getInt("progress_$materialId", 0)

    fun isFinished(materialId: String): Boolean =
        prefs.getBoolean("read_$materialId", false)

    /** Stored as "score/total|score/total|..." per material, newest last. */
    fun saveScore(materialId: String, title: String, score: Int, total: Int) {
        val key = "scores_$materialId"
        val existing = prefs.getString(key, "") ?: ""
        val entry = "$score/$total"
        val updated = if (existing.isEmpty()) entry else "$existing|$entry"
        prefs.edit().putString(key, updated).apply()
        prefs.edit().putInt("best_$materialId", maxOf(bestScore(materialId), score)).apply()
        prefs.edit().putLong("last_quiz", System.currentTimeMillis()).apply()
    }

    fun bestScore(materialId: String): Int =
        prefs.getInt("best_$materialId", 0)

    fun scoresFor(materialId: String): List<Pair<Int, Int>> {
        val raw = prefs.getString("scores_$materialId", "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split("|").mapNotNull {
            val parts = it.split("/")
            if (parts.size == 2) {
                val s = parts[0].toIntOrNull()
                val t = parts[1].toIntOrNull()
                if (s != null && t != null) s to t else null
            } else null
        }
    }

    fun allScores(): List<Score> {
        val out = mutableListOf<Score>()
        for (material in SampleData.materials) {
            for ((s, t) in scoresFor(material.id)) {
                out.add(
                    Score(
                        id = "${material.id}-$s-$t",
                        userId = AppState.currentUser.value.uid,
                        materialId = material.id,
                        materialTitle = material.title,
                        score = s,
                        total = t,
                        date = System.currentTimeMillis()
                    )
                )
            }
        }
        return out
    }

    fun loadIntoAppState() {
        for (material in SampleData.materials) {
            val progress = readingProgress(material.id)
            if (progress > 0) AppState.readingProgress[material.id] = progress
            if (isFinished(material.id) && !AppState.finishedReading.contains(material.id)) {
                AppState.finishedReading.add(material.id)
            }
            val best = bestScore(material.id)
            if (best > 0) AppState.bestScores[material.id] = best
        }
        for (score in allScores()) {
            if (AppState.scores.none { it.materialId == score.materialId && it.score == score.score && it.total == score.total }) {
                AppState.scores.add(score)
            }
        }
    }

    // --- Sample-mode account (used when Firebase is not configured) ---

    fun samplePassword(): String = prefs.getString("sample_password", "password123") ?: "password123"

    fun setSamplePassword(newPassword: String) {
        prefs.edit().putString("sample_password", newPassword).apply()
    }

    fun savedDisplayName(): String = prefs.getString("display_name", "") ?: ""

    fun saveDisplayName(name: String) {
        prefs.edit().putString("display_name", name).apply()
    }
}
