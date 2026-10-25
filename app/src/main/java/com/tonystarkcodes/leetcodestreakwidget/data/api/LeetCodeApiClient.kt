package com.tonystarkcodes.leetcodestreakwidget.data.api

import com.tonystarkcodes.leetcodestreakwidget.data.model.LeetCodeProfile
import com.tonystarkcodes.leetcodestreakwidget.data.model.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LeetCodeApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchUserProfile(rawUsername: String): Result<LeetCodeProfile> = withContext(Dispatchers.IO) {
        val username = rawUsername.trim()
        if (username.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Username cannot be empty."))
        }

        val graphQLQuery = """
            query getUserProfileData(${'$'}username: String!) {
              matchedUser(username: ${'$'}username) {
                username
                profile {
                  realName
                  userAvatar
                  ranking
                }
                userCalendar {
                  streak
                  totalActiveDays
                  submissionCalendar
                }
                submitStatsGlobal {
                  acSubmissionNum {
                    difficulty
                    count
                  }
                }
              }
            }
        """.trimIndent()

        val jsonPayload = JSONObject().apply {
            put("query", graphQLQuery)
            put("variables", JSONObject().apply {
                put("username", username)
            })
        }

        val request = Request.Builder()
            .url("https://leetcode.com/graphql")
            .header("Content-Type", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Referer", "https://leetcode.com")
            .header("Origin", "https://leetcode.com")
            .post(jsonPayload.toString().toRequestBody(mediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}: ${response.message}"))
            }

            val bodyString = response.body?.string() ?: ""
            val rootJson = JSONObject(bodyString)

            if (rootJson.has("errors")) {
                val errorsArray = rootJson.getJSONArray("errors")
                if (errorsArray.length() > 0) {
                    val firstErr = errorsArray.getJSONObject(0).optString("message", "API Error")
                    return@withContext Result.failure(Exception(firstErr))
                }
            }

            val dataObj = rootJson.optJSONObject("data")
            val matchedUser = dataObj?.optJSONObject("matchedUser")
                ?: return@withContext Result.failure(Exception("LeetCode user '$username' was not found. Please check spelling."))

            val profileObj = matchedUser.optJSONObject("profile")
            val realName = profileObj?.optString("realName", "") ?: ""
            val userAvatar = profileObj?.optString("userAvatar", "") ?: ""
            val ranking = profileObj?.optInt("ranking", 0) ?: 0

            // User Calendar
            val calendarObj = matchedUser.optJSONObject("userCalendar")
            val apiStreak = calendarObj?.optInt("streak", 0)
            val apiTotalActive = calendarObj?.optInt("totalActiveDays", 0)
            val submissionCalendarStr = calendarObj?.optString("submissionCalendar", "{}") ?: "{}"

            val calendarMap = mutableMapOf<Long, Int>()
            try {
                val calJson = JSONObject(submissionCalendarStr)
                val keys = calJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val count = calJson.optInt(key, 0)
                    key.toLongOrNull()?.let { timestamp ->
                        calendarMap[timestamp] = count
                    }
                }
            } catch (e: Exception) {
                // If parsing raw calendar fails, fallback to empty
            }

            // Submit Stats
            val submitStats = matchedUser.optJSONObject("submitStatsGlobal")
            val acList = submitStats?.optJSONArray("acSubmissionNum")
            var totalSolved = 0
            var easySolved = 0
            var mediumSolved = 0
            var hardSolved = 0

            if (acList != null) {
                for (i in 0 until acList.length()) {
                    val item = acList.getJSONObject(i)
                    val diff = item.optString("difficulty")
                    val count = item.optInt("count", 0)
                    when (diff.lowercase()) {
                        "all" -> totalSolved = count
                        "easy" -> easySolved = count
                        "medium" -> mediumSolved = count
                        "hard" -> hardSolved = count
                    }
                }
            }

            val streakResult = StreakCalculator.calculate(
                calendarMap = calendarMap,
                apiReportedStreak = apiStreak,
                apiReportedTotalActive = apiTotalActive
            )

            val profile = LeetCodeProfile(
                username = matchedUser.optString("username", username),
                realName = realName,
                userAvatar = userAvatar,
                ranking = ranking,
                currentStreak = streakResult.currentStreak,
                maxStreak = streakResult.maxStreak,
                totalActiveDays = streakResult.totalActiveDays,
                solvedToday = streakResult.solvedToday,
                statusMessage = streakResult.statusMessage,
                streakExplanation = streakResult.streakExplanation,
                totalSolved = totalSolved,
                easySolved = easySolved,
                mediumSolved = mediumSolved,
                hardSolved = hardSolved,
                lastSyncTimestamp = System.currentTimeMillis(),
                rawCalendarJson = submissionCalendarStr
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
