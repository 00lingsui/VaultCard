package com.example.cardmanager.feature.card.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import com.example.cardmanager.ui.icons.AppIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cardmanager.R
import com.example.cardmanager.core.common.util.AlarmIntentHelper
import com.example.cardmanager.core.common.util.CalendarSyncHelper
import com.example.cardmanager.core.currency.AppCurrency
import com.example.cardmanager.core.currency.CurrencyPreferenceManager
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.feedback.FeedbackManager
import com.example.cardmanager.core.security.BiometricHelper
import com.example.cardmanager.core.security.findActivity
import com.example.cardmanager.feature.card.ui.components.AddCardDialog
import com.example.cardmanager.feature.card.ui.components.CardDetailBottomSheet
import com.example.cardmanager.feature.card.ui.components.CardDetailContent
import com.example.cardmanager.feature.card.ui.components.CreditCardItem
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    viewModel: CardListViewModel = hiltViewModel(),
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeState = LocalThemeState.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { CurrencyPreferenceManager.getDefaultCurrency(context) }
    val defaultCurrency by CurrencyPreferenceManager.defaultCurrencyFlow.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    val isWideScreen = widthSizeClass != WindowWidthSizeClass.Compact
    val expandedBankKeys = remember { mutableStateMapOf<String, Boolean>() }

    val selectedCardForDetail = remember(selectedCardId, uiState.cards) {
        uiState.cards.find { it.id == selectedCardId }
    }

    LaunchedEffect(isWideScreen, uiState.cards) {
        if (isWideScreen && selectedCardId == null && uiState.cards.isNotEmpty()) {
            selectedCardId = uiState.cards.first().id
        }
    }

    fun requestCardDeletion(cardId: Long, onCancelled: (() -> Unit)? = null) {
        val activity = context.findActivity()
        if (activity != null) {
            BiometricHelper.showBiometricPromptForAction(
                activity = activity,
                title = "确认删除",
                subtitle = "验证生物识别以删除此卡片",
                onError = { _, _ ->
                    onCancelled?.invoke()
                },
                onSuccess = {
                    FeedbackManager.performDestructive(view)
                    viewModel.deleteCard(cardId)
                    if (selectedCardId == cardId) {
                        selectedCardId = null
                    }
                }
            )
        } else {
            FeedbackManager.performDestructive(view)
            viewModel.deleteCard(cardId)
            if (selectedCardId == cardId) {
                selectedCardId = null
            }
        }
    }

    val cardCornerRadius = if (isMiui) 28.dp else 20.dp
    val fabCornerRadius = if (isMiui) 24.dp else 16.dp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (isMiui) (-0.5).sp else 0.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    FeedbackManager.performClick(view)
                    showAddDialog = true
                },
                shape = RoundedCornerShape(fabCornerRadius),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 14.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_card), modifier = Modifier.size(26.dp))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
        } else {
            val swipeableCardItem = @Composable { card: CreditCardEntity ->
                var dismissStateRef: SwipeToDismissBoxState? = null
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            FeedbackManager.performDestructive(view)
                            requestCardDeletion(card.id) {
                                coroutineScope.launch { dismissStateRef?.reset() }
                            }
                            false
                        } else {
                            false
                        }
                    }
                ).also { dismissStateRef = it }

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSwiping) 1.25f else 0.85f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                            label = "deleteIconScale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(cardCornerRadius))
                                .background(Color(0xFFDC2626))
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "删除",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.scale(iconScale)
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除卡片",
                                    tint = Color.White,
                                    modifier = Modifier.scale(iconScale)
                                )
                            }
                        }
                    }
                ) {
                    CreditCardItem(
                        card = card,
                        recordedSwipesInCurrentYear = uiState.annualSwipeCounts[card.id] ?: 0,
                        cardDebt = uiState.debtByCardId[card.id] ?: 0.0,
                        onClick = { selectedCardId = card.id },
                        onDeleteClick = {
                            requestCardDeletion(card.id, null)
                        }
                    )
                }
            }

            val cardListPane = @Composable { listModifier: Modifier ->
                val groupedBanks = remember(uiState.cards) {
                    uiState.cards.groupBy { it.bankName.trim().lowercase(Locale.ROOT) }.entries.toList()
                }

                LazyColumn(
                    modifier = listModifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // 1. Redesigned Premium Gradient Summary Card (Multi-Currency Aware)
                item {
                    val activeCurrencies = (uiState.limitByCurrency.keys + uiState.debtByCurrency.keys).distinct()
                    val isMultiCurrency = activeCurrencies.size > 1
                    val singleCurrency = if (activeCurrencies.size == 1) {
                        AppCurrency.fromCode(activeCurrencies.first())
                    } else {
                        defaultCurrency
                    }
                    val totalLimit = uiState.totalCreditLimit
                    val totalDebt = uiState.totalDebt
                    val availableCredit = (totalLimit - totalDebt).coerceAtLeast(0.0)
                    val utilizationRatio = if (totalLimit > 0) (totalDebt / totalLimit).toFloat().coerceIn(0f, 1f) else 0f
                    val utilizationPct = (utilizationRatio * 100).toInt()

                    val totalLimitText = if (isMultiCurrency) {
                        uiState.limitByCurrency.entries.joinToString(" · ") { (code, amt) ->
                            AppCurrency.fromCode(code).format(amt, trimWholeDecimals = true)
                        }.ifEmpty { defaultCurrency.format(0.0) }
                    } else {
                        singleCurrency.format(totalLimit)
                    }

                    val totalDebtText = if (isMultiCurrency) {
                        val nonZeroDebts = uiState.debtByCurrency.entries.filter { it.value > 0.0 }
                        if (nonZeroDebts.isEmpty()) {
                            defaultCurrency.format(0.0)
                        } else {
                            nonZeroDebts.joinToString(" · ") { (code, amt) ->
                                AppCurrency.fromCode(code).format(amt, trimWholeDecimals = true)
                            }
                        }
                    } else {
                        singleCurrency.format(totalDebt)
                    }

                    val availableCreditText = if (isMultiCurrency) {
                        uiState.limitByCurrency.entries.joinToString(" · ") { (code, lim) ->
                            val avail = (lim - (uiState.debtByCurrency[code] ?: 0.0)).coerceAtLeast(0.0)
                            AppCurrency.fromCode(code).format(avail, trimWholeDecimals = true)
                        }.ifEmpty { defaultCurrency.format(0.0) }
                    } else {
                        singleCurrency.format(availableCredit)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(cardCornerRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isMiui) {
                                            listOf(Color(0xFF1E2533), Color(0xFF141923), Color(0xFF0D1117))
                                        } else {
                                            listOf(Color(0xFF242F42), Color(0xFF1A2230), Color(0xFF111620))
                                        }
                                    )
                                )
                                .padding(22.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.portfolio_overview),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.4.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = stringResource(R.string.cards_count, uiState.cards.size),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = stringResource(R.string.total_credit_limit),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = totalLimitText,
                                            style = if (isMultiCurrency) {
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = (-0.3).sp
                                                )
                                            } else {
                                                MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = (-0.5).sp
                                                )
                                            },
                                            color = Color.White
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = stringResource(R.string.total_outstanding),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = totalDebtText,
                                            style = if (isMultiCurrency) {
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = (-0.3).sp
                                                )
                                            } else {
                                                MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = (-0.5).sp
                                                )
                                            },
                                            color = if (totalDebt > 0) Color(0xFFFF5252) else Color(0xFF10B981)
                                        )
                                    }
                                }

                                // Credit Utilization Progress Bar
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.available_credit_label, availableCreditText),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        val utilColor = when {
                                            utilizationPct >= 70 -> Color(0xFFFF5252)
                                            utilizationPct >= 35 -> Color(0xFFFBBF24)
                                            else -> Color(0xFF34D399)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(utilColor, CircleShape)
                                            )
                                            Text(
                                                text = stringResource(R.string.utilization_pct_label, utilizationPct),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = utilColor
                                            )
                                        }
                                    }

                                    LinearProgressIndicator(
                                        progress = { utilizationRatio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = when {
                                            utilizationPct >= 70 -> Color(0xFFFF5252)
                                            utilizationPct >= 35 -> Color(0xFFFBBF24)
                                            else -> Color(0xFF10B981)
                                        },
                                        trackColor = Color.White.copy(alpha = 0.12f),
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Section Header: My Cards
                if (uiState.cards.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "我的信用卡",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "点击卡片查看详情",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 3. Elevated Empty State or Collapsible Bank-Grouped Card List
                if (uiState.cards.isEmpty()) {
                    item {
                        com.example.cardmanager.feature.card.ui.components.EmptyCardsState(
                            onAddCardClick = {
                                FeedbackManager.performClick(view)
                                showAddDialog = true
                            },
                            isMiui = isMiui
                        )
                    }
                } else {
                    groupedBanks.forEach { (bankKey, bankCards) ->
                        if (bankCards.size == 1) {
                            val singleCard = bankCards.first()
                            item(key = "card_${singleCard.id}") {
                                swipeableCardItem(singleCard)
                            }
                        } else {
                            item(key = "bank_group_$bankKey") {
                                val isGroupExpanded = expandedBankKeys[bankKey] ?: true
                                val displayBankName = bankCards.first().bankName.trim()
                                val bankLimitSummary = bankCards
                                    .groupBy { it.currencyCode }
                                    .entries
                                    .joinToString(" · ") { (code, currencyCards) ->
                                        val maxLim = currencyCards.maxOfOrNull { it.creditLimit } ?: 0.0
                                        AppCurrency.fromCode(code).format(maxLim, trimWholeDecimals = true)
                                    }
                                val groupChevronRotation by animateFloatAsState(
                                    targetValue = if (isGroupExpanded) 180f else 0f,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "bankGroupChevron_$bankKey"
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(if (isMiui) 20.dp else 14.dp))
                                            .clickable {
                                                FeedbackManager.performClick(view)
                                                expandedBankKeys[bankKey] = !isGroupExpanded
                                            },
                                        shape = RoundedCornerShape(if (isMiui) 20.dp else 14.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = AppIcons.AccountBalance,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = displayBankName,
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        ) {
                                                            Text(
                                                                text = stringResource(R.string.cards_count, bankCards.size),
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 10.sp
                                                                ),
                                                                color = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = stringResource(R.string.bank_group_limit_summary, bankLimitSummary),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .rotate(groupChevronRotation)
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isGroupExpanded,
                                        enter = fadeIn(spring(dampingRatio = 0.85f)) + expandVertically(spring(dampingRatio = 0.8f, stiffness = 320f)),
                                        exit = fadeOut() + shrinkVertically(spring(dampingRatio = 0.85f, stiffness = 350f))
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            bankCards.forEach { card ->
                                                swipeableCardItem(card)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight()
                    ) {
                        cardListPane(Modifier)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        if (selectedCardForDetail != null) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(cardCornerRadius),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                CardDetailContent(
                                    card = selectedCardForDetail,
                                    recordedSwipesInCurrentYear = uiState.annualSwipeCounts[selectedCardForDetail.id] ?: 0,
                                    onUpdate = { updatedCard ->
                                        viewModel.updateCard(updatedCard)
                                    },
                                    onDelete = { cardId ->
                                        requestCardDeletion(cardId, null)
                                    },
                                    onSetAlarm = {
                                        AlarmIntentHelper.setRepaymentAlarm(context, selectedCardForDetail)
                                    },
                                    onClose = {
                                        selectedCardId = null
                                    },
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(cardCornerRadius),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = AppIcons.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "请选择一张信用卡",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "从左侧面板选择任意卡片以查看账单日、修改信用额度或管理交易。",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    cardListPane(Modifier)
                }
            }
        }
    }

    if (showAddDialog) {
        AddCardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newCard, syncCalendar ->
                viewModel.addCard(newCard)
                if (syncCalendar) {
                    CalendarSyncHelper.addRepaymentCalendarEvent(context, newCard)
                }
                showAddDialog = false
            }
        )
    }

    if (!isWideScreen && selectedCardForDetail != null) {
        CardDetailBottomSheet(
            card = selectedCardForDetail,
            recordedSwipesInCurrentYear = uiState.annualSwipeCounts[selectedCardForDetail.id] ?: 0,
            onDismiss = { selectedCardId = null },
            onUpdate = { updatedCard ->
                viewModel.updateCard(updatedCard)
                selectedCardId = null
            },
            onDelete = { cardId ->
                requestCardDeletion(cardId, null)
            },
            onSetAlarm = {
                AlarmIntentHelper.setRepaymentAlarm(context, selectedCardForDetail)
            }
        )
    }

    val updateManager = remember { com.example.cardmanager.core.update.UpdateManager() }
    var updateInfo by remember { mutableStateOf<com.example.cardmanager.core.update.AppUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val currentVersion = com.example.cardmanager.BuildConfig.VERSION_NAME
        val info = updateManager.checkForUpdates(currentVersion)
        if (info?.isUpdateAvailable == true && updateManager.isNewerVersion(currentVersion, info.latestVersion)) {
            updateInfo = info
        }
    }

    updateInfo?.let { update ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text(stringResource(R.string.update_available_title, update.latestVersion)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.update_available_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        update.releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateManager.startDownload(context, update.downloadUrl)
                        updateInfo = null
                    }
                ) {
                    Text(stringResource(R.string.download_update))
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text(stringResource(R.string.later))
                }
            }
        )
    }
}
