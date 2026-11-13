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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cardmanager.R
import com.example.cardmanager.core.database.entity.CardNetwork
import com.example.cardmanager.core.database.entity.CardStylePreset
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.detectCardNetwork

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (card: CreditCardEntity, syncCalendar: Boolean) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var cardAlias by remember { mutableStateOf("") }
    var cardNumberInput by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var billDay by remember { mutableStateOf("") }
    var repaymentDay by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(CardStylePreset.DEEP_OCEAN) }
    var selectedNetwork by remember { mutableStateOf(CardNetwork.UNIONPAY) }
    var hasManuallySelectedNetwork by remember { mutableStateOf(false) }
    var hasAnnualFee by remember { mutableStateOf(false) }
    var annualFeeAmount by remember { mutableStateOf("") }
    var annualFeeWaiverCount by remember { mutableStateOf("") }
    var syncCalendar by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val extractedLast4 = cardNumberInput.filter { it.isDigit() }.let { digits ->
        if (digits.length >= 4) digits.takeLast(4) else digits
    }

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
                CardPreviewBanner(
                    bankName = bankName,
                    cardAlias = cardAlias,
                    last4Digits = extractedLast4,
                    creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                    repaymentDay = repaymentDay.toIntOrNull()?.coerceIn(1, 31) ?: 15,
                    stylePreset = selectedStyle,
                    network = selectedNetwork
                )

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

                OutlinedTextField(
                    value = cardNumberInput,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(19)
                        cardNumberInput = digits
                        if (!hasManuallySelectedNetwork && digits.isNotEmpty()) {
                            val detected = detectCardNetwork(digits)
                            if (detected != CardNetwork.OTHER) {
                                selectedNetwork = detected
                            }
                        }
                    },
                    label = { Text("卡号或后4位 (输入完整卡号自动识别卡组织)") },
                    placeholder = { Text("例如 6225... 或直接输入后4位") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("信用额度 (元)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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

                Text(stringResource(R.string.card_network_selector_title), style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CardNetwork.entries) { network ->
                        FilterChip(
                            selected = selectedNetwork == network,
                            onClick = {
                                selectedNetwork = network
                                hasManuallySelectedNetwork = true
                            },
                            label = { Text(network.localizedName()) }
                        )
                    }
                }

                Text(stringResource(R.string.card_style_selector_title), style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CardStylePreset.entries) { preset ->
                        val isSelected = preset == selectedStyle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedStyle = preset }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(preset.gradientColors))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = preset.primaryTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = preset.localizedName(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.annual_fee_toggle_title), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (hasAnnualFee) stringResource(R.string.annual_fee_enabled_desc) else stringResource(R.string.annual_fee_free_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = hasAnnualFee, onCheckedChange = { hasAnnualFee = it })
                }

                if (hasAnnualFee) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = annualFeeAmount,
                            onValueChange = { annualFeeAmount = it },
                            label = { Text(stringResource(R.string.annual_fee_amount_label)) },
                            placeholder = { Text("300") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = annualFeeWaiverCount,
                            onValueChange = { annualFeeWaiverCount = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.annual_fee_waiver_count_label)) },
                            placeholder = { Text(stringResource(R.string.annual_fee_waiver_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
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
                    val feeAmt = if (hasAnnualFee) (annualFeeAmount.toDoubleOrNull() ?: 0.0) else 0.0
                    val waiverCount = if (hasAnnualFee) (annualFeeWaiverCount.toIntOrNull() ?: 0) else 0

                    when {
                        bankName.isBlank() || cardAlias.isBlank() -> errorText = "请填写银行名称与卡片备注"
                        extractedLast4.length != 4 -> errorText = "请输入完整卡号或至少4位卡号尾数"
                        limitVal == null || limitVal <= 0.0 -> errorText = "额度必须为大于0的数字"
                        bDay == null || bDay !in 1..31 -> errorText = "账单日必须在1至31之间"
                        rDay == null || rDay !in 1..31 -> errorText = "还款日必须在1至31之间"
                        hasAnnualFee && feeAmt < 0.0 -> errorText = "年费金额不能为负数"
                        hasAnnualFee && waiverCount < 0 -> errorText = "刷卡免年费笔数不能为负数"
                        else -> {
                            onConfirm(
                                CreditCardEntity(
                                    bankName = bankName.trim(),
                                    cardAlias = cardAlias.trim(),
                                    last4Digits = extractedLast4,
                                    creditLimit = limitVal,
                                    billDay = bDay,
                                    repaymentDay = rDay,
                                    cardColorHex = selectedStyle.primaryHex,
                                    cardNetwork = selectedNetwork,
                                    cardStyle = selectedStyle.name,
                                    hasAnnualFee = hasAnnualFee,
                                    annualFeeAmount = feeAmt,
                                    annualFeeWaiverCount = waiverCount
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
