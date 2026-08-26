package com.swagatmali.remider.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Re-arms all active reminders after a reboot. Pulls one snapshot of the active
 * list from the repository and hands each to the scheduler (which skips any that
 * are completed or already past due). Dependencies come from the global Koin
 * graph started in the Application.
 */
class RescheduleWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val repository: ReminderRepository by inject()
    private val scheduler: ReminderScheduler by inject()

    override suspend fun doWork(): Result {
        val reminders = repository.observeReminders().first()
        reminders.forEach(scheduler::schedule)
        return Result.success()
    }
}
