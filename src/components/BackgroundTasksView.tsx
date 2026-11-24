import React, { useState } from 'react';
import {
  BellRing,
  Clock,
  CheckCircle2,
  Copy,
  Check,
  Layers,
  AlertTriangle,
  Cpu,
  ShieldCheck,
  Settings2,
  CalendarClock,
  FileCode2,
} from 'lucide-react';

type ConfigTab = 'gradle_proguard' | 'manifest' | 'application' | 'worker' | 'scheduler';

interface ConfigFileTab {
  id: ConfigTab;
  label: string;
  fileName: string;
  role: string;
  highlights: string[];
  code: string;
}

const CONFIG_TABS: ConfigFileTab[] = [
  {
    id: 'gradle_proguard',
    label: 'Gradle & ProGuard',
    fileName: 'app/build.gradle.kts + app/proguard-rules.pro',
    role: 'Adds AndroidX WorkManager + Hilt-Work integration and preserves AssistedInject constructors under R8.',
    highlights: [
      'Uses KSP (ksp("androidx.hilt:hilt-compiler:1.2.0")) rather than kapt for fast annotation processing.',
      'Requires matching androidx.hilt:hilt-work:1.2.0 and work-runtime-ktx:2.9.1.',
      'R8 keep rules prevent minification from stripping @HiltWorker and @AssistedInject constructors in release APKs.',
    ],
    code: `// app/build.gradle.kts
dependencies {
    // WorkManager & Hilt Work integration for background repayment reminders
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
}

# app/proguard-rules.pro
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }
-keep class androidx.hilt.work.** { *; }
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
    @dagger.assisted.Assisted <init>(...);
}`,
  },
  {
    id: 'manifest',
    label: 'AndroidManifest.xml',
    fileName: 'app/src/main/AndroidManifest.xml',
    role: 'Declares POST_NOTIFICATIONS and removes WorkManager’s default Startup initializer so HiltWorkerFactory is used.',
    highlights: [
      'Without tools:node="remove", WorkManager auto-initializes using DefaultWorkerFactory before CardManagerApp provides HiltWorkerFactory.',
      'DefaultWorkerFactory cannot instantiate @AssistedInject workers with DAO/DataStore parameters, causing NoSuchMethodException.',
      'POST_NOTIFICATIONS enables runtime permission prompting on Android 13+ (API 33+).',
    ],
    code: `<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application android:name=".CardManagerApp" ...>
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="\${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <!-- Disable default WorkManagerInitializer so Configuration.Provider supplies HiltWorkerFactory -->
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>
    </application>
</manifest>`,
  },
  {
    id: 'application',
    label: 'CardManagerApp.kt',
    fileName: 'app/src/main/java/com/example/cardmanager/CardManagerApp.kt',
    role: 'Provides HiltWorkerFactory via Configuration.Provider and schedules daily reminders on app startup.',
    highlights: [
      'WorkManager 2.9+ uses property override val workManagerConfiguration: Configuration.',
      'Registers the high-priority NotificationChannel ("vaultcard_repayment_reminders") in onCreate().',
      'Launches reminderScheduler.scheduleFromPreferences() asynchronously on SupervisorJob + Dispatchers.IO.',
    ],
    code: `@HiltAndroidApp
class CardManagerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CardReminderNotificationManager.createNotificationChannel(this)
        applicationScope.launch {
            reminderScheduler.scheduleFromPreferences()
        }
    }
}`,
  },
  {
    id: 'worker',
    label: 'RepaymentReminderWorker.kt',
    fileName: 'core/notification/RepaymentReminderWorker.kt',
    role: 'Injects Room DAOs and DataStore ReminderPreferenceManager to evaluate due/statement dates and deduplicate notifications.',
    highlights: [
      'Annotated with @HiltWorker and uses @AssistedInject with @Assisted Context + WorkerParameters.',
      'Computes card debt from TransactionDao.getAllTransactionsList() and safe month-end dates via calculateDaysUntilRepayment().',
      'Deduplicates alerts per (cardId, eventKey, LocalDate) in DataStore so repeated triggers never spam on the same day.',
    ],
    code: `@HiltWorker
class RepaymentReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val creditCardDao: CreditCardDao,
    private val transactionDao: TransactionDao,
    private val preferenceManager: ReminderPreferenceManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settings = preferenceManager.getSettings()
        if (!settings.enabled || !CardReminderNotificationManager.hasNotificationPermission(applicationContext)) {
            return Result.success()
        }

        val cards = creditCardDao.getAllCardsList()
        val debtByCardId = transactionDao.getAllTransactionsList()
            .groupBy { it.cardId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val today = LocalDate.now()

        for (card in cards) {
            val balance = debtByCardId[card.id] ?: 0.0
            val daysUntilDue = card.calculateDaysUntilRepayment(today)

            val shouldNotifyDue = (daysUntilDue == 0L && settings.notifyOnDueDate) ||
                (daysUntilDue > 0L && daysUntilDue.toInt() in settings.advanceDays)

            if (shouldNotifyDue) {
                val eventKey = "due_\$daysUntilDue"
                if (!preferenceManager.hasNotifiedOnDate(card.id, eventKey, today)) {
                    val posted = CardReminderNotificationManager.showPaymentDueNotification(
                        applicationContext, card, daysUntilDue, balance
                    )
                    if (posted) preferenceManager.markNotifiedOnDate(card.id, eventKey, today)
                }
            }
        }
        return Result.success()
    }
}`,
  },
  {
    id: 'scheduler',
    label: 'ReminderScheduler.kt',
    fileName: 'core/notification/ReminderScheduler.kt',
    role: 'Aligns a 24h PeriodicWorkRequest to the user’s chosen local reminder hour and triggers immediate checks on card mutations.',
    highlights: [
      'Uses ExistingPeriodicWorkPolicy.UPDATE so changing reminderHour in Settings immediately updates the schedule.',
      'Cancels legacy uninjected work ("VaultCardRepaymentCheck") during sync to keep upgrades clean.',
      'Exposes triggerImmediateCheck() (OneTimeWorkRequest) invoked by CardListViewModel.addCard() and updateCard().',
    ],
    code: `@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: ReminderPreferenceManager
) {
    fun syncSchedule(reminderHour: Int, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
        if (!enabled) {
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
            return
        }

        val delayMillis = computeInitialDelayMillis(reminderHour)
        val periodicRequest = PeriodicWorkRequestBuilder<RepaymentReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(TAG_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    fun triggerImmediateCheck() {
        val oneTime = OneTimeWorkRequestBuilder<RepaymentReminderWorker>()
            .addTag(TAG_REMINDER_WORK)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTime
        )
    }
}`,
  },
];

