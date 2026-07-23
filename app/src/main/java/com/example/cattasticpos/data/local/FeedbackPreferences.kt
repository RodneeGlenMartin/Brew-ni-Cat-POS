package com.example.cattasticpos.data.local

import android.content.Context
import android.content.SharedPreferences

class FeedbackPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var soundsEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUNDS, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUNDS, value).apply()

    companion object {
        private const val PREFS_NAME = "pos_feedback_prefs"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_SOUNDS = "sounds_enabled"
    }
}
