package com.example.cardmanager.feature.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cardmanager.core.database.entity.CreditCardEntity

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (card: CreditCardEntity, syncCalendar: Boolean) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var cardAlias by remember { mutableStateOf("") }
    var last4Digits by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var billDay by remember { mutableStateOf("") }
    var repaymentDay by remember { mutableStateOf("") }
    var selectedCardColor by remember { mutableStateOf("#1E3A8A") }
    var syncCalendar by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val colorPalette = listOf("#1E3A8A", "#0F172A", "#B45309", "#0284C7", "#059669", "#7C3AED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Credit Card") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name (e.g. Chase)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cardAlias,
                    onValueChange = { cardAlias = it },
                    label = { Text("Card Alias (e.g. Sapphire)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Text("Card Theme", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorPalette.forEach { hex ->
                        val isSelected = hex == selectedCardColor
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { selectedCardColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sync to Calendar", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = syncCalendar, onCheckedChange = { syncCalendar = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitVal = creditLimit.toDoubleOrNull()
                    val bDay = billDay.toIntOrNull()
                    val rDay = repaymentDay.toIntOrNull()

                    when {
                        bankName.isBlank() || cardAlias.isBlank() -> errorText = "Please fill in all name fields"
                        last4Digits.length != 4 -> errorText = "Last 4 digits must be exactly 4 digits"
                        limitVal == null || limitVal <= 0.0 -> errorText = "Limit must be a positive number"
                        bDay == null || bDay !in 1..31 -> errorText = "Bill day must be 1..31"
                        rDay == null || rDay !in 1..31 -> errorText = "Repay day must be 1..31"
                        else -> {
                            onConfirm(
                                CreditCardEntity(
                                    bankName = bankName.trim(),
                                    cardAlias = cardAlias.trim(),
                                    last4Digits = last4Digits,
                                    creditLimit = limitVal,
                                    billDay = bDay,
                                    repaymentDay = rDay,
                                    cardColorHex = selectedCardColor
                                ),
                                syncCalendar
                            )
                        }
                    }
                }
            ) {
                Text("Save Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