const SIM_CURRENT_TIMES = [
  { label: '08:15 AM', hour: 8, minute: 15 },
  { label: '14:30 PM', hour: 14, minute: 30 },
  { label: '21:00 PM', hour: 21, minute: 0 },
];

const PREFERRED_HOURS = [9, 12, 18, 20];

const EVENT_SCENARIOS = [
  {
    id: 'due_3',
    label: 'Due in 3 Days',
    daysRemaining: 3,
    eventKey: 'due_3',
    title: 'Due in 3 days · CMB Platinum',
    body: 'China Merchants Bank (•••• 8842) is Due in 3 days. Current balance: ¥4,280.00',
  },
  {
    id: 'due_1',
    label: 'Due Tomorrow',
    daysRemaining: 1,
    eventKey: 'due_1',
    title: 'Due Tomorrow · CMB Platinum',
    body: 'China Merchants Bank (•••• 8842) is Due Tomorrow. Current balance: ¥4,280.00',
  },
  {
    id: 'due_0',
    label: 'Due Today',
    daysRemaining: 0,
    eventKey: 'due_0',
    title: 'Due Today · CMB Platinum',
    body: 'China Merchants Bank (•••• 8842) is Due Today. Current balance: ¥4,280.00',
  },
  {
    id: 'statement_0',
    label: 'Statement Day',
    daysRemaining: 0,
    eventKey: 'statement_0',
    title: 'New Statement Ready · CMB Platinum',
    body: 'China Merchants Bank (•••• 8842) statement issued today. Current balance: ¥4,280.00 — tap to review.',
  },
];

