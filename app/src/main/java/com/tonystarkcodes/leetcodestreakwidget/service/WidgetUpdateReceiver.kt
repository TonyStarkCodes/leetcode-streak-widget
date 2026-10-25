package com.tonystarkcodes.leetcodestreakwidget.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.tonystarkcodes.leetcodestreakwidget.data.repository.LeetCodeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REFRESH = "com.tonystarkcodes.leetcodestreakwidget.ACTION_REFRESH_WIDGET"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_REFRESH) {
            val pendingResult = goAsync()
            val mainHandler = Handler(Looper.getMainLooper())

            mainHandler.post {
                Toast.makeText(context, "Refreshing LeetCode streak...", Toast.LENGTH_SHORT).show()
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repo = LeetCodeRepository(context)
                    val result = repo.refreshCurrentProfile()
                    mainHandler.post {
                        if (result.isSuccess) {
                            Toast.makeText(context, "Streak updated! 🔥", Toast.LENGTH_SHORT).show()
                        } else {
                            val msg = result.exceptionOrNull()?.localizedMessage ?: "Sync error"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
