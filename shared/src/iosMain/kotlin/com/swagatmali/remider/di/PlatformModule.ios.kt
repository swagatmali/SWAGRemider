package com.swagatmali.remider.di

import com.swagatmali.remider.data.local.DatabaseDriverFactory
import com.swagatmali.remider.data.local.IosDatabaseDriverFactory
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import com.swagatmali.remider.notification.IosReminderScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
    single<ReminderScheduler> { IosReminderScheduler() }
}
