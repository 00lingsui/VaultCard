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
        title = { Text("添加信用卡") },
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
                    label = { Text("发卡银行 (如: 招商银行)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cardAlias,
                    onValueChange = { cardAlias = it },
                    label = { Text("卡片名称 (如: 经典白金卡)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = last4Digits,
                        onValueChange = { if (it.length <= 4) last4Digits = it.filter { ch -> ch.isDigit() } },
                        label = { Text("卡号后4位") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it },
                        label = { Text("信用额度 (元)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = billDay,
                        onValueChange = { billDay = it.filter { ch -> ch.isDigit() } },
                        label = { Text("账单日 (1-31日)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repaymentDay,
                        onValueChange = { repaymentDay = it.filter { ch -> ch.isDigit() } },
                        label = { Text("还款日 (1-31日)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("卡面配色", style = MaterialTheme.typography.labelMedium)
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
                    Text("同步还款日程至系统日历", style = MaterialTheme.typography.bodySmall)
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
                        bankName.isBlank() || cardAlias.isBlank() -> errorText = "请填写银行名称与卡片备注"
                        last4Digits.length != 4 -> errorText = "卡号后4位必须为4位数字"
                        limitVal == null || limitVal <= 0.0 -> errorText = "额度必须为大于0的数字"
                        bDay == null || bDay !in 1..31 -> errorText = "账单日必须在1至31之间"
                        rDay == null || rDay !in 1..31 -> errorText = "还款日必须在1至31之间"
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
                Text("保存卡片")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
