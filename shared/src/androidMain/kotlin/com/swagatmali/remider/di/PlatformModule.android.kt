package com.swagatmali.remider.di

import com.swagatmali.remider.data.local.AndroidDatabaseDriverFactory
import com.swagatmali.remider.data.local.DatabaseDriverFactory
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import com.swagatmali.remider.notification.AndroidReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
    single<ReminderScheduler> { AndroidReminderScheduler(androidContext()) }
}
