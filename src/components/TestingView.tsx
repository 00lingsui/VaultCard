import React, { useState } from 'react';
import {
  FlaskConical,
  CheckCircle2,
  Copy,
  Check,
  Layers,
  Cpu,
  Database,
  ShieldAlert,
  Sparkles,
  Terminal,
  Activity,
} from 'lucide-react';

type SetupTab = 'gradle' | 'dispatcher_rule';
type SuiteTab = 'viewmodel' | 'dao' | 'repository' | 'settings_vm';

const GRADLE_TEST_DEPS_SNIPPET = `// app/build.gradle.kts
dependencies {
    // Core JUnit 4 + Kotlin Coroutines Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // MockK for Kotlin classes, suspend functions & Flow stubbing
    testImplementation("io.mockk:mockk:1.13.12")

    // Cash App Turbine for deterministic Flow / StateFlow assertions
    testImplementation("app.cash.turbine:turbine:1.1.0")

    // Room In-Memory testing + Robolectric for local JVM DAO tests
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.robolectric:robolectric:4.13")
}`;

const MAIN_DISPATCHER_RULE_SNIPPET = `// app/src/test/java/com/example/cardmanager/MainDispatcherRule.kt
package com.example.cardmanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces Dispatchers.Main so viewModelScope coroutines in CardListViewModel
 * and SettingsViewModel execute deterministically on the JVM test scheduler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}`;

interface SuiteExample {
  id: SuiteTab;
  label: string;
  fileName: string;
  target: string;
  tools: string[];
  summary: string;
  highlights: string[];
  code: string;
}

