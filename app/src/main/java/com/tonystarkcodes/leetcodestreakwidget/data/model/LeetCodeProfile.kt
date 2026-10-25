package com.tonystarkcodes.leetcodestreakwidget.data.model

data class LeetCodeProfile(
    val username: String,
    val realName: String = "",
    val userAvatar: String = "",
    val ranking: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalActiveDays: Int = 0,
    val solvedToday: Boolean = false,
    val statusMessage: String = "",
    val streakExplanation: String = "",
    val totalSolved: Int = 0,
    val easySolved: Int = 0,
    val mediumSolved: Int = 0,
    val hardSolved: Int = 0,
    val lastSyncTimestamp: Long = 0L,
    val rawCalendarJson: String = ""
)
