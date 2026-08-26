package com.swagatmali.remider

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.swagatmali.remider.presentation.reminderlist.ReminderListScreen
import org.koin.compose.KoinContext

/**
 * Root composable shared by Android and iOS. Each platform host starts Koin
 * (Android: Application; iOS: doInitKoin) before this renders; [KoinContext]
 * bridges that already-started global Koin into the composition so
 * `koinViewModel()` can resolve the shared ViewModel.
 */
@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            ReminderListScreen()
        }
    }
}
