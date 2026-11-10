package com.example.cardmanager.feature.card.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cardmanager.R
import com.example.cardmanager.core.common.util.AlarmIntentHelper
import com.example.cardmanager.core.common.util.CalendarSyncHelper
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.feature.card.ui.components.AddCardDialog
import com.example.cardmanager.feature.card.ui.components.CardDetailBottomSheet
import com.example.cardmanager.feature.card.ui.components.CreditCardItem
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    viewModel: CardListViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeState = LocalThemeState.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<CreditCardEntity?>(null) }

    val cardCornerRadius = if (isMiui) 28.dp else 20.dp
    val fabCornerRadius = if (isMiui) 24.dp else 16.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (isMiui) (-0.5).sp else 0.sp
                            )
                        )
                        Text(
                            text = if (isMiui) stringResource(R.string.style_miuix) else stringResource(R.string.style_material3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Financial Analytics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Redesigned Premium Gradient Summary Card
                item {
                    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                    val totalLimit = uiState.totalCreditLimit
                    val totalDebt = uiState.totalDebt
                    val availableCredit = (totalLimit - totalDebt).coerceAtLeast(0.0)
                    val utilizationRatio = if (totalLimit > 0) (totalDebt / totalLimit).toFloat().coerceIn(0f, 1f) else 0f
                    val utilizationPct = (utilizationRatio * 100).toInt()

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
                                    Column {
                                        Text(
                                            text = stringResource(R.string.total_credit_limit),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = currencyFormat.format(totalLimit),
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = (-0.5).sp
                                            ),
                                            color = Color.White
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.total_outstanding),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = currencyFormat.format(totalDebt),
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = (-0.5).sp
                                            ),
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
                                            text = "Available: ${currencyFormat.format(availableCredit)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f)
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
                                                text = "$utilizationPct% Utilized",
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
                                text = "MY CREDIT CARDS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap card for details",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 3. Elevated Empty State or Card List
                if (uiState.cards.isEmpty()) {
                    item {
                        com.example.cardmanager.feature.card.ui.components.EmptyCardsState(
                            onAddCardClick = { showAddDialog = true },
                            isMiui = isMiui
                        )
                    }
                } else {
                    items(uiState.cards, key = { it.id }) { card ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteCard(card.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

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
                                            text = "Delete",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.scale(iconScale)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Card",
                                            tint = Color.White,
                                            modifier = Modifier.scale(iconScale)
                                        )
                                    }
                                }
                            }
                        ) {
                            CreditCardItem(
                                card = card,
                                onClick = { selectedCardForDetail = card }
                            )
                        }
                    }
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

    selectedCardForDetail?.let { card ->
        CardDetailBottomSheet(
            card = card,
            onDismiss = { selectedCardForDetail = null },
            onUpdate = { updatedCard ->
                viewModel.updateCard(updatedCard)
                selectedCardForDetail = null
            },
            onDelete = { cardId ->
                viewModel.deleteCard(cardId)
                selectedCardForDetail = null
            },
            onSetAlarm = {
                AlarmIntentHelper.setRepaymentAlarm(context, card)
            }
        )
    }

    val updateManager = remember { com.example.cardmanager.core.update.UpdateManager() }
    var updateInfo by remember { mutableStateOf<com.example.cardmanager.core.update.AppUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = updateManager.checkForUpdates("1.0.2")
        if (info?.isUpdateAvailable == true) {
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
