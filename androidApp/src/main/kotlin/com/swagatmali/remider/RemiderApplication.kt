package com.swagatmali.remider

import android.app.Application
import com.swagatmali.remider.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Android entry point. Starts the shared Koin graph once, injecting the
 * application Context (needed by AndroidDatabaseDriverFactory and
 * AndroidReminderScheduler) and Android logging. Registered as
 * `android:name` in the manifest.
 */
class RemiderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@RemiderApplication)
        }
    }
}
