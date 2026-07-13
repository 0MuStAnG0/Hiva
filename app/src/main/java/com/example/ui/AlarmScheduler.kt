package com.example.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.Medication
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(medication: Medication) {
        // Cancel all possible existing alarms first (assuming max 10 times per med)
        cancel(medication.id)

        val times = medication.timeOfDay.split(",")
        times.forEachIndexed { index, timeStr ->
            val parts = timeStr.trim().split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: return@forEachIndexed
                val minute = parts[1].toIntOrNull() ?: return@forEachIndexed
                
                val intent = Intent(context, MedicationReceiver::class.java).apply {
                    putExtra("MED_ID", medication.id)
                    putExtra("MED_NAME", medication.name)
                    putExtra("MED_DOSE", medication.currentDose)
                    putExtra("MED_TIME_INDEX", index)
                }
                
                // Use a unique request code for each time
                val requestCode = medication.id * 100 + index
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                var maxLookahead = 30
                while (maxLookahead > 0) {
                    val epochDay = calendar.timeInMillis / 86400000L
                    val isConsumptionDay = if (medication.type == "INJECTION") {
                        val interval = medication.injectionIntervalDays ?: 0
                        if (interval > 0) {
                            epochDay % interval == (medication.id % interval).toLong()
                        } else {
                            true
                        }
                    } else {
                        val skipInterval = medication.skipIntervalDays ?: 0
                        if (skipInterval > 0) {
                            epochDay % skipInterval != (medication.id % skipInterval).toLong()
                        } else {
                            true
                        }
                    }
                    
                    if (isConsumptionDay) {
                        break
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    maxLookahead--
                }
    
                try {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } catch (e: SecurityException) {
                    // Fallback
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancel(medicationId: Int) {
        for (i in 0 until 10) {
            val intent = Intent(context, MedicationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicationId * 100 + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
