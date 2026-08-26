package com.swagatmali.remider.di

import com.swagatmali.remider.presentation.reminderlist.ReminderListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Presentation layer: multiplatform ViewModels. `viewModel { }` (koin-core MP
 * DSL) scopes each instance to the platform ViewModelStoreOwner and resolves the
 * six use cases positionally; the ViewModel's Clock uses its default.
 */
val presentationModule = module {
    viewModel {
        ReminderListViewModel(get(), get(), get(), get(), get(), get())
    }
}
