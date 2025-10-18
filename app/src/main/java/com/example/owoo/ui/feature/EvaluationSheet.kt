package com.example.owoo.ui.feature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.owoo.util.EvaluationConstants
import com.example.owoo.util.EvaluationField

@Composable
fun EvaluationSheetContent(
    currentPage: Int,
    evaluationForm: Map<String, String>,
    onFormChange: (String, String) -> Unit,
    rejectionMessages: List<String>
) {
    // Filter fields based on the current image page, similar to the React code
    val filteredFields = remember(currentPage) {
        when (currentPage) {
            0 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO PAPAN NAMA" || it.label == "GEO TAGGING" }
            1 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO BOX & PIC" }
            2 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO KELENGKAPAN UNIT" }
            3 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO PROSES INSTALASI" } // Assuming index 3 is this
            4 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO SERIAL NUMBER" }
            5 -> EvaluationConstants.evaluationFields.filter { it.label == "FOTO TRAINING" } // Assuming index 5 is this
            6 -> EvaluationConstants.evaluationFields.filter { it.label == "CEKLIS BAPP HAL 1" || it.label == "BARCODE BAPP" }
            7 -> EvaluationConstants.evaluationFields.filter {
                it.label == "CEKLIS BAPP HAL 2" ||
                it.label == "NAMA PENANDATANGANAN BAPP" ||
                it.label == "STEMPEL" ||
                it.label == "KESIMPULAN LENGKAP" ||
                it.label == "PESERTA PELATIHAN"
            }
            else -> emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Form Evaluasi", style = MaterialTheme.typography.headlineMedium)
            Divider(modifier = Modifier.padding(top = 16.dp))
        }

        if (filteredFields.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada form evaluasi untuk gambar ini.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredFields) { field ->
                EvaluationFieldItem(
                    field = field,
                    selectedValue = evaluationForm[field.col],
                    onOptionSelected = { onFormChange(field.col, it) }
                )
            }
        }

        // Display Rejection Messages
        if (rejectionMessages.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Alasan Penolakan:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    rejectionMessages.forEach { message ->
                        Text(
                            text = "• $message",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            // Add space at the bottom for better scrolling
            Spacer(modifier = Modifier.height(32.dp))
        }
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