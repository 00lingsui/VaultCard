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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cardmanager.R
import com.example.cardmanager.core.currency.AppCurrency
import com.example.cardmanager.core.database.entity.CardNetwork
import com.example.cardmanager.core.database.entity.CardStylePreset
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.detectCardNetwork
import com.example.cardmanager.core.feedback.FeedbackManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailBottomSheet(
    card: CreditCardEntity,
    recordedSwipesInCurrentYear: Int = 0,
    onDismiss: () -> Unit,
    onUpdate: (CreditCardEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onSetAlarm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CardDetailContent(
            card = card,
            recordedSwipesInCurrentYear = recordedSwipesInCurrentYear,
            onUpdate = onUpdate,
            onDelete = onDelete,
            onSetAlarm = onSetAlarm,
            onClose = onDismiss,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
fun CardDetailContent(
    card: CreditCardEntity,
    recordedSwipesInCurrentYear: Int = 0,
    onUpdate: (CreditCardEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onSetAlarm: () -> Unit,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val view = LocalView.current
    var bankName by remember(card) { mutableStateOf(card.bankName) }
    var cardAlias by remember(card) { mutableStateOf(card.cardAlias) }
    var cardNumberInput by remember(card) { mutableStateOf(card.last4Digits) }
    var creditLimit by remember(card) { mutableStateOf(card.creditLimit.toString()) }
    var billDay by remember(card) { mutableStateOf(card.billDay.toString()) }
    var repaymentDay by remember(card) { mutableStateOf(card.repaymentDay.toString()) }
    var selectedStyle by remember(card) {
        mutableStateOf(CardStylePreset.resolve(card.cardStyle, card.cardColorHex))
    }
    var selectedNetwork by remember(card) { mutableStateOf(card.cardNetwork) }
    var selectedCurrency by remember(card) { mutableStateOf(AppCurrency.fromCode(card.currencyCode)) }
    var hasManuallySelectedNetwork by remember(card) {
        mutableStateOf(card.cardNetwork != CardNetwork.OTHER)
    }
    var hasAnnualFee by remember(card) { mutableStateOf(card.hasAnnualFee) }
    var annualFeeAmount by remember(card) { mutableStateOf(if (card.annualFeeAmount > 0) card.annualFeeAmount.toString() else "") }
    var annualFeeWaiverCount by remember(card) { mutableStateOf(if (card.annualFeeWaiverCount > 0) card.annualFeeWaiverCount.toString() else "") }
    var initialWaiverSwipes by remember(card) { mutableStateOf(if (card.initialWaiverSwipes > 0) card.initialWaiverSwipes.toString() else "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val extractedLast4 = cardNumberInput.filter { it.isDigit() }.let { digits ->
        if (digits.length >= 4) digits.takeLast(4) else card.last4Digits
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("卡片详情与管理", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = {
                    FeedbackManager.performClick(view)
                    showDeleteConfirm = true
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
                if (onClose != null) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            }
        }

        CardPreviewBanner(
            bankName = bankName,
            cardAlias = cardAlias,
            last4Digits = extractedLast4,
            creditLimit = creditLimit.toDoubleOrNull() ?: card.creditLimit,
            repaymentDay = repaymentDay.toIntOrNull()?.coerceIn(1, 31) ?: card.repaymentDay,
            stylePreset = selectedStyle,
            network = selectedNetwork,
            currency = selectedCurrency
        )

        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("发卡银行") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cardAlias,
            onValueChange = { cardAlias = it },
            label = { Text("卡片名称/备注") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = cardNumberInput,
                onValueChange = { raw ->
                    val digits = raw.filter { it.isDigit() }.take(19)
                    cardNumberInput = digits
                    if (!hasManuallySelectedNetwork && digits.length > 4) {
                        val detected = detectCardNetwork(digits)
                        if (detected != CardNetwork.OTHER) {
                            selectedNetwork = detected
                        }
                    }
                },
                label = { Text("卡号/后4位") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1.1f)
            )
            OutlinedTextField(
                value = creditLimit,
                onValueChange = { creditLimit = it },
                label = { Text("信用额度 (${selectedCurrency.code} ${selectedCurrency.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1.4f)
            )
        }

        Text(stringResource(R.string.card_currency_selector_title), style = MaterialTheme.typography.labelMedium)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppCurrency.entries) { currency ->
                FilterChip(
                    selected = selectedCurrency == currency,
                    onClick = {
                        FeedbackManager.performClick(view)
                        selectedCurrency = currency
                    },
                    label = { Text("${currency.code} (${currency.symbol})") }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        FeedbackManager.performClick(view)
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
                        .clickable {
                            FeedbackManager.performClick(view)
                            selectedStyle = preset
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
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

        val previewFeeAmt = if (hasAnnualFee) (annualFeeAmount.toDoubleOrNull() ?: card.annualFeeAmount) else 0.0
        val previewWaiver = if (hasAnnualFee) (annualFeeWaiverCount.toIntOrNull() ?: card.annualFeeWaiverCount) else 0
        val previewInitialSwipes = if (hasAnnualFee && previewWaiver > 0) {
            (initialWaiverSwipes.toIntOrNull() ?: 0).coerceAtLeast(0)
        } else {
            0
        }
        val feeAmtDisplay = selectedCurrency.format(previewFeeAmt, trimWholeDecimals = true)
        val annualFeeSummary = when {
            !hasAnnualFee -> stringResource(R.string.annual_fee_free_summary)
            previewWaiver > 0 -> stringResource(R.string.annual_fee_waivable_summary, feeAmtDisplay, previewWaiver)
            else -> stringResource(R.string.annual_fee_rigid_summary, feeAmtDisplay)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.annual_fee_policy_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = annualFeeSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasAnnualFee) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (hasAnnualFee && previewWaiver > 0) {
            val completedSwipes = previewInitialSwipes + recordedSwipesInCurrentYear
            val remainingSwipes = maxOf(0, previewWaiver - completedSwipes)
            val progress = (completedSwipes.toFloat() / previewWaiver.toFloat()).coerceIn(0f, 1f)
            val isWaived = remainingSwipes == 0

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (isWaived) {
                    Color(0xFF059669).copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.waiver_progress_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isWaived) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface
                        )
                        if (isWaived) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF059669)
                            ) {
                                Text(
                                    text = stringResource(R.string.waiver_badge_completed),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isWaived) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                        trackColor = if (isWaived) Color(0xFF059669).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        text = if (isWaived) {
                            stringResource(R.string.waiver_progress_completed, completedSwipes)
                        } else {
                            stringResource(
                                R.string.waiver_progress_remaining,
                                completedSwipes,
                                previewWaiver,
                                remainingSwipes,
                                feeAmtDisplay
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWaived) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.annual_fee_section_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (hasAnnualFee) stringResource(R.string.annual_fee_enabled_desc) else stringResource(R.string.annual_fee_free_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = hasAnnualFee,
                onCheckedChange = {
                    FeedbackManager.performToggle(view)
                    hasAnnualFee = it
                }
            )
        }

        if (hasAnnualFee) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
            if ((annualFeeWaiverCount.toIntOrNull() ?: 0) > 0) {
                OutlinedTextField(
                    value = initialWaiverSwipes,
                    onValueChange = { initialWaiverSwipes = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(R.string.initial_waiver_swipes_label)) },
                    placeholder = { Text("0 (${stringResource(R.string.initial_waiver_swipes_hint)})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedButton(
            onClick = {
                FeedbackManager.performClick(view)
                onSetAlarm()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("在系统时钟中设置还款提醒闹钟")
        }

        Button(
            onClick = {
                FeedbackManager.performConfirm(view)
                val limit = (creditLimit.toDoubleOrNull() ?: card.creditLimit).coerceAtLeast(0.0)
                val bDay = billDay.toIntOrNull()?.coerceIn(1, 31) ?: card.billDay
                val rDay = repaymentDay.toIntOrNull()?.coerceIn(1, 31) ?: card.repaymentDay
                val feeAmt = if (hasAnnualFee) (annualFeeAmount.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0) else 0.0
                val waiverCount = if (hasAnnualFee) (annualFeeWaiverCount.toIntOrNull() ?: 0).coerceAtLeast(0) else 0
                val initialSwipes = if (hasAnnualFee && waiverCount > 0) (initialWaiverSwipes.toIntOrNull() ?: 0).coerceAtLeast(0) else 0

                onUpdate(
                    card.copy(
                        bankName = bankName.trim().ifBlank { card.bankName },
                        cardAlias = cardAlias.trim().ifBlank { card.cardAlias },
                        last4Digits = extractedLast4,
                        creditLimit = limit,
                        billDay = bDay,
                        repaymentDay = rDay,
                        cardColorHex = selectedStyle.primaryHex,
                        cardNetwork = selectedNetwork,
                        cardStyle = selectedStyle.name,
                        hasAnnualFee = hasAnnualFee,
                        annualFeeAmount = feeAmt,
                        annualFeeWaiverCount = waiverCount,
                        initialWaiverSwipes = initialSwipes,
                        currencyCode = selectedCurrency.code
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        ) {
            Text("保存修改")
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除卡片？") },
            text = { Text("确定要删除卡片“${card.cardAlias}”吗？此操作将同时清除该卡的所有记录。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FeedbackManager.performDestructive(view)
                        showDeleteConfirm = false
                        onDelete(card.id)
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
}