const TEST_SUITES: SuiteExample[] = [
  {
    id: 'viewmodel',
    label: 'CardListViewModel',
    fileName: 'CardListViewModelTest.kt',
    target: 'com.example.cardmanager.feature.card.ui.CardListViewModel',
    tools: ['MockK', 'Turbine', 'MainDispatcherRule'],
    summary:
      'Tests CardListUiState emission, shared bank credit-limit deduplication across multiple cards from the same bank, per-currency debt sums, and ReminderScheduler triggers on add/update.',
    highlights: [
      'Stubs CreditCardRepository Flow getters with MutableStateFlow so upstream updates re-emit through stateIn(WhileSubscribed(5_000)).',
      'Uses turbine.test { ... } to verify loading -> populated CardListUiState transitions without brittle Thread.sleep.',
      'Uses coVerify(exactly = 1) to confirm addCard() persists to repository and invokes reminderScheduler.triggerImmediateCheck().',
    ],
    code: `package com.example.cardmanager.feature.card.ui

import app.cash.turbine.test
import com.example.cardmanager.MainDispatcherRule
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity
import com.example.cardmanager.core.notification.ReminderScheduler
import com.example.cardmanager.feature.card.data.repository.CreditCardRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: CreditCardRepository = mockk()
    private val reminderScheduler: ReminderScheduler = mockk(relaxed = true)

    private val cardsFlow = MutableStateFlow<List<CreditCardEntity>>(emptyList())
    private val txFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private val totalDebtFlow = MutableStateFlow(0.0)
    private val swipeCountsFlow = MutableStateFlow<Map<Long, Int>>(emptyMap())

    @Before
    fun setup() {
        every { repository.getAllCards() } returns cardsFlow
        every { repository.getAllTransactions() } returns txFlow
        every { repository.getTotalDebt() } returns totalDebtFlow
        every { repository.getQualifyingSwipeCountsForCurrentYear() } returns swipeCountsFlow
    }

    @Test
    fun \`uiState deduplicates shared bank limit and aggregates debt by currency\`() = runTest {
        val viewModel = CardListViewModel(repository, reminderScheduler)

        val cmbCardA = CreditCardEntity(
            id = 1L, bankName = "招商银行", cardAlias = "Young",
            last4Digits = "1122", creditLimit = 30_000.0,
            billDay = 5, repaymentDay = 23, currencyCode = "CNY"
        )
        val cmbCardB = CreditCardEntity(
            id = 2L, bankName = "招商银行", cardAlias = "Classic",
            last4Digits = "3344", creditLimit = 50_000.0,
            billDay = 5, repaymentDay = 23, currencyCode = "CNY"
        )
        val spend = TransactionEntity(id = 10L, cardId = 1L, amount = 1280.0, category = "Dining")

        viewModel.uiState.test {
            // Initial emission from stateIn(initialValue = CardListUiState(isLoading = true))
            val initial = awaitItem()
            assertEquals(true, initial.isLoading)

            // Trigger upstream combine emissions
            cardsFlow.value = listOf(cmbCardA, cmbCardB)
            txFlow.value = listOf(spend)
            totalDebtFlow.value = 1280.0
            swipeCountsFlow.value = mapOf(1L to 4)

            val state = awaitItem()
            assertFalse(state.isLoading)
            // Same bank shares highest limit (50k instead of 30k + 50k)
            assertEquals(50_000.0, state.limitByCurrency["CNY"] ?: 0.0, 0.01)
            assertEquals(1280.0, state.debtByCardId[1L] ?: 0.0, 0.01)
            assertEquals(4, state.annualSwipeCounts[1L])

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun \`addCard persists entity and triggers immediate reminder check\`() = runTest {
        val viewModel = CardListViewModel(repository, reminderScheduler)
        val newCard = CreditCardEntity(
            bankName = "HSBC", cardAlias = "Pulse", last4Digits = "8899",
            creditLimit = 40_000.0, billDay = 10, repaymentDay = 28, currencyCode = "HKD"
        )
        coEvery { repository.addCard(newCard) } returns 9L
        every { reminderScheduler.triggerImmediateCheck() } just Runs

        viewModel.addCard(newCard)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.addCard(newCard) }
        coVerify(exactly = 1) { reminderScheduler.triggerImmediateCheck() }
    }
}`,
  },
  {
    id: 'dao',
    label: 'Room DAOs (In-Memory)',
    fileName: 'CreditCardDaoTest.kt',
    target: 'CreditCardDao & TransactionDao (Room In-Memory + Turbine)',
    tools: ['Room In-Memory', 'Turbine', 'Robolectric'],
    summary:
      'Executes real SQLite queries on Room.inMemoryDatabaseBuilder and collects DAO Flow emissions with Turbine to validate ordering, swipe waiver SQL filters, and ForeignKey.CASCADE deletes.',
    highlights: [
      'Uses allowMainThreadQueries() strictly in test builds with close() in @After.',
      'Asserts getAllCards() emits updates sorted by repayment_day ASC immediately after @Insert/@Delete.',
      'Verifies getQualifyingSwipeCountsByCardInRange() excludes Repayment/Refund rows and ForeignKey.CASCADE cleans up child transactions.',
    ],
    code: `package com.example.cardmanager.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.cardmanager.core.database.AppDatabase
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CreditCardDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var cardDao: CreditCardDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cardDao = db.creditCardDao()
        transactionDao = db.transactionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun \`getAllCards Flow emits cards sorted by repaymentDay ascending\`() = runTest {
        cardDao.getAllCards().test {
            assertTrue(awaitItem().isEmpty())

            cardDao.insertCard(
                CreditCardEntity(
                    bankName = "ICBC", cardAlias = "Peony", last4Digits = "1001",
                    creditLimit = 20_000.0, billDay = 1, repaymentDay = 25
                )
            )
            assertEquals(1, awaitItem().size)

            cardDao.insertCard(
                CreditCardEntity(
                    bankName = "Citi", cardAlias = "Premier", last4Digits = "2002",
                    creditLimit = 35_000.0, billDay = 15, repaymentDay = 8
                )
            )
            val sorted = awaitItem()
            assertEquals(listOf(8, 25), sorted.map { it.repaymentDay })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun \`deleting parent card cascades to transactions and excludes repayments from swipe count\`() = runTest {
        val cardId = cardDao.insertCard(
            CreditCardEntity(
                bankName = "CCB", cardAlias = "Dragon", last4Digits = "4321",
                creditLimit = 15_000.0, billDay = 5, repaymentDay = 25
            )
        )
        val now = 1_750_000_000_000L
        transactionDao.insertTransactions(
            listOf(
                TransactionEntity(cardId = cardId, amount = 240.0, category = "Shopping", dateTimestamp = now),
                TransactionEntity(cardId = cardId, amount = -240.0, category = "还款", dateTimestamp = now),
                TransactionEntity(cardId = cardId, amount = 95.0, category = "Refund", dateTimestamp = now)
            )
        )

        // Only the qualifying "Shopping" swipe should count toward annual fee waiver
        transactionDao.getQualifyingSwipeCountInRange(cardId, now - 10_000, now + 10_000).test {
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // ForeignKey.CASCADE removes child transactions when parent credit card is deleted
        cardDao.deleteCardById(cardId)
        assertTrue(transactionDao.getAllTransactionsList().isEmpty())
    }
}`,
  },
  {
    id: 'repository',
    label: 'Repository (MockK)',
    fileName: 'CreditCardRepositoryImplTest.kt',
    target: 'CreditCardRepositoryImpl (DAO Mocking + Mapping)',
    tools: ['MockK', 'Turbine', 'Coroutines Test'],
    summary:
      'Isolates CreditCardRepositoryImpl from SQLite by mocking CreditCardDao and TransactionDao with MockK, verifying swipe-count map transformation and boolean write outcomes.',
    highlights: [
      'Uses every { dao.flowMethod() } returns flowOf(...) for reactive DAO streams.',
      'Validates map { list -> list.associate { it.cardId to it.swipeCount } } in getQualifyingSwipeCountsForCurrentYear().',
      'Checks updateCard() returns true when DAO reports > 0 updated rows and false on 0.',
    ],
    code: `package com.example.cardmanager.feature.card.data.repository

import app.cash.turbine.test
import com.example.cardmanager.core.database.dao.CardSwipeCount
import com.example.cardmanager.core.database.dao.CreditCardDao
import com.example.cardmanager.core.database.dao.TransactionDao
import com.example.cardmanager.core.database.entity.CreditCardEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditCardRepositoryImplTest {

    private val cardDao: CreditCardDao = mockk()
    private val transactionDao: TransactionDao = mockk()
    private val repository = CreditCardRepositoryImpl(cardDao, transactionDao)

    @Test
    fun \`getQualifyingSwipeCountsForCurrentYear maps CardSwipeCount list to Map\`() = runTest {
        every {
            transactionDao.getQualifyingSwipeCountsByCardInRange(any(), any())
        } returns flowOf(
            listOf(
                CardSwipeCount(cardId = 1L, swipeCount = 6),
                CardSwipeCount(cardId = 3L, swipeCount = 12)
            )
        )

        repository.getQualifyingSwipeCountsForCurrentYear().test {
            assertEquals(mapOf(1L to 6, 3L to 12), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun \`updateCard maps affected row count to Boolean\`() = runTest {
        val card = CreditCardEntity(
            id = 5L, bankName = "BOC", cardAlias = "Global",
            last4Digits = "7788", creditLimit = 60_000.0, billDay = 7, repaymentDay = 27
        )
        coEvery { cardDao.updateCard(card) } returnsMany listOf(1, 0)

        assertTrue(repository.updateCard(card))
        assertFalse(repository.updateCard(card))
        coVerify(exactly = 2) { cardDao.updateCard(card) }
    }
}`,
  },
  {
    id: 'settings_vm',
    label: 'SettingsViewModel',
    fileName: 'SettingsViewModelTest.kt',
    target: 'SettingsViewModel (ReminderPreferenceManager + ReminderScheduler)',
    tools: ['MockK', 'Turbine', 'MainDispatcherRule'],
    summary:
      'Verifies smart notification settings state synchronization and WorkManager scheduler calls when toggling reminders, lead days, or preferred daily reminder hour.',
    highlights: [
      'Drives SettingsViewModel.reminderSettings via a mocked ReminderPreferenceManager.settingsFlow StateFlow.',
      'Asserts toggling reminders ON calls setEnabled(true), syncSchedule(hour, true), and triggerImmediateCheck().',
      'Asserts changing reminderHour updates WorkManager periodic scheduling via syncSchedule(newHour, enabled).',
    ],
    code: `package com.example.cardmanager.feature.settings

import android.content.Context
import app.cash.turbine.test
import com.example.cardmanager.MainDispatcherRule
import com.example.cardmanager.core.notification.ReminderPreferenceManager
import com.example.cardmanager.core.notification.ReminderScheduler
import com.example.cardmanager.core.notification.ReminderSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appContext: Context = mockk(relaxed = true)
    private val preferenceManager: ReminderPreferenceManager = mockk(relaxed = true)
    private val reminderScheduler: ReminderScheduler = mockk(relaxed = true)
    private val settingsState = MutableStateFlow(ReminderSettings())

    @Before
    fun setup() {
        every { preferenceManager.settingsFlow } returns settingsState
        coEvery { preferenceManager.getSettings() } answers { settingsState.value }
    }

    @Test
    fun \`enabling reminders persists preference and schedules immediate + periodic work\`() = runTest {
        settingsState.value = ReminderSettings(enabled = false, reminderHour = 9)
        val viewModel = SettingsViewModel(appContext, preferenceManager, reminderScheduler)

        viewModel.reminderSettings.test {
            assertEquals(false, awaitItem().enabled)

            coEvery { preferenceManager.setEnabled(true) } answers {
                settingsState.value = settingsState.value.copy(enabled = true)
            }

            viewModel.setRemindersEnabled(true)
            advanceUntilIdle()

            assertEquals(true, awaitItem().enabled)
            coVerify(exactly = 1) { preferenceManager.setEnabled(true) }
            verify(exactly = 1) { reminderScheduler.syncSchedule(reminderHour = 9, enabled = true) }
            verify(exactly = 1) { reminderScheduler.triggerImmediateCheck() }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun \`setReminderHour reschedules periodic worker with updated hour\`() = runTest {
        settingsState.value = ReminderSettings(enabled = true, reminderHour = 9)
        val viewModel = SettingsViewModel(appContext, preferenceManager, reminderScheduler)

        viewModel.setReminderHour(18)
        advanceUntilIdle()

        coVerify(exactly = 1) { preferenceManager.setReminderHour(18) }
        verify(exactly = 1) { reminderScheduler.syncSchedule(reminderHour = 18, enabled = true) }
    }
}`,
  },
];

