package com.swagatmali.remider.di

import org.koin.core.module.Module

/**
 * Target-specific Koin bindings. Each source set supplies the platform bridges:
 * for now the [com.swagatmali.remider.data.local.DatabaseDriverFactory];
 * auth and notification schedulers join here in later phases.
 */
expect fun platformModule(): Module
