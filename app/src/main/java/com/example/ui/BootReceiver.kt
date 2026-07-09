package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MedApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            val app = context.applicationContext as MedApplication
            val alarmScheduler = AlarmScheduler(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val medications = app.repository.activeMedications.first()
                medications.forEach { med ->
                    alarmScheduler.schedule(med)
                }
            }
        }
    }
}