const PITFALLS = [
  {
    title: 'Cold Flow vs. Hot StateFlow in Turbine',
    bad: 'Calling stateIn(WhileSubscribed(5_000)) assertions before subscribing inside turbine.test { ... }.',
    good: 'Wrap the Flow/StateFlow collection inside viewModel.uiState.test { ... } first, then mutate upstream MutableStateFlow mocks so WhileSubscribed remains active.',
  },
  {
    title: 'every vs. coEvery in MockK',
    bad: 'Stubbing suspend DAO functions (insertCard, getAllCardsList) with every { ... } causes compile/runtime errors.',
    good: 'Use coEvery / coVerify for suspend functions and regular every / verify for synchronous or Flow-returning DAO methods.',
  },
  {
    title: 'Advancing StandardTestDispatcher',
    bad: 'Asserting coVerify right after viewModel.addCard(card) without advancing the coroutine scheduler.',
    good: 'Call advanceUntilIdle() after triggering ViewModel launch blocks so viewModelScope coroutines finish before verify/coVerify.',
  },
  {
    title: 'Deterministic Date & Currency Assertions',
    bad: 'Relying on LocalDate.now() implicitly inside unit tests that run near midnight or month-end.',
    good: 'Pass explicit LocalDate fixtures into calculateDaysUntilRepayment(currentDate) and calculateDaysUntilStatement(currentDate) (e.g., Feb 28 with billDay = 31).',
  },
];

