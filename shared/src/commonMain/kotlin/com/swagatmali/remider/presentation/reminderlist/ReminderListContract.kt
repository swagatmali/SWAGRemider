package com.swagatmali.remider.presentation.reminderlist

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * MVI contract for the reminder list screen.
 *
 * - [ReminderListState] is the single immutable snapshot the UI renders.
 * - [ReminderListIntent] are the user actions the UI sends back via `onIntent`.
 * - [ReminderListEffect] are one-shot events (snackbars) consumed exactly once.
 *
 * The create/edit form is modelled as [EditorState]; its presence in
 * [ReminderListState.editor] drives the modal bottom sheet's visibility.
 */
data class ReminderListState(
    val isLoading: Boolean = true,
    val reminders: List<Reminder> = emptyList(),
    val editor: EditorState? = null,
)

/**
 * In-progress state of the create/edit form. [editingId] null = creating a new
 * reminder; non-null = editing that existing one. Date and time are kept split
 * so the UI can bind separate date/time pickers.
 */
data class EditorState(
    val editingId: ReminderId? = null,
    val title: String = "",
    val notes: String = "",
    val date: LocalDate,
    val time: LocalTime,
    val isSaving: Boolean = false,
    val titleError: Boolean = false,
) {
    val isEditing: Boolean get() = editingId != null
}

sealed interface ReminderListIntent {
    // List actions
    data object AddClicked : ReminderListIntent
    data class EditClicked(val id: ReminderId) : ReminderListIntent
    data class CompletionToggled(val id: ReminderId, val completed: Boolean) : ReminderListIntent
    data class DeleteClicked(val id: ReminderId) : ReminderListIntent

    // Editor (bottom sheet) actions
    data object EditorDismissed : ReminderListIntent
    data class EditorTitleChanged(val title: String) : ReminderListIntent
    data class EditorNotesChanged(val notes: String) : ReminderListIntent
    data class EditorDateChanged(val date: LocalDate) : ReminderListIntent
    data class EditorTimeChanged(val time: LocalTime) : ReminderListIntent
    data object EditorSaveClicked : ReminderListIntent
}

sealed interface ReminderListEffect {
    data class ShowMessage(val message: String) : ReminderListEffect
}
