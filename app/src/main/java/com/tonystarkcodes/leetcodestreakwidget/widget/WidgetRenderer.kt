package com.tonystarkcodes.leetcodestreakwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.tonystarkcodes.leetcodestreakwidget.R
import com.tonystarkcodes.leetcodestreakwidget.data.model.LeetCodeProfile
import com.tonystarkcodes.leetcodestreakwidget.data.repository.UserPreferences
import com.tonystarkcodes.leetcodestreakwidget.service.WidgetUpdateReceiver
import com.tonystarkcodes.leetcodestreakwidget.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WidgetRenderer {

    fun buildWidget2x1(context: Context, profile: LeetCodeProfile?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_2x1)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 201, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_2x1_root, appPendingIntent)

        if (profile == null) {
            views.setTextViewText(R.id.widget_2x1_streak, "--")
            views.setTextViewText(R.id.widget_2x1_username, "LeetCode")
            views.setTextViewText(R.id.widget_2x1_status_pill, "Tap to setup")
            views.setInt(R.id.widget_2x1_status_pill, "setBackgroundResource", R.drawable.widget_pill_pending)
            views.setTextColor(R.id.widget_2x1_status_pill, ContextCompat.getColor(context, R.color.status_pending_content))
            return views
        }

        views.setTextViewText(R.id.widget_2x1_streak, "${profile.currentStreak}")
        views.setTextViewText(R.id.widget_2x1_username, "@${profile.username}")

        applyStatusPill(context, views, R.id.widget_2x1_status_pill, profile)
        return views
    }

    fun buildWidget2x2(context: Context, profile: LeetCodeProfile?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_2x2)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 202, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_2x2_root, appPendingIntent)

        // Attach 1-tap refresh action
        val refreshIntent = Intent(context, WidgetUpdateReceiver::class.java).apply {
            action = WidgetUpdateReceiver.ACTION_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 203, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_2x2_btn_refresh, refreshPendingIntent)

        if (profile == null) {
            views.setTextViewText(R.id.widget_2x2_streak, "--")
            views.setTextViewText(R.id.widget_2x2_username, "LeetCode")
            views.setTextViewText(R.id.widget_2x2_status_pill, "Tap to setup")
            views.setInt(R.id.widget_2x2_status_pill, "setBackgroundResource", R.drawable.widget_pill_pending)
            views.setTextColor(R.id.widget_2x2_status_pill, ContextCompat.getColor(context, R.color.status_pending_content))
            views.setTextViewText(R.id.widget_2x2_solved, "0 Solved")
            views.setTextViewText(R.id.widget_2x2_sync_time, "Not synced")
            return views
        }

        views.setTextViewText(R.id.widget_2x2_streak, "${profile.currentStreak}")
        views.setTextViewText(R.id.widget_2x2_username, "@${profile.username}")
        views.setTextViewText(R.id.widget_2x2_solved, "${profile.totalSolved} Solved")
        views.setTextViewText(R.id.widget_2x2_sync_time, formatSyncTime(profile.lastSyncTimestamp))

        applyStatusPill(context, views, R.id.widget_2x2_status_pill, profile)
        return views
    }

    fun buildWidget4x2(context: Context, profile: LeetCodeProfile?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_4x2)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 401, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_4x2_root, appPendingIntent)

        // Attach 1-tap refresh action
        val refreshIntent = Intent(context, WidgetUpdateReceiver::class.java).apply {
            action = WidgetUpdateReceiver.ACTION_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 402, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_4x2_btn_refresh, refreshPendingIntent)

        if (profile == null) {
            views.setTextViewText(R.id.widget_4x2_streak, "--")
            views.setTextViewText(R.id.widget_4x2_username, "LeetCode")
            views.setTextViewText(R.id.widget_4x2_rank, "Rank --")
            views.setTextViewText(R.id.widget_4x2_status_pill, "Tap to setup")
            views.setInt(R.id.widget_4x2_status_pill, "setBackgroundResource", R.drawable.widget_pill_pending)
            views.setTextColor(R.id.widget_4x2_status_pill, ContextCompat.getColor(context, R.color.status_pending_content))
            views.setTextViewText(R.id.widget_4x2_streak_subtext, "Best: -- • -- Active Days")
            views.setTextViewText(R.id.widget_4x2_total_solved, "-- Solved")
            views.setTextViewText(R.id.widget_4x2_easy, "--")
            views.setTextViewText(R.id.widget_4x2_med, "--")
            views.setTextViewText(R.id.widget_4x2_hard, "--")
            views.setTextViewText(R.id.widget_4x2_sync_time, "Not synced")
            return views
        }

        views.setTextViewText(R.id.widget_4x2_streak, "${profile.currentStreak}")
        views.setTextViewText(R.id.widget_4x2_username, "@${profile.username}")
        val rankStr = if (profile.ranking > 0) "Rank #${String.format(Locale.US, "%,d", profile.ranking)}" else "Unranked"
        views.setTextViewText(R.id.widget_4x2_rank, rankStr)
        views.setTextViewText(R.id.widget_4x2_streak_subtext, "Best: ${profile.maxStreak}d • ${profile.totalActiveDays} Active Days")

        views.setTextViewText(R.id.widget_4x2_total_solved, "${profile.totalSolved} Solved")
        views.setTextViewText(R.id.widget_4x2_easy, "${profile.easySolved}")
        views.setTextViewText(R.id.widget_4x2_med, "${profile.mediumSolved}")
        views.setTextViewText(R.id.widget_4x2_hard, "${profile.hardSolved}")

        views.setTextViewText(R.id.widget_4x2_sync_time, "Synced: ${formatSyncTime(profile.lastSyncTimestamp)}")

        applyStatusPill(context, views, R.id.widget_4x2_status_pill, profile)
        return views
    }

    private fun applyStatusPill(context: Context, views: RemoteViews, viewId: Int, profile: LeetCodeProfile) {
        when {
            profile.solvedToday -> {
                views.setTextViewText(viewId, "Solved Today 🔥")
                views.setInt(viewId, "setBackgroundResource", R.drawable.widget_pill_solved)
                views.setTextColor(viewId, ContextCompat.getColor(context, R.color.status_solved_content))
            }
            profile.currentStreak > 0 -> {
                views.setTextViewText(viewId, "Pending Today ⚠️")
                views.setInt(viewId, "setBackgroundResource", R.drawable.widget_pill_pending)
                views.setTextColor(viewId, ContextCompat.getColor(context, R.color.status_pending_content))
            }
            else -> {
                views.setTextViewText(viewId, "Streak Lost ⚡")
                views.setInt(viewId, "setBackgroundResource", R.drawable.widget_pill_broken)
                views.setTextColor(viewId, ContextCompat.getColor(context, R.color.status_broken_content))
            }
        }
    }

    private fun formatSyncTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Never"
        val diffSeconds = (System.currentTimeMillis() - timestamp) / 1000
        return when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun updateAllWidgets(context: Context, profile: LeetCodeProfile? = null) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val data = profile ?: UserPreferences(context).getCachedProfile()

        // 2x1
        val comp2x1 = ComponentName(context, StreakWidget2x1Provider::class.java)
        val ids2x1 = appWidgetManager.getAppWidgetIds(comp2x1)
        if (ids2x1.isNotEmpty()) {
            val views = buildWidget2x1(context, data)
            appWidgetManager.updateAppWidget(ids2x1, views)
        }

        // 2x2
        val comp2x2 = ComponentName(context, StreakWidget2x2Provider::class.java)
        val ids2x2 = appWidgetManager.getAppWidgetIds(comp2x2)
        if (ids2x2.isNotEmpty()) {
            val views = buildWidget2x2(context, data)
            appWidgetManager.updateAppWidget(ids2x2, views)
        }

        // 4x2
        val comp4x2 = ComponentName(context, StreakWidget4x2Provider::class.java)
        val ids4x2 = appWidgetManager.getAppWidgetIds(comp4x2)
        if (ids4x2.isNotEmpty()) {
            val views = buildWidget4x2(context, data)
            appWidgetManager.updateAppWidget(ids4x2, views)
        }
    }
}