export const BackgroundTasksView: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ConfigTab>('gradle_proguard');
  const [simNowIdx, setSimNowIdx] = useState<number>(0);
  const [targetHour, setTargetHour] = useState<number>(9);
  const [simEventId, setSimEventId] = useState<string>('due_3');
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  const copyCode = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const currentTab = CONFIG_TABS.find((t) => t.id === activeTab) || CONFIG_TABS[0];
  const simNow = SIM_CURRENT_TIMES[simNowIdx];
  const nowMinutes = simNow.hour * 60 + simNow.minute;
  const targetMinutes = targetHour * 60;
  const isTomorrow = targetMinutes <= nowMinutes;
  const deltaMinutes = isTomorrow ? 24 * 60 - nowMinutes + targetMinutes : targetMinutes - nowMinutes;
  const delayHrs = Math.floor(deltaMinutes / 60);
  const delayMins = deltaMinutes % 60;
  const activeEvent = EVENT_SCENARIOS.find((e) => e.id === simEventId) || EVENT_SCENARIOS[0];

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-wrap items-center gap-2 mb-2">
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-mono font-semibold">
            WorkManager 2.9.1
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono font-semibold">
            @HiltWorker + HiltWorkerFactory
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20 font-mono font-semibold">
            24h Periodic + Immediate Check
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 font-mono">
            API 33+ POST_NOTIFICATIONS
          </span>
        </div>

        <h2 className="text-xl font-bold text-white flex items-center gap-2">
          <BellRing className="w-5 h-5 text-indigo-400 shrink-0" />
          Background Tasks: Hilt-Injected WorkManager for Repayment Reminders
        </h2>
        <p className="text-sm text-slate-400 mt-1 max-w-3xl">
          How VaultCard wires <code className="text-indigo-300">androidx.hilt:hilt-work</code> so{' '}
          <code className="text-emerald-300">RepaymentReminderWorker</code> cleanly injects{' '}
          <code className="text-indigo-300">CreditCardDao</code>,{' '}
          <code className="text-indigo-300">TransactionDao</code>, and DataStore{' '}
          <code className="text-indigo-300">ReminderPreferenceManager</code> without manual singletons or context leaks.
        </p>

        {/* 4-Step Wiring Architecture */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
          {[
            {
              step: 'Step 1',
              title: 'Hilt-Work + KSP',
              desc: 'Declare hilt-work:1.2.0 and ksp(hilt-compiler:1.2.0) in app/build.gradle.kts plus R8 keep rules.',
              style: 'border-blue-500/30 bg-blue-500/5 text-blue-300',
            },
            {
              step: 'Step 2',
              title: 'Remove Default Startup',
              desc: 'Disable WorkManagerInitializer in AndroidManifest.xml so WorkManager awaits HiltWorkerFactory.',
              style: 'border-amber-500/30 bg-amber-500/5 text-amber-300',
            },
            {
              step: 'Step 3',
              title: 'Configuration.Provider',
              desc: 'Implement Configuration.Provider in CardManagerApp and inject HiltWorkerFactory.',
              style: 'border-purple-500/30 bg-purple-500/5 text-purple-300',
            },
            {
              step: 'Step 4',
              title: '@HiltWorker + Scheduler',
              desc: 'Schedule aligned 24h PeriodicWorkRequest + OneTimeWorkRequest triggers in ReminderScheduler.',
              style: 'border-emerald-500/30 bg-emerald-500/5 text-emerald-300',
            },
          ].map((card) => (
            <div key={card.step} className={`rounded-xl border p-4 ${card.style}`}>
              <div className="text-[10px] font-mono uppercase tracking-wider opacity-80">
                {card.step}
              </div>
              <div className="text-xs font-bold mt-1">{card.title}</div>
              <p className="text-xs text-slate-300 mt-1.5 leading-relaxed">{card.desc}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Interactive Setup & Code Walkthrough */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-4 border-b border-slate-800">
          <div>
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <FileCode2 className="w-4 h-4 text-indigo-400" />
              1. Complete Hilt-Work Configuration & Worker Implementation
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Inspect each configuration layer across VaultCard’s build, manifest, Application class, Worker, and Scheduler.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap gap-2 mt-4">
          {CONFIG_TABS.map((tab) => {
            const isSelected = tab.id === activeTab;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-3.5 py-2 rounded-xl text-xs font-medium transition-all border ${
                  isSelected
                    ? 'bg-indigo-600 text-white border-indigo-500 shadow-lg shadow-indigo-600/25'
                    : 'bg-slate-950/70 text-slate-300 border-slate-800 hover:border-slate-700 hover:text-white'
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 mt-5">
          <div className="lg:col-span-4 space-y-4 bg-slate-950/70 border border-slate-800 rounded-xl p-4">
            <div>
              <span className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
                Artifact Role
              </span>
              <p className="text-xs text-slate-300 mt-1.5 leading-relaxed">{currentTab.role}</p>
            </div>

            <div className="pt-3 border-t border-slate-800/80 space-y-2">
              <span className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
                Key Requirements
              </span>
              {currentTab.highlights.map((h, i) => (
                <div key={i} className="flex items-start gap-2 text-xs text-slate-300">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                  <span className="leading-relaxed">{h}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="lg:col-span-8 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
            <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
              <span className="font-mono text-xs text-slate-300">{currentTab.fileName}</span>
              <button
                onClick={() => copyCode(currentTab.code, `tab-${currentTab.id}`)}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
              >
                {copiedKey === `tab-${currentTab.id}` ? (
                  <>
                    <Check className="w-3 h-3 text-emerald-400" />
                    <span className="text-emerald-400">Copied</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3 h-3" />
                    <span>Copy Code</span>
                  </>
                )}
              </button>
            </div>
            <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed max-h-[480px] overflow-y-auto">
              <code>{currentTab.code}</code>
            </pre>
          </div>
        </div>
      </div>

      {/* Interactive Schedule Delay & Deduplication Simulator */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-6 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="pb-3 border-b border-slate-800">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <CalendarClock className="w-4 h-4 text-indigo-400" />
              2. Initial Delay & Time-Alignment Simulator
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Simulates <code className="text-indigo-300">ReminderScheduler.computeInitialDelayMillis(targetHour)</code> so daily periodic checks align to the user’s preferred local time.
            </p>
          </div>

          <div className="space-y-3 text-xs">
            <div>
              <span className="text-slate-400 block mb-1.5 font-medium">Simulated Local Time (ZonedDateTime.now())</span>
              <div className="flex flex-wrap gap-2">
                {SIM_CURRENT_TIMES.map((t, idx) => (
                  <button
                    key={t.label}
                    onClick={() => setSimNowIdx(idx)}
                    className={`px-3 py-1.5 rounded-lg font-mono border transition-colors ${
                      simNowIdx === idx
                        ? 'bg-indigo-600 text-white border-indigo-500'
                        : 'bg-slate-950 text-slate-300 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    {t.label}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <span className="text-slate-400 block mb-1.5 font-medium">Preferred Reminder Hour (Settings)</span>
              <div className="flex flex-wrap gap-2">
                {PREFERRED_HOURS.map((hr) => (
                  <button
                    key={hr}
                    onClick={() => setTargetHour(hr)}
                    className={`px-3 py-1.5 rounded-lg font-mono border transition-colors ${
                      targetHour === hr
                        ? 'bg-emerald-600 text-white border-emerald-500'
                        : 'bg-slate-950 text-slate-300 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    {String(hr).padStart(2, '0')}:00
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-950/90 border border-slate-800 space-y-2 text-xs font-mono">
            <div className="flex items-center justify-between text-slate-400">
              <span>Next Target Trigger:</span>
              <span className="text-emerald-400 font-semibold">
                {isTomorrow ? 'Tomorrow' : 'Today'} at {String(targetHour).padStart(2, '0')}:00
              </span>
            </div>
            <div className="flex items-center justify-between text-slate-400">
              <span>Calculated Initial Delay:</span>
              <span className="text-indigo-300 font-semibold">
                {delayHrs}h {delayMins}m ({deltaMinutes * 60 * 1000} ms)
              </span>
            </div>
            <div className="flex items-center justify-between text-slate-400">
              <span>Work Policy:</span>
              <span className="text-slate-200">24h Periodic • ExistingPeriodicWorkPolicy.UPDATE</span>
            </div>
          </div>
        </div>

        {/* Deduplication & Notification Payload Preview */}
        <div className="lg:col-span-6 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="pb-3 border-b border-slate-800">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Settings2 className="w-4 h-4 text-emerald-400" />
              3. Deduplication Key & Notification Preview
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Combining <code className="text-indigo-300">PeriodicWorkRequest</code> with immediate checks after <code className="text-indigo-300">addCard()</code> requires per-day DataStore deduplication.
            </p>
          </div>

          <div className="flex flex-wrap gap-2">
            {EVENT_SCENARIOS.map((ev) => (
              <button
                key={ev.id}
                onClick={() => setSimEventId(ev.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                  simEventId === ev.id
                    ? 'bg-indigo-600 text-white border-indigo-500'
                    : 'bg-slate-950 text-slate-300 border-slate-800 hover:border-slate-700'
                }`}
              >
                {ev.label}
              </button>
            ))}
          </div>

          {/* Simulated Notification Card */}
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-700/80 shadow-lg space-y-2">
            <div className="flex items-center justify-between text-[11px] text-slate-400">
              <span className="flex items-center gap-1.5 font-semibold text-indigo-300">
                <BellRing className="w-3.5 h-3.5" />
                VaultCard • CHANNEL: vaultcard_repayment_reminders
              </span>
              <span className="font-mono text-slate-500">IMPORTANCE_HIGH</span>
            </div>
            <div className="text-sm font-bold text-white">{activeEvent.title}</div>
            <div className="text-xs text-slate-300 leading-relaxed">{activeEvent.body}</div>
          </div>

          <div className="p-3 rounded-xl bg-[#070b14] border border-slate-800 text-xs font-mono text-slate-300 space-y-1">
            <div className="text-[11px] text-slate-500">// Stored in DataStore (14-day retention pruning)</div>
            <div>
              dedupEntry = <span className="text-emerald-400">"1:{activeEvent.eventKey}:2026-11-23"</span>
            </div>
          </div>
        </div>
      </div>

      {/* Common WorkManager + Hilt Pitfalls */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="pb-3 border-b border-slate-800">
          <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-400" />
            4. Hilt-Work Troubleshooting Checklist
          </h3>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4 text-xs">
          <div className="p-4 rounded-xl bg-slate-950/70 border border-slate-800 space-y-1.5">
            <div className="font-semibold text-white flex items-center gap-1.5">
              <Cpu className="w-3.5 h-3.5 text-rose-400" />
              NoSuchMethodException on Worker
            </div>
            <p className="text-slate-400 leading-relaxed">
              Occurs when <code className="text-indigo-300">WorkManagerInitializer</code> is not removed in <code className="text-indigo-300">AndroidManifest.xml</code> or <code className="text-indigo-300">ksp("androidx.hilt:hilt-compiler:1.2.0")</code> is missing.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-950/70 border border-slate-800 space-y-1.5">
            <div className="font-semibold text-white flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-amber-400" />
              Month-End Day 31 Edge Cases
            </div>
            <p className="text-slate-400 leading-relaxed">
              Clamp day-of-month using <code className="text-emerald-300">minOf(day, yearMonth.lengthOfMonth())</code> in <code className="text-indigo-300">CreditCardExtensions.kt</code> so cards billed on the 31st still fire in Feb/April.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-950/70 border border-slate-800 space-y-1.5">
            <div className="font-semibold text-white flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              Android 13+ Silent Suppression
            </div>
            <p className="text-slate-400 leading-relaxed">
              Verify both <code className="text-indigo-300">POST_NOTIFICATIONS</code> and <code className="text-indigo-300">NotificationManagerCompat.areNotificationsEnabled()</code> before marking a notification sent in DataStore.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
