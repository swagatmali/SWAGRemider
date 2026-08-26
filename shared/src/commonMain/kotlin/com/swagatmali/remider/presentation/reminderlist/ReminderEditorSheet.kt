package com.swagatmali.remider.presentation.reminderlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swagatmali.remider.presentation.util.formatTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Modal bottom sheet hosting the create/edit form. All form state lives in
 * [EditorState] (owned by the ViewModel); this composable is stateless except
 * for the transient "is a picker dialog open" flags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorSheet(
    editor: EditorState,
    onIntent: (ReminderListIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onIntent(ReminderListIntent.EditorDismissed) },
        sheetState = sheetState,
    ) {
        EditorContent(editor = editor, onIntent = onIntent)
    }
}

@Composable
private fun EditorContent(
    editor: EditorState,
    onIntent: (ReminderListIntent) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (editor.isEditing) "Edit reminder" else "New reminder",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = editor.title,
            onValueChange = { onIntent(ReminderListIntent.EditorTitleChanged(it)) },
            label = { Text("Title") },
            singleLine = true,
            isError = editor.titleError,
            supportingText = { if (editor.titleError) Text("Title can't be empty") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = editor.notes,
            onValueChange = { onIntent(ReminderListIntent.EditorNotesChanged(it)) },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                Text(editor.date.toString())
            }
            OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                Text(formatTime(editor.time))
            }
        }

        Button(
            onClick = { onIntent(ReminderListIntent.EditorSaveClicked) },
            enabled = !editor.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (editor.isSaving) "Saving…" else "Save")
        }
    }

    if (showDatePicker) {
        DueDatePickerDialog(
            initialDate = editor.date,
            onDateSelected = { onIntent(ReminderListIntent.EditorDateChanged(it)) },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        DueTimePickerDialog(
            initialTime = editor.time,
            onTimeSelected = { onIntent(ReminderListIntent.EditorTimeChanged(it)) },
            onDismiss = { showTimePicker = false },
        )
    }
}

/**
 * Material3 date picker. The picker stores the selection as UTC-midnight epoch
 * millis, so we convert to/from [LocalDate] through [TimeZone.UTC] to keep the
 * calendar date stable regardless of the device's actual zone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.fromEpochMilliseconds(millis)
                        .toLocalDateTime(TimeZone.UTC)
                        .date
                    onDateSelected(date)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

/** Material3 time picker, wrapped in an AlertDialog (there is no TimePickerDialog). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = timePickerState) },
    )
}
