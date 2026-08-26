package com.swagatmali.remider.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * AlarmManager alarms are cleared when the device reboots. On BOOT_COMPLETED we
 * enqueue [RescheduleWorker] to re-arm every future reminder. Registered in the
 * app manifest with RECEIVE_BOOT_COMPLETED.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val request = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
