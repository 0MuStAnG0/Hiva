package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class MedicationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "MedAlarm::AlarmReceiverWakeLock"
        )
        wakeLock.acquire(60 * 1000L /*1 minute*/)

        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isAlarmEnabled = prefs.getBoolean("is_alarm_enabled", true)
        
        if (!isAlarmEnabled) {
            wakeLock.release()
            return
        }

        val medId = intent.getIntExtra("MED_ID", 0)
        val medName = intent.getStringExtra("MED_NAME") ?: "دارو"
        val medDose = intent.getStringExtra("MED_DOSE") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("med_channel", "Medication Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for taking medications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            medId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            medId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val notification = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("یادآوری مصرف دارو")
            .setContentText("زمان مصرف $medName ($medDose) فرا رسیده است.")
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .build()

        notificationManager.notify(medId, notification)
    }
}
