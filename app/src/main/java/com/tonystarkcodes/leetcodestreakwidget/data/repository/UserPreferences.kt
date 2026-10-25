package com.tonystarkcodes.leetcodestreakwidget.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.tonystarkcodes.leetcodestreakwidget.data.model.LeetCodeProfile

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("leetcode_streak_widget_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USERNAME = "key_username"
        private const val KEY_CACHED_PROFILE = "key_cached_profile"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_SYNC_INTERVAL = "key_sync_interval_hours"
    }

    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username.trim()).apply()
    }

    fun getUsername(): String {
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }

    fun saveProfile(profile: LeetCodeProfile) {
        val json = gson.toJson(profile)
        prefs.edit()
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_CACHED_PROFILE, json)
            .apply()
    }

    fun getCachedProfile(): LeetCodeProfile? {
        val json = prefs.getString(KEY_CACHED_PROFILE, null) ?: return null
        return try {
            gson.fromJson(json, LeetCodeProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun setAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun isAutoSyncEnabled(): Boolean {
        // Battery-first default: manual only
        return prefs.getBoolean(KEY_AUTO_SYNC, false)
    }

    fun setSyncIntervalHours(hours: Int) {
        prefs.edit().putInt(KEY_SYNC_INTERVAL, hours).apply()
    }

    fun getSyncIntervalHours(): Int {
        return prefs.getInt(KEY_SYNC_INTERVAL, 12)
    }
}
