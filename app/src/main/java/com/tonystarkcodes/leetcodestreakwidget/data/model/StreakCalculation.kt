package com.tonystarkcodes.leetcodestreakwidget.data.model

import java.time.LocalDate
import java.time.ZoneId

enum class StreakStatus {
    SOLVED_TODAY,        // User solved a problem today (streak incremented and safe!)
    STREAK_AT_RISK,      // User solved yesterday, but NOT today yet (streak is still intact, solve today!)
    STREAK_LOST          // User did not solve yesterday or today (streak is 0)
}

data class StreakResult(
    val currentStreak: Int,
    val maxStreak: Int,
    val totalActiveDays: Int,
    val solvedToday: Boolean,
    val status: StreakStatus,
    val statusMessage: String,
    val streakExplanation: String
)

object StreakCalculator {
    /**
     * Accurately calculates LeetCode streak from submission calendar JSON map.
     *
     * Why other apps fail:
     * 1. Timezone conversion: LeetCode stores submission timestamps as midnight UTC epoch seconds.
     *    Converting these directly using local timezone without proper calendar day alignment
     *    shifts timestamps backwards or forwards, creating artificial gaps in the streak.
     * 2. Premature streak reset: If it's midway through the day and the user hasn't submitted yet,
     *    their streak is NOT 0! Their streak is still alive from yesterday and pending today's submission.
     * 3. UTC Day boundaries: LeetCode daily problems reset at 00:00 UTC. We cross-check both UTC epoch day
     *    and device local day to ensure submissions are detected accurately across all timezones.
     */
    fun calculate(
        calendarMap: Map<Long, Int>,
        apiReportedStreak: Int? = null,
        apiReportedTotalActive: Int? = null
    ): StreakResult {
        if (calendarMap.isEmpty()) {
            val fallbackStreak = apiReportedStreak ?: 0
            val fallbackTotal = apiReportedTotalActive ?: 0
            val isStreakActive = fallbackStreak > 0
            return StreakResult(
                currentStreak = fallbackStreak,
                maxStreak = fallbackStreak,
                totalActiveDays = fallbackTotal,
                solvedToday = false,
                status = if (isStreakActive) StreakStatus.STREAK_AT_RISK else StreakStatus.STREAK_LOST,
                statusMessage = if (isStreakActive) "Streak Pending Today 🔥" else "No recent submissions",
                streakExplanation = if (isStreakActive) "Streak active from previous days. Solve today to extend!" else "Start your streak by solving a problem today!"
            )
        }

        // Active days set in UTC epoch days (timestamp / 86400)
        val activeEpochDays = mutableSetOf<Long>()
        calendarMap.forEach { (timestamp, count) ->
            if (count > 0) {
                // In LeetCode submissionCalendar, keys are UTC seconds at midnight
                val epochDay = timestamp / 86400L
                activeEpochDays.add(epochDay)
            }
        }

        val nowMillis = System.currentTimeMillis()
        val todayUtcEpochDay = nowMillis / 1000L / 86400L
        val yesterdayUtcEpochDay = todayUtcEpochDay - 1L

        // Also check local timezone day
        val localToday = LocalDate.now(ZoneId.systemDefault())
        val localTodayEpochDay = localToday.toEpochDay()
        val localYesterdayEpochDay = localTodayEpochDay - 1L

        // Did user solve today? Check both UTC day and local day
        val solvedToday = activeEpochDays.contains(todayUtcEpochDay) ||
                          activeEpochDays.contains(localTodayEpochDay)

        // Did user solve yesterday?
        val solvedYesterday = activeEpochDays.contains(yesterdayUtcEpochDay) ||
                              activeEpochDays.contains(localYesterdayEpochDay)

        var currentStreak = 0
        val status: StreakStatus
        val statusMessage: String

        if (solvedToday) {
            status = StreakStatus.SOLVED_TODAY
            statusMessage = "Solved Today! Streak Safe 🔥"

            var dayPointer = if (activeEpochDays.contains(todayUtcEpochDay)) todayUtcEpochDay else localTodayEpochDay
            while (activeEpochDays.contains(dayPointer)) {
                currentStreak++
                dayPointer--
            }
        } else if (solvedYesterday) {
            status = StreakStatus.STREAK_AT_RISK
            statusMessage = "Pending Today! Streak at Risk ⚠️"

            var dayPointer = if (activeEpochDays.contains(yesterdayUtcEpochDay)) yesterdayUtcEpochDay else localYesterdayEpochDay
            while (activeEpochDays.contains(dayPointer)) {
                currentStreak++
                dayPointer--
            }
        } else {
            status = StreakStatus.STREAK_LOST
            statusMessage = "Streak Broken (0 days)"
            currentStreak = 0
        }

        // Reconcile with API reported streak if available
        val finalStreak = if (apiReportedStreak != null && apiReportedStreak > 0) {
            if (solvedToday) {
                maxOf(currentStreak, apiReportedStreak)
            } else if (solvedYesterday) {
                maxOf(currentStreak, apiReportedStreak)
            } else {
                currentStreak
            }
        } else {
            currentStreak
        }

        // Calculate max streak across all recorded history
        var maxStreak = 0
        if (activeEpochDays.isNotEmpty()) {
            val sortedDays = activeEpochDays.sorted()
            var tempStreak = 0
            var prevDay: Long? = null
            for (day in sortedDays) {
                if (prevDay == null || day == prevDay + 1L) {
                    tempStreak++
                } else {
                    maxStreak = maxOf(maxStreak, tempStreak)
                    tempStreak = 1
                }
                prevDay = day
            }
            maxStreak = maxOf(maxStreak, tempStreak)
        }
        maxStreak = maxOf(maxStreak, finalStreak)

        val totalActive = apiReportedTotalActive ?: activeEpochDays.size

        val explanation = when (status) {
            StreakStatus.SOLVED_TODAY -> "Awesome! You have solved problem(s) today. Your streak is safe and incremented."
            StreakStatus.STREAK_AT_RISK -> "Your streak of $finalStreak day(s) is still intact! Solve today's daily problem before midnight UTC to keep it going."
            StreakStatus.STREAK_LOST -> "No activity was recorded yesterday or today. Solve a problem now to ignite a new streak!"
        }

        return StreakResult(
            currentStreak = finalStreak,
            maxStreak = maxStreak,
            totalActiveDays = totalActive,
            solvedToday = solvedToday,
            status = status,
            statusMessage = statusMessage,
            streakExplanation = explanation
        )
    }
}
