export interface KotlinFile {
  id: string;
  name: string;
  packagePath: string;
  module: string;
  category: 'Entity' | 'DAO' | 'Database' | 'DI' | 'Utility' | 'Repository' | 'Compose UI';
  description: string;
  code: string;
}

export const KOTLIN_CODEBASE: KotlinFile[] = [
  {
    id: 'entity',
    name: 'CreditCardEntity.kt',
    packagePath: 'com.example.cardmanager.core.database.entity',
    module: ':core:database',
    category: 'Entity',
    description: 'Room Entity representing credit card details with indexing on last4Digits and repaymentDay for fast query filtering.',
    code: `package com.example.cardmanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Credit Card record.
 *
 * @property id Auto-generated primary key (0L defaults to auto-generation)
 * @property bankName Name of the issuing bank (e.g. "Chase", "Citibank", "Capital One")
 * @property cardAlias User custom label (e.g. "Sapphire Preferred", "Daily Groceries")
 * @property last4Digits Last 4 digits of the physical/virtual card for quick identification
 * @property creditLimit Total credit limit allocated to this card
 * @property billDay Monthly statement generation day (1..31)
 * @property repaymentDay Monthly payment deadline/due day (1..31)
 * @property cardColorHex Hex color code (e.g. "#1E3A8A") for Material 3 card styling
 */
@Entity(
    tableName = "credit_cards",
    indices = [
        Index(value = ["last4Digits"]),
        Index(value = ["repaymentDay"])
    ]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "bank_name")
    val bankName: String,

    @ColumnInfo(name = "card_alias")
    val cardAlias: String,

    @ColumnInfo(name = "last_4_digits")
    val last4Digits: String,

    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double,

    @ColumnInfo(name = "bill_day")
    val billDay: Int,

    @ColumnInfo(name = "repayment_day")
    val repaymentDay: Int,

    @ColumnInfo(name = "card_color_hex")
    val cardColorHex: String = "#1E293B"
) {
    init {
        require(billDay in 1..31) { "Bill day must be between 1 and 31" }
        require(repaymentDay in 1..31) { "Repayment day must be between 1 and 31" }
        require(last4Digits.length == 4 && last4Digits.all { it.isDigit() }) {
            "last4Digits must be exactly 4 numeric characters"
        }
        require(creditLimit >= 0.0) { "Credit limit cannot be negative" }
    }
}`
  },
  {
    id: 'dao',
    name: 'CreditCardDao.kt',
    packagePath: 'com.example.cardmanager.core.database.dao',
    module: ':core:database',
    category: 'DAO',
    description: 'Room Data Access Object with reactive Kotlin Coroutines Flow streams and asynchronous suspend functions.',
    code: `package com.example.cardmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.cardmanager.core.database.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [CreditCardEntity].
 * Exposes reactive [Flow] streams for real-time UI updates with Jetpack Compose.
 */
@Dao
interface CreditCardDao {

    /**
     * Observes all credit cards sorted by repayment day ascending.
     * Emits a new list whenever the database table changes.
     */
    @Query("SELECT * FROM credit_cards ORDER BY repayment_day ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    /**
     * Observes a single credit card by its primary key ID.
     */
    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    fun getCardById(id: Long): Flow<CreditCardEntity?>

    /**
     * One-shot fetch for a credit card (used in background workers / Sync).
     */
    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCardByIdOnce(id: Long): CreditCardEntity?

    /**
     * Inserts a new credit card into the database.
     * @return The newly generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity): Long

    /**
     * Inserts or updates a credit card using Room 2.5+ upsert capability.
     */
    @Upsert
    suspend fun upsertCard(card: CreditCardEntity): Long

    /**
     * Updates an existing credit card record.
     * @return The number of rows successfully updated.
     */
    @Update
    suspend fun updateCard(card: CreditCardEntity): Int

    /**
     * Deletes a credit card record.
     * @return The number of rows successfully deleted.
     */
    @Delete
    suspend fun deleteCard(card: CreditCardEntity): Int

    /**
     * Deletes a credit card directly by its primary key ID.
     */
    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long): Int

    /**
     * Returns total count of registered cards for metrics.
     */
    @Query("SELECT COUNT(*) FROM credit_cards")
    fun getCardCount(): Flow<Int>
}`
  },
  {
    id: 'database',
    name: 'AppDatabase.kt',
    packagePath: 'com.example.cardmanager.core.database',
    module: ':core:database',
    category: 'Database',
    description: 'Room Database definition with singleton companion and exportSchema configuration.',
    code: `package com.example.cardmanager.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.entity.CreditCardEntity

/**
 * Main Room Database for the Credit Card Management application.
 */
@Database(
    entities = [CreditCardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun creditCardDao(): CreditCardDao

    companion object {
        const val DATABASE_NAME = "credit_card_app.db"

        /**
         * Fallback manual factory for non-Hilt environments or standalone tests.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}`
  },
  {
    id: 'di',
    name: 'DatabaseModule.kt',
    packagePath: 'com.example.cardmanager.core.database.di',
    module: ':core:database',
    category: 'DI',
    description: 'Hilt Dependency Injection module providing singleton AppDatabase and CreditCardDao instances.',
    code: `package com.example.cardmanager.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.cardmanager.core.database.AppDatabase
import com.example.cardmanager.core.database.dao.CreditCardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module that provisions Room database dependencies
 * throughout the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCreditCardDao(
        database: AppDatabase
    ): CreditCardDao {
        return database.creditCardDao()
    }
}`
  },
  {
    id: 'utility',
    name: 'CreditCardExtensions.kt',
    packagePath: 'com.example.cardmanager.core.common.util',
    module: ':core:common',
    category: 'Utility',
    description: 'Production-ready Kotlin extension function calculating remaining days until next repayment, handling leap years, month length differences, and cycle rollovers.',
    code: `package com.example.cardmanager.core.common.util

import com.example.cardmanager.core.database.entity.CreditCardEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Calculates the exact remaining calendar days from [currentDate] until the next repayment date.
 *
 * Real-world Financial Cycle Handling:
 * 1. Clamps day of month to valid boundary (e.g., repaymentDay 31 clamped to 28/29 in February).
 * 2. If [currentDate] is on or before this month's repayment day, the due date is in the current month.
 * 3. If [currentDate] has passed this month's repayment day, the target due date advances to next month.
 * 4. Uses modern [java.time.LocalDate] and [ChronoUnit.DAYS] for accurate calendar calculations.
 *
 * @param currentDate The reference evaluation date, defaults to [LocalDate.now].
 * @return Remaining days (0 = due today, >0 = future days).
 */
fun CreditCardEntity.calculateDaysUntilRepayment(
    currentDate: LocalDate = LocalDate.now()
): Long {
    val currentYearMonth = YearMonth.from(currentDate)

    // Clamp repaymentDay to the maximum days available in the current month
    val safeRepaymentDayThisMonth = minOf(this.repaymentDay, currentYearMonth.lengthOfMonth())
    val repaymentDateThisMonth = currentYearMonth.atDay(safeRepaymentDayThisMonth)

    val nextRepaymentDate = if (!currentDate.isAfter(repaymentDateThisMonth)) {
        // Today is before or on today's due date
        repaymentDateThisMonth
    } else {
        // Already passed this month's repayment due date; advance to next month
        val nextYearMonth = currentYearMonth.plusMonths(1)
        val safeRepaymentDayNextMonth = minOf(this.repaymentDay, nextYearMonth.lengthOfMonth())
        nextYearMonth.atDay(safeRepaymentDayNextMonth)
    }

    return ChronoUnit.DAYS.between(currentDate, nextRepaymentDate)
}

/**
 * Calculates the exact upcoming repayment [LocalDate].
 */
fun CreditCardEntity.getNextRepaymentDate(
    currentDate: LocalDate = LocalDate.now()
): LocalDate {
    val currentYearMonth = YearMonth.from(currentDate)
    val safeRepaymentDayThisMonth = minOf(this.repaymentDay, currentYearMonth.lengthOfMonth())
    val repaymentDateThisMonth = currentYearMonth.atDay(safeRepaymentDayThisMonth)

    return if (!currentDate.isAfter(repaymentDateThisMonth)) {
        repaymentDateThisMonth
    } else {
        val nextYearMonth = currentYearMonth.plusMonths(1)
        val safeRepaymentDayNextMonth = minOf(this.repaymentDay, nextYearMonth.lengthOfMonth())
        nextYearMonth.atDay(safeRepaymentDayNextMonth)
    }
}

/**
 * Checks if the repayment deadline is imminent (e.g., within 3 days or today).
 */
fun CreditCardEntity.isRepaymentDueSoon(
    thresholdDays: Long = 3L,
    currentDate: LocalDate = LocalDate.now()
): Boolean {
    val daysRemaining = calculateDaysUntilRepayment(currentDate)
    return daysRemaining in 0L..thresholdDays
}`
  },
  {
    id: 'repository',
    name: 'CreditCardRepository.kt',
    packagePath: 'com.example.cardmanager.feature.card.data.repository',
    module: ':feature:card_management',
    category: 'Repository',
    description: 'Clean Architecture Repository abstracting CreditCardDao with Coroutine Dispatchers.',
    code: `package com.example.cardmanager.feature.card.data.repository

import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.entity.CreditCardEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface CreditCardRepository {
    fun getAllCards(): Flow<List<CreditCardEntity>>
    fun getCardById(id: Long): Flow<CreditCardEntity?>
    suspend fun addCard(card: CreditCardEntity): Long
    suspend fun updateCard(card: CreditCardEntity): Boolean
    suspend fun deleteCard(cardId: Long): Boolean
}

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val cardDao: CreditCardDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CreditCardRepository {

    override fun getAllCards(): Flow<List<CreditCardEntity>> =
        cardDao.getAllCards().flowOn(ioDispatcher)

    override fun getCardById(id: Long): Flow<CreditCardEntity?> =
        cardDao.getCardById(id).flowOn(ioDispatcher)

    override suspend fun addCard(card: CreditCardEntity): Long =
        withContext(ioDispatcher) {
            cardDao.insertCard(card)
        }

    override suspend fun updateCard(card: CreditCardEntity): Boolean =
        withContext(ioDispatcher) {
            cardDao.updateCard(card) > 0
        }

    override suspend fun deleteCard(cardId: Long): Boolean =
        withContext(ioDispatcher) {
            cardDao.deleteCardById(cardId) > 0
        }
}`
  },
  {
    id: 'viewmodel',
    name: 'CardListViewModel.kt',
    packagePath: 'com.example.cardmanager.feature.card.ui',
    module: ':feature:card_management',
    category: 'Repository',
    description: 'MVVM ViewModel exposing StateFlow of CardListUiState with card list, total credit limit, and debt calculation.',
    code: `package com.example.cardmanager.feature.card.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.feature.card.data.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardListUiState(
    val cards: List<CreditCardEntity> = emptyList(),
    val totalCreditLimit: Double = 0.0,
    val totalEstimatedDebt: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val repository: CreditCardRepository
) : ViewModel() {

    val uiState: StateFlow<CardListUiState> = repository.getAllCards()
        .map { cardList ->
            val totalLimit = cardList.sumOf { it.creditLimit }
            // Estimated current cycle outstanding balance across cards
            val totalDebt = cardList.sumOf { it.creditLimit * 0.15 } 

            CardListUiState(
                cards = cardList,
                totalCreditLimit = totalLimit,
                totalEstimatedDebt = totalDebt,
                isLoading = false
            )
        }
        .catch { error ->
            emit(
                CardListUiState(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to load credit cards"
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CardListUiState(isLoading = true)
        )

    fun addCard(card: CreditCardEntity) {
        viewModelScope.launch {
            repository.addCard(card)
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }
}`
  },
  {
    id: 'compose_ui',
    name: 'CreditCardItem.kt',
    packagePath: 'com.example.cardmanager.feature.card.ui.components',
    module: ':feature:card_management',
    category: 'Compose UI',
    description: 'Jetpack Compose Material 3 Credit Card UI component with dynamic gradient and remaining days badge.',
    code: `package com.example.cardmanager.feature.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.database.entity.CreditCardEntity
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardItem(
    card: CreditCardEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val daysRemaining = card.calculateDaysUntilRepayment()
    val baseColor = try {
        Color(android.graphics.Color.parseColor(card.cardColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            baseColor.copy(alpha = 0.75f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Bank Name and Repayment Countdown Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = card.bankName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = card.cardAlias,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // Urgency Badge
                    Surface(
                        color = when {
                            daysRemaining <= 3L -> Color(0xFFEF4444) // Urgent red
                            daysRemaining <= 7L -> Color(0xFFF59E0B) // Amber
                            else -> Color(0xFF10B981) // Green
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (daysRemaining == 0L) "Due Today!" else "$daysRemaining days left",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Middle: Masked Card Number
                Text(
                    text = "••••  ••••  ••••  \${card.last4Digits}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )

                // Bottom row: Limit and Billing/Repayment Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CREDIT LIMIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                        Text(
                            text = currencyFormat.format(card.creditLimit),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BILL / DUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "\${card.billDay}th / \${card.repaymentDay}th",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}`
  },
  {
    id: 'screen',
    name: 'CardListScreen.kt',
    packagePath: 'com.example.cardmanager.feature.card.ui',
    module: ':feature:card_management',
    category: 'Compose UI',
    description: 'Main Dashboard screen observing StateFlow with summary metrics header, lazy column of cards, and FAB.',
    code: `package com.example.cardmanager.feature.card.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cardmanager.feature.card.ui.components.AddCardDialog
import com.example.cardmanager.feature.card.ui.components.CreditCardItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    viewModel: CardListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards", fontWeight = FontWeight.Bold) },
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
                                .padding(16.dp),
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
                                Text("Estimated Debt", style = MaterialTheme.typography.labelMedium)
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
                        CreditCardItem(card = card)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newCard ->
                viewModel.addCard(newCard)
                showAddDialog = false
            }
        )
    }
}`
  },
  {
    id: 'dialog',
    name: 'AddCardDialog.kt',
    packagePath: 'com.example.cardmanager.feature.card.ui.components',
    module: ':feature:card_management',
    category: 'Compose UI',
    description: 'Material 3 input dialog with form validation and color selector.',
    code: `package com.example.cardmanager.feature.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cardmanager.core.database.entity.CreditCardEntity

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (CreditCardEntity) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var cardAlias by remember { mutableStateOf("") }
    var last4Digits by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var billDay by remember { mutableStateOf("") }
    var repaymentDay by remember { mutableStateOf("") }
    var selectedCardColor by remember { mutableStateOf("#1E3A8A") }
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
                                )
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
}`
  }
];

export const PACKAGE_STRUCTURE_MARKDOWN = `
\`\`\`
app/
├── core/
│   ├── common/
│   │   └── util/
│   │       └── CreditCardExtensions.kt  <-- Repayment calculation extension
│   └── database/
│       ├── AppDatabase.kt               <-- Room DB declaration
│       ├── dao/
│       │   └── CreditCardDao.kt         <-- CRUD & Flow streams
│       ├── di/
│       │   └── DatabaseModule.kt        <-- Hilt SingletonComponent bindings
│       └── entity/
│           └── CreditCardEntity.kt      <-- Room Entity with indices
└── feature/
    └── card_management/
        ├── data/
        │   └── repository/
        │       ├── CreditCardRepository.kt
        │       └── CreditCardRepositoryImpl.kt
        ├── domain/
        │   └── usecase/
        │       └── GetUpcomingPaymentsUseCase.kt
        └── ui/
            ├── components/
            │   └── CreditCardItem.kt    <-- Material 3 Jetpack Compose
            ├── CardListViewModel.kt
            └── CardListScreen.kt
\`\`\`
`;
