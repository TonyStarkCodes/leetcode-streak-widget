package com.tonystarkcodes.leetcodestreakwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.tonystarkcodes.leetcodestreakwidget.data.repository.UserPreferences

class StreakWidget2x1Provider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val profile = UserPreferences(context).getCachedProfile()
        val views = WidgetRenderer.buildWidget2x1(context, profile)
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
