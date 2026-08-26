package com.swagatmali.remider.di

import com.swagatmali.remider.data.local.DatabaseDriverFactory
import com.swagatmali.remider.data.local.ReminderRepositoryImpl
import com.swagatmali.remider.db.ReminderDatabase
import com.swagatmali.remider.domain.repository.ReminderRepository
import org.koin.dsl.module

/**
 * Data layer graph: the driver (from platformModule) → the database → the
 * repository. Both are singletons — one DB connection for the app's lifetime.
 */
val dataModule = module {
    single { ReminderDatabase(get<DatabaseDriverFactory>().create()) }
    single<ReminderRepository> { ReminderRepositoryImpl(get()) }
}