export const TestingView: React.FC = () => {
  const [setupTab, setSetupTab] = useState<SetupTab>('gradle');
  const [activeSuiteId, setActiveSuiteId] = useState<SuiteTab>('viewmodel');
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  const copyCode = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const activeSuite = TEST_SUITES.find((s) => s.id === activeSuiteId) || TEST_SUITES[0];
  const setupCode = setupTab === 'gradle' ? GRADLE_TEST_DEPS_SNIPPET : MAIN_DISPATCHER_RULE_SNIPPET;

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-wrap items-center gap-2 mb-2">
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-mono font-semibold">
            MockK 1.13
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono font-semibold">
            Turbine 1.1
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/20 font-mono font-semibold">
            Room In-Memory
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 font-mono">
            kotlinx-coroutines-test
          </span>
        </div>

        <h2 className="text-xl font-bold text-white flex items-center gap-2">
          <FlaskConical className="w-5 h-5 text-indigo-400 shrink-0" />
          Unit Testing Architecture: Room DAOs & ViewModels (MockK + Turbine)
        </h2>
        <p className="text-sm text-slate-400 mt-1 max-w-3xl">
          Deterministic JVM unit test patterns for VaultCard’s reactive data layer—isolating{' '}
          <code className="text-indigo-300">CreditCardDao</code>,{' '}
          <code className="text-indigo-300">TransactionDao</code>,{' '}
          <code className="text-indigo-300">CreditCardRepositoryImpl</code>, and Hilt ViewModels using{' '}
          <code className="text-emerald-300">MockK</code> stubbing and{' '}
          <code className="text-purple-300">Turbine</code> Flow assertions.
        </p>

        {/* 4 Core Pillars */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
          <div className="rounded-xl border border-indigo-500/25 bg-indigo-500/5 p-4">
            <div className="flex items-center gap-2 text-indigo-300 text-xs font-bold mb-1.5">
              <Cpu className="w-4 h-4" />
              MockK Stubbing
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Mocks <code className="text-indigo-200">CreditCardDao</code>,{' '}
              <code className="text-indigo-200">CreditCardRepository</code>, and{' '}
              <code className="text-indigo-200">ReminderScheduler</code> with{' '}
              <code className="text-indigo-200">coEvery</code> / <code className="text-indigo-200">coVerify</code> for suspend calls.
            </p>
          </div>

          <div className="rounded-xl border border-emerald-500/25 bg-emerald-500/5 p-4">
            <div className="flex items-center gap-2 text-emerald-300 text-xs font-bold mb-1.5">
              <Activity className="w-4 h-4" />
              Turbine Flow Testing
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Collects cold Room <code className="text-emerald-200">Flow</code> & hot{' '}
              <code className="text-emerald-200">StateFlow</code> emissions sequentially via{' '}
              <code className="text-emerald-200">.test {'{ awaitItem() }'}</code>.
            </p>
          </div>

          <div className="rounded-xl border border-purple-500/25 bg-purple-500/5 p-4">
            <div className="flex items-center gap-2 text-purple-300 text-xs font-bold mb-1.5">
              <Database className="w-4 h-4" />
              In-Memory Room SQL
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Runs real SQLite queries (<code className="text-purple-200">ORDER BY</code>,{' '}
              <code className="text-purple-200">ForeignKey.CASCADE</code>, waiver filters) without persisting files.
            </p>
          </div>

          <div className="rounded-xl border border-amber-500/25 bg-amber-500/5 p-4">
            <div className="flex items-center gap-2 text-amber-300 text-xs font-bold mb-1.5">
              <Terminal className="w-4 h-4" />
              MainDispatcherRule
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Binds <code className="text-amber-200">Dispatchers.Main</code> to{' '}
              <code className="text-amber-200">StandardTestDispatcher</code> so{' '}
              <code className="text-amber-200">viewModelScope</code> runs on JVM.
            </p>
          </div>
        </div>
      </div>

      {/* Test Harness Setup Section */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-800">
          <div>
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Layers className="w-4 h-4 text-indigo-400" />
              1. Test Dependencies & Coroutine Harness Setup
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Add MockK, Turbine, Coroutines Test, and Room In-Memory dependencies, then wire <code className="text-indigo-300">MainDispatcherRule</code> into ViewModel test classes.
            </p>
          </div>

          <div className="flex items-center gap-1.5 bg-slate-950 p-1 rounded-xl border border-slate-800 shrink-0">
            <button
              onClick={() => setSetupTab('gradle')}
              className={`px-3 py-1.5 rounded-lg text-xs font-mono transition-colors ${
                setupTab === 'gradle'
                  ? 'bg-indigo-600 text-white font-semibold'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              build.gradle.kts
            </button>
            <button
              onClick={() => setSetupTab('dispatcher_rule')}
              className={`px-3 py-1.5 rounded-lg text-xs font-mono transition-colors ${
                setupTab === 'dispatcher_rule'
                  ? 'bg-indigo-600 text-white font-semibold'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              MainDispatcherRule.kt
            </button>
          </div>
        </div>

        <div className="mt-4 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
          <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
            <span className="font-mono text-xs text-slate-300">
              {setupTab === 'gradle'
                ? 'app/build.gradle.kts — Unit Testing Dependencies'
                : 'app/src/test/.../MainDispatcherRule.kt'}
            </span>
            <button
              onClick={() => copyCode(setupCode, `setup-${setupTab}`)}
              className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
            >
              {copiedKey === `setup-${setupTab}` ? (
                <>
                  <Check className="w-3 h-3 text-emerald-400" />
                  <span className="text-emerald-400">Copied</span>
                </>
              ) : (
                <>
                  <Copy className="w-3 h-3" />
                  <span>Copy Snippet</span>
                </>
              )}
            </button>
          </div>
          <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed">
            <code>{setupCode}</code>
          </pre>
        </div>
      </div>

      {/* Interactive Test Suite Explorer */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-4 border-b border-slate-800">
          <div>
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-400" />
              2. Interactive Test Suite Examples (DAOs, Repository & ViewModels)
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Inspect production-ready JUnit tests targeting VaultCard’s exact DAO methods, repository contracts, and Hilt ViewModels.
            </p>
          </div>
        </div>

        {/* Suite Selector Buttons */}
        <div className="flex flex-wrap gap-2 mt-4">
          {TEST_SUITES.map((suite) => {
            const isSelected = suite.id === activeSuiteId;
            return (
              <button
                key={suite.id}
                onClick={() => setActiveSuiteId(suite.id)}
                className={`px-3.5 py-2 rounded-xl text-xs font-medium transition-all border ${
                  isSelected
                    ? 'bg-indigo-600 text-white border-indigo-500 shadow-lg shadow-indigo-600/25'
                    : 'bg-slate-950/70 text-slate-300 border-slate-800 hover:border-slate-700 hover:text-white'
                }`}
              >
                {suite.label}
              </button>
            );
          })}
        </div>

        {/* Suite Details + Code */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 mt-5">
          <div className="lg:col-span-4 space-y-4 bg-slate-950/70 border border-slate-800 rounded-xl p-4">
            <div>
              <div className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
                Target Under Test
              </div>
              <div className="text-xs font-mono text-indigo-300 mt-1 break-all">
                {activeSuite.target}
              </div>
            </div>

            <div className="flex flex-wrap gap-1.5">
              {activeSuite.tools.map((tool) => (
                <span
                  key={tool}
                  className="text-[10px] px-2 py-0.5 rounded bg-slate-900 border border-slate-700 text-slate-300 font-mono"
                >
                  {tool}
                </span>
              ))}
            </div>

            <p className="text-xs text-slate-300 leading-relaxed">{activeSuite.summary}</p>

            <div className="pt-3 border-t border-slate-800/80 space-y-2">
              <div className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
                What This Suite Verifies
              </div>
              {activeSuite.highlights.map((h, i) => (
                <div key={i} className="flex items-start gap-2 text-xs text-slate-300">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                  <span className="leading-relaxed">{h}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="lg:col-span-8 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
            <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
              <span className="font-mono text-xs text-slate-300">{activeSuite.fileName}</span>
              <button
                onClick={() => copyCode(activeSuite.code, `suite-${activeSuite.id}`)}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
              >
                {copiedKey === `suite-${activeSuite.id}` ? (
                  <>
                    <Check className="w-3 h-3 text-emerald-400" />
                    <span className="text-emerald-400">Copied</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3 h-3" />
                    <span>Copy Kotlin Test</span>
                  </>
                )}
              </button>
            </div>
            <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed max-h-[540px] overflow-y-auto">
              <code>{activeSuite.code}</code>
            </pre>
          </div>
        </div>
      </div>

      {/* Common MockK + Turbine Pitfalls */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="pb-4 border-b border-slate-800">
          <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-amber-400" />
            3. MockK & Turbine Pitfalls to Avoid
          </h3>
          <p className="text-xs text-slate-400 mt-1">
            Common failure modes when testing Kotlin Coroutines <code className="text-indigo-300">StateFlow</code> and Room DAOs on the JVM.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          {PITFALLS.map((item, idx) => (
            <div
              key={idx}
              className="rounded-xl bg-slate-950/70 border border-slate-800 p-4 space-y-2.5 text-xs"
            >
              <div className="font-semibold text-white">{item.title}</div>
              <div className="p-2.5 rounded-lg bg-rose-500/5 border border-rose-500/20 text-rose-200/90 leading-relaxed">
                <span className="font-mono text-[10px] uppercase tracking-wider text-rose-400 block mb-0.5">
                  Avoid
                </span>
                {item.bad}
              </div>
              <div className="p-2.5 rounded-lg bg-emerald-500/5 border border-emerald-500/20 text-emerald-200/90 leading-relaxed">
                <span className="font-mono text-[10px] uppercase tracking-wider text-emerald-400 block mb-0.5">
                  Recommended
                </span>
                {item.good}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
