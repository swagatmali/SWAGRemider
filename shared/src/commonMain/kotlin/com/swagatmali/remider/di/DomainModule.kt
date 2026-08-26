package com.swagatmali.remider.di

import com.swagatmali.remider.domain.usecase.CreateReminderUseCase
import com.swagatmali.remider.domain.usecase.DeleteReminderUseCase
import com.swagatmali.remider.domain.usecase.GetReminderByIdUseCase
import com.swagatmali.remider.domain.usecase.GetRemindersUseCase
import com.swagatmali.remider.domain.usecase.SetReminderCompletedUseCase
import com.swagatmali.remider.domain.usecase.UpdateReminderUseCase
import org.koin.dsl.module

/**
 * Use cases are stateless — created fresh on each request via `factory`. The
 * four mutating use cases also receive the ReminderScheduler (bound per platform
 * in platformModule): first `get()` = repository, second `get()` = scheduler.
 */
val domainModule = module {
    factory { GetRemindersUseCase(get()) }
    factory { GetReminderByIdUseCase(get()) }
    factory { CreateReminderUseCase(get(), get()) }
    factory { UpdateReminderUseCase(get(), get()) }
    factory { SetReminderCompletedUseCase(get(), get()) }
    factory { DeleteReminderUseCase(get(), get()) }
}
