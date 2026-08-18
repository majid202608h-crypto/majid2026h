package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {
    private val targetHours = listOf(10, 15, 19)
    private val targetMinutes = listOf(0, 30, 30)

    fun scheduleDailyReminderAlarm(context: Context) {
        for (i in 0..2) {
            scheduleSingleReminder(context, i)
        }
    }

    fun scheduleSingleReminder(context: Context, alarmIndex: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("alarm_index", alarmIndex)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001 + alarmIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, targetHours[alarmIndex])
            set(Calendar.MINUTE, targetMinutes[alarmIndex])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
