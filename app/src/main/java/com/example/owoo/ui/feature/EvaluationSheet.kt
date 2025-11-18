package com.example.owoo.ui.feature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.owoo.util.EvaluationConstants
import com.example.owoo.util.EvaluationField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@Composable
fun EvaluationSheetContent(
    currentPage: Int,
    evaluationForm: Map<String, String>,
    onFormChange: (String, String) -> Unit,
    rejectionMessages: List<String>,
    rejectionReasonString: String,
    onReasonChange: (String) -> Unit
) {
    val filteredFields = remember(currentPage) {
        val pageFields = when (currentPage) {
            0 -> EvaluationConstants.evaluationFields.filter {
                it.label == "FOTO PAPAN NAMA" || it.label == "GEO TAGGING"
            }
            1 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO BOX & PIC" }
            2 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO KELENGKAPAN UNIT" }
            3 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO PROSES INSTALASI" }
            4 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO SERIAL NUMBER" }
            5 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO TRAINING" }
            6 -> EvaluationConstants.evaluationFields.filter {
                it.label == "CEKLIS BAPP HAL 1" || it.label == "BARCODE BAPP"
            }
            7 -> EvaluationConstants.evaluationFields.filter {
                it.label == "CEKLIS BAPP HAL 2" ||
                        it.label == "NAMA PENANDATANGANAN BAPP" ||
                        it.label == "STEMPEL" ||
                        it.label == "KESIMPULAN LENGKAP" ||
                        it.label == "PESERTA PELATIHAN"
            }
            else -> emptyList()
        }
        val tanggalField = EvaluationConstants.evaluationFields.firstOrNull { it.col == "X" }
        if (tanggalField != null) pageFields + tanggalField else pageFields
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Form Evaluasi", style = MaterialTheme.typography.headlineMedium)
            Divider(modifier = Modifier.padding(top = 16.dp))
        }

        items(filteredFields) { field ->
            EvaluationFieldItem(
                field = field,
                selectedValue = evaluationForm[field.col],
                onOptionSelected = { onFormChange(field.col, it) }
            )
        }

        if (rejectionMessages.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Alasan Penolakan (bisa diedit):", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonString,
                        onValueChange = onReasonChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        label = { Text("Alasan Penolakan") }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvaluationFieldItem(
    field: EvaluationField,
    selectedValue: String?,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(field.label, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        if (field.col == "X") {
            DateInputField(
                value = selectedValue ?: "",
                onValueChange = onOptionSelected
            )
        } else if (field.options.isEmpty()) {
            OutlinedTextField(
                value = selectedValue ?: "",
                onValueChange = { onOptionSelected(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(field.label) }
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                field.options.forEach { option ->
                    RadioChip(
                        text = option,
                        selected = selectedValue == option,
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun RadioChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    OutlinedButton(
        onClick = onClick,
        colors = colors,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.small
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val open = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        label = { Text("Tanggal Instalasi Selesai") },
        trailingIcon = {
            IconButton(onClick = { open.value = true }) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }
    )

    if (open.value) {
        DatePickerDialog(
            onDismissRequest = { open.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val formatted = java.text.SimpleDateFormat("dd/MM/yyyy")
                            .format(java.util.Date(millis))
                        onValueChange(formatted)
                    }
                    open.value = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
