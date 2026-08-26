package com.swagatmali.remider.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Single shared entry point for the Koin graph. Android passes
 * `androidContext()` / `androidLogger()` through [appDeclaration]; iOS uses
 * [doInitKoin]. Called once at app startup (wired in Phase 4).
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(platformModule(), dataModule, domainModule, presentationModule)
    }

/** Objective-C/Swift-friendly launcher (no default arguments across the bridge). */
fun doInitKoin() {
    initKoin()
}
