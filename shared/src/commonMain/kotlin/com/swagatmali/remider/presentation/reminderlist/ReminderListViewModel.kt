package com.swagatmali.remider.presentation.reminderlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.usecase.CreateReminderUseCase
import com.swagatmali.remider.domain.usecase.DeleteReminderUseCase
import com.swagatmali.remider.domain.usecase.GetReminderByIdUseCase
import com.swagatmali.remider.domain.usecase.GetRemindersUseCase
import com.swagatmali.remider.domain.usecase.SetReminderCompletedUseCase
import com.swagatmali.remider.domain.usecase.UpdateReminderUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Shared MVI ViewModel driving the reminder list + create/edit sheet. Lives in
 * commonMain (JetBrains multiplatform lifecycle), so Android and iOS bind the
 * same instance via Koin's `koinViewModel()`.
 *
 * State flows out through [state]; the UI pushes actions in through [onIntent];
 * one-shot messages come out through [effects]. All persistence is delegated to
 * use cases — the ViewModel holds no platform or data-layer knowledge.
 */
class ReminderListViewModel(
    private val getReminders: GetRemindersUseCase,
    private val getReminderById: GetReminderByIdUseCase,
    private val createReminder: CreateReminderUseCase,
    private val updateReminder: UpdateReminderUseCase,
    private val setCompleted: SetReminderCompletedUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val clock: Clock = Clock.System,
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderListState())
    val state: StateFlow<ReminderListState> = _state.asStateFlow()

    private val _effects = Channel<ReminderListEffect>(Channel.BUFFERED)
    val effects: Flow<ReminderListEffect> = _effects.receiveAsFlow()

    init {
        getReminders()
            .onEach { list -> _state.update { it.copy(isLoading = false, reminders = list) } }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: ReminderListIntent) {
        when (intent) {
            ReminderListIntent.AddClicked -> openEditorForNew()
            is ReminderListIntent.EditClicked -> openEditorForEdit(intent.id)
            is ReminderListIntent.CompletionToggled ->
                viewModelScope.launch { setCompleted(intent.id, intent.completed) }
            is ReminderListIntent.DeleteClicked ->
                viewModelScope.launch { deleteReminder(intent.id) }

            ReminderListIntent.EditorDismissed -> _state.update { it.copy(editor = null) }
            is ReminderListIntent.EditorTitleChanged ->
                updateEditor { it.copy(title = intent.title, titleError = false) }
            is ReminderListIntent.EditorNotesChanged ->
                updateEditor { it.copy(notes = intent.notes) }
            is ReminderListIntent.EditorDateChanged ->
                updateEditor { it.copy(date = intent.date) }
            is ReminderListIntent.EditorTimeChanged ->
                updateEditor { it.copy(time = intent.time) }
            ReminderListIntent.EditorSaveClicked -> save()
        }
    }

    private fun openEditorForNew() {
        val now = nowLocal()
        _state.update { it.copy(editor = EditorState(date = now.date, time = now.time)) }
    }

    private fun openEditorForEdit(id: ReminderId) {
        viewModelScope.launch {
            val reminder = getReminderById(id)
            if (reminder == null) {
                _effects.send(ReminderListEffect.ShowMessage("Reminder not found"))
                return@launch
            }
            _state.update {
                it.copy(
                    editor = EditorState(
                        editingId = reminder.id,
                        title = reminder.title,
                        notes = reminder.notes.orEmpty(),
                        date = reminder.dueDateTime.date,
                        time = reminder.dueDateTime.time,
                    ),
                )
            }
        }
    }

    private fun save() {
        val editor = _state.value.editor ?: return
        if (editor.title.isBlank()) {
            updateEditor { it.copy(titleError = true) }
            return
        }
        val timeZone = TimeZone.currentSystemDefault()
        val dueDateTime = LocalDateTime(editor.date, editor.time)
        val cleanNotes = editor.notes.ifBlank { null }

        viewModelScope.launch {
            updateEditor { it.copy(isSaving = true) }
            val result: Result<Unit> = if (editor.editingId == null) {
                createReminder(editor.title, cleanNotes, dueDateTime, timeZone).map { }
            } else {
                val original = getReminderById(editor.editingId)
                if (original == null) {
                    Result.failure(IllegalStateException("Reminder no longer exists"))
                } else {
                    updateReminder(
                        original.copy(
                            title = editor.title,
                            notes = cleanNotes,
                            dueDateTime = dueDateTime,
                            timeZone = timeZone,
                        ),
                    )
                }
            }
            result
                .onSuccess { _state.update { it.copy(editor = null) } }
                .onFailure { error ->
                    updateEditor { it.copy(isSaving = false) }
                    _effects.send(
                        ReminderListEffect.ShowMessage(error.message ?: "Could not save reminder"),
                    )
                }
        }
    }

    private inline fun updateEditor(transform: (EditorState) -> EditorState) {
        _state.update { state -> state.editor?.let { state.copy(editor = transform(it)) } ?: state }
    }

    private fun nowLocal(): LocalDateTime =
        clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
