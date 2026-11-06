package com.example.cardmanager.feature.card.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeState = LocalThemeState.current
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<CreditCardEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("VaultCard", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (themeState.currentStyle == AppThemeStyle.MIUIX) "MIUIX HyperOS" else "Material 3",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { themeState.toggleStyle() },
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (themeState.currentStyle == AppThemeStyle.MIUIX) "M3" else "MIUIX",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Credit Card")
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
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Limit", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    currencyFormat.format(uiState.totalCreditLimit),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Outstanding", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    currencyFormat.format(uiState.totalDebt),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }

                if (uiState.cards.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No credit cards registered yet. Tap + to add one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(uiState.cards, key = { it.id }) { card ->
                        CreditCardItem(
                            card = card,
                            onClick = { selectedCardForDetail = card }
                        )
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
}
