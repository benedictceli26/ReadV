package com.mnhs.readventures.data

import android.content.Context
import android.content.SharedPreferences

/**
 * On-device progress storage (SharedPreferences).
 *
 * Saves reading progress per material (save-and-continue) and finished
 * materials. Account and quiz-score data are backed by Firebase.
 */
class ProgressStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("readventures_progress", Context.MODE_PRIVATE)

    init {
        prefs.edit().remove("sample_password").remove("display_name").apply()
    }

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

    fun loadIntoAppState() {
        for (material in SampleData.materials) {
            val progress = readingProgress(material.id)
            if (progress > 0) AppState.readingProgress[material.id] = progress
            if (isFinished(material.id) && !AppState.finishedReading.contains(material.id)) {
                AppState.finishedReading.add(material.id)
            }
        }
    }
}
