package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val alarmIndex = intent.getIntExtra("alarm_index", 0)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.example.data.AppDatabase.getDatabase(context)
                val profile = db.dao().getUserProfile()
                val name = profile?.name.orEmpty().trim()
                
                val studentName = if (name.isNotEmpty()) " قهرمانم $name" else " قهرمان جدول ضرب"
                
                val title = when (alarmIndex) {
                    0 -> "صبح بخیر$studentName! ☀️"
                    1 -> "ظهر شده$studentName! ⚡"
                    else -> "عصر شده$studentName! 🏆"
                }
                
                val content = when (alarmIndex) {
                    0 -> "امروز چند دقیقه بازی کن تا مهارت ضربت همیشه اول باشه! 🎯"
                    1 -> "وقتشه مغزت رو به یک چالش سرعت هیجان‌انگیز دعوت کنی! 🔥"
                    else -> "مبارزه ریاضی امشب منتظرته! آخرین مسابقه رو برنده شو! 💎"
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "multiplication_daily_reminder"
                val channelName = "یادآوری مبارزه ریاضی"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "یادآوری‌های جذاب منظم جهت یادگیری و حل جدول ضرب"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    100 + alarmIndex,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(1001 + alarmIndex, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    ReminderScheduler.scheduleSingleReminder(context, alarmIndex)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                pendingResult.finish()
            }
        }
    }
}
