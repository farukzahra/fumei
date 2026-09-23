package fumei.faruk.dev.br.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPuffDialog(
    timestampMillis: Long,
    grams: Double,
    onDismiss: () -> Unit,
    onConfirm: (timestampMillis: Long, grams: Double) -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val locale = Locale.forLanguageTag("pt-BR")
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", locale) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", locale) }

    val initial = remember(timestampMillis) {
        Instant.ofEpochMilli(timestampMillis).atZone(zone)
    }
    var selectedDate by remember(timestampMillis) { mutableStateOf(initial.toLocalDate()) }
    var selectedTime by remember(timestampMillis) {
        mutableStateOf(initial.toLocalTime().withSecond(0).withNano(0))
    }
    var gramsText by remember(timestampMillis, grams) {
        mutableStateOf(ConsumptionFormat.formatGrams(grams))
    }
    var gramsError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar registro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Data: ${selectedDate.format(dateFormatter)}")
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Hora: ${selectedTime.format(timeFormatter)}")
                }
                OutlinedTextField(
                    value = gramsText,
                    onValueChange = {
                        gramsText = it
                        gramsError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_grams_field"),
                    label = { Text("Gramas nesta sessão") },
                    suffix = { Text("g") },
                    isError = gramsError,
                    supportingText = {
                        if (gramsError) {
                            Text("Informe um valor válido")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedGrams = ConsumptionFormat.parseGramsInput(gramsText)
                    if (parsedGrams == null) {
                        gramsError = true
                        return@TextButton
                    }
                    val newMillis = LocalDateTime.of(selectedDate, selectedTime)
                        .atZone(zone)
                        .toInstant()
                        .toEpochMilli()
                    onConfirm(newMillis, parsedGrams)
                },
                modifier = Modifier.testTag("confirm_edit_button"),
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(zone)
                                .toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Selecionar hora") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
fun DeletePuffDialog(
    label: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir registro?") },
        text = { Text("O registro de $label será removido permanentemente.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button"),
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
