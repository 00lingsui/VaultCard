package com.example.cardmanager.feature.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cardmanager.core.database.entity.CreditCardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailBottomSheet(
    card: CreditCardEntity,
    onDismiss: () -> Unit,
    onUpdate: (CreditCardEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onSetAlarm: () -> Unit
) {
    var bankName by remember(card) { mutableStateOf(card.bankName) }
    var cardAlias by remember(card) { mutableStateOf(card.cardAlias) }
    var last4Digits by remember(card) { mutableStateOf(card.last4Digits) }
    var creditLimit by remember(card) { mutableStateOf(card.creditLimit.toString()) }
    var billDay by remember(card) { mutableStateOf(card.billDay.toString()) }
    var repaymentDay by remember(card) { mutableStateOf(card.repaymentDay.toString()) }
    var selectedCardColor by remember(card) { mutableStateOf(card.cardColorHex) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val colorPalette = listOf("#1E3A8A", "#0F172A", "#B45309", "#0284C7", "#059669", "#7C3AED")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Card Details & Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cardAlias,
                onValueChange = { cardAlias = it },
                label = { Text("Card Alias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = last4Digits,
                    onValueChange = { if (it.length <= 4) last4Digits = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Last 4") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("Limit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1.5f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = billDay,
                    onValueChange = { billDay = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Bill Day (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = repaymentDay,
                    onValueChange = { repaymentDay = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Repay Day (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Card Theme Color", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorPalette.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .clickable { selectedCardColor = hex }
                            .then(
                                if (hex == selectedCardColor) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier
                            )
                    )
                }
            }

            OutlinedButton(
                onClick = onSetAlarm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Set Due Date Alarm in Clock App")
            }

            Button(
                onClick = {
                    val limit = creditLimit.toDoubleOrNull() ?: card.creditLimit
                    val bDay = billDay.toIntOrNull()?.coerceIn(1, 31) ?: card.billDay
                    val rDay = repaymentDay.toIntOrNull()?.coerceIn(1, 31) ?: card.repaymentDay

                    onUpdate(
                        card.copy(
                            bankName = bankName.trim(),
                            cardAlias = cardAlias.trim(),
                            last4Digits = last4Digits.takeIf { it.length == 4 } ?: card.last4Digits,
                            creditLimit = limit,
                            billDay = bDay,
                            repaymentDay = rDay,
                            cardColorHex = selectedCardColor
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Text("Save Changes")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Card?") },
            text = { Text("Are you sure you want to remove '${card.cardAlias}'? This will also remove associated transactions.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(card.id)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
