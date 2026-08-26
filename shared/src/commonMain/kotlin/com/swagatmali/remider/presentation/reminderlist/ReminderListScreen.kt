package com.swagatmali.remider.presentation.reminderlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.presentation.util.formatDueDateTime
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.viewmodel.koinViewModel

/**
 * The reminder list screen. Renders one immutable [ReminderListState] snapshot,
 * forwards user actions as [ReminderListIntent]s, and surfaces one-shot
 * [ReminderListEffect]s (errors) as snackbars. The create/edit form is shown as
 * a modal bottom sheet whenever [ReminderListState.editor] is present.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is ReminderListEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                }
            }
            .launchIn(this)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reminders") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onIntent(ReminderListIntent.AddClicked) }) {
                Text("+", fontSize = 28.sp)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.reminders.isEmpty() -> Text(
                    text = "No reminders yet.\nTap + to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.reminders, key = { it.id.value }) { reminder ->
                        ReminderRow(
                            reminder = reminder,
                            onToggleComplete = { checked ->
                                viewModel.onIntent(ReminderListIntent.CompletionToggled(reminder.id, checked))
                            },
                            onClick = { viewModel.onIntent(ReminderListIntent.EditClicked(reminder.id)) },
                            onDelete = { viewModel.onIntent(ReminderListIntent.DeleteClicked(reminder.id)) },
                        )
                    }
                }
            }
        }
    }

    state.editor?.let { editor ->
        ReminderEditorSheet(editor = editor, onIntent = viewModel::onIntent)
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    onToggleComplete: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = reminder.isCompleted, onCheckedChange = onToggleComplete)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                )
                Text(
                    text = formatDueDateTime(reminder.dueDateTime),
                    style = MaterialTheme.typography.bodySmall,
                )
                val notes = reminder.notes
                if (!notes.isNullOrBlank()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}
