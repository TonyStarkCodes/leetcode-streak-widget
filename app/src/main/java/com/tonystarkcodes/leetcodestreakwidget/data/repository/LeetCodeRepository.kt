package com.tonystarkcodes.leetcodestreakwidget.data.repository

import android.content.Context
import com.tonystarkcodes.leetcodestreakwidget.data.api.LeetCodeApiClient
import com.tonystarkcodes.leetcodestreakwidget.data.model.LeetCodeProfile
import com.tonystarkcodes.leetcodestreakwidget.widget.WidgetRenderer

class LeetCodeRepository(private val context: Context) {

    private val apiClient = LeetCodeApiClient()
    val preferences = UserPreferences(context)

    suspend fun syncProfile(username: String): Result<LeetCodeProfile> {
        val result = apiClient.fetchUserProfile(username)
        if (result.isSuccess) {
            val profile = result.getOrThrow()
            preferences.saveProfile(profile)
            // Immediately sync to all home screen widgets
            WidgetRenderer.updateAllWidgets(context, profile)
        }
        return result
    }

    suspend fun refreshCurrentProfile(): Result<LeetCodeProfile> {
        val username = preferences.getUsername()
        if (username.isBlank()) {
            return Result.failure(IllegalStateException("No username set. Please enter your handle first."))
        }
        return syncProfile(username)
    }

    fun getCachedProfile(): LeetCodeProfile? {
        return preferences.getCachedProfile()
    }
}
