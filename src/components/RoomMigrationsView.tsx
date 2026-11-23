import React, { useState } from 'react';
import {
  Database,
  GitBranch,
  CheckCircle2,
  AlertTriangle,
  Copy,
  Check,
  ArrowRight,
  Layers,
  ShieldCheck,
  Wrench,
  Sparkles,
  FileJson,
  FlaskConical,
  Table2,
} from 'lucide-react';

type SchemaTab = 'credit_cards' | 'transactions';
type StrategyScenarioId = 'add_column' | 'rename_delete' | 'backfill_data' | 'rebuild_table';
type GuideTab = 'auto' | 'manual' | 'testing';

interface ColumnSpec {
  column: string;
  kotlinField: string;
  kotlinType: string;
  sqliteType: string;
  defaultVal?: string;
  addedIn: string;
  notes?: string;
}

const CREDIT_CARDS_COLUMNS: ColumnSpec[] = [
  { column: 'id', kotlinField: 'id', kotlinType: 'Long', sqliteType: 'INTEGER PRIMARY KEY AUTOINCREMENT', addedIn: 'v1' },
  { column: 'bank_name', kotlinField: 'bankName', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', addedIn: 'v1' },
  { column: 'card_alias', kotlinField: 'cardAlias', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', addedIn: 'v1' },
  { column: 'last_4_digits', kotlinField: 'last4Digits', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', addedIn: 'v1', notes: 'Indexed' },
  { column: 'credit_limit', kotlinField: 'creditLimit', kotlinType: 'Double', sqliteType: 'REAL NOT NULL', addedIn: 'v1' },
  { column: 'bill_day', kotlinField: 'billDay', kotlinType: 'Int', sqliteType: 'INTEGER NOT NULL', addedIn: 'v1' },
  { column: 'repayment_day', kotlinField: 'repaymentDay', kotlinType: 'Int', sqliteType: 'INTEGER NOT NULL', addedIn: 'v1', notes: 'Indexed' },
  { column: 'card_color_hex', kotlinField: 'cardColorHex', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', addedIn: 'v1' },
  { column: 'has_annual_fee', kotlinField: 'hasAnnualFee', kotlinType: 'Boolean', sqliteType: 'INTEGER NOT NULL', defaultVal: '0', addedIn: 'v2' },
  { column: 'annual_fee_amount', kotlinField: 'annualFeeAmount', kotlinType: 'Double', sqliteType: 'REAL NOT NULL', defaultVal: '0.0', addedIn: 'v2' },
  { column: 'annual_fee_waiver_count', kotlinField: 'annualFeeWaiverCount', kotlinType: 'Int', sqliteType: 'INTEGER NOT NULL', defaultVal: '0', addedIn: 'v2' },
  { column: 'card_network', kotlinField: 'cardNetwork', kotlinType: 'CardNetwork', sqliteType: 'TEXT NOT NULL', defaultVal: "'UNIONPAY'", addedIn: 'v3', notes: 'TypeConverter' },
  { column: 'card_style', kotlinField: 'cardStyle', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', defaultVal: "'DEEP_OCEAN'", addedIn: 'v3' },
  { column: 'initial_waiver_swipes', kotlinField: 'initialWaiverSwipes', kotlinType: 'Int', sqliteType: 'INTEGER NOT NULL', defaultVal: '0', addedIn: 'v4' },
  { column: 'currency_code', kotlinField: 'currencyCode', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', defaultVal: "'CNY'", addedIn: 'v5' },
];

const TRANSACTIONS_COLUMNS: ColumnSpec[] = [
  { column: 'id', kotlinField: 'id', kotlinType: 'Long', sqliteType: 'INTEGER PRIMARY KEY AUTOINCREMENT', addedIn: 'v1' },
  { column: 'card_id', kotlinField: 'cardId', kotlinType: 'Long', sqliteType: 'INTEGER NOT NULL', addedIn: 'v1', notes: 'FK → credit_cards(id) CASCADE • Indexed' },
  { column: 'amount', kotlinField: 'amount', kotlinType: 'Double', sqliteType: 'REAL NOT NULL', addedIn: 'v1' },
  { column: 'category', kotlinField: 'category', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', addedIn: 'v1' },
  { column: 'note', kotlinField: 'note', kotlinType: 'String', sqliteType: 'TEXT NOT NULL', addedIn: 'v1' },
  { column: 'date_timestamp', kotlinField: 'dateTimestamp', kotlinType: 'Long', sqliteType: 'INTEGER NOT NULL', addedIn: 'v1' },
];

const EXISTING_MIGRATIONS = [
  {
    id: 'MIGRATION_1_2',
    from: 1,
    to: 2,
    summary: 'Added annual fee tracking & waiver swipe threshold columns to credit_cards.',
    sql: [
      'ALTER TABLE credit_cards ADD COLUMN has_annual_fee INTEGER NOT NULL DEFAULT 0',
      'ALTER TABLE credit_cards ADD COLUMN annual_fee_amount REAL NOT NULL DEFAULT 0.0',
      'ALTER TABLE credit_cards ADD COLUMN annual_fee_waiver_count INTEGER NOT NULL DEFAULT 0',
    ],
  },
  {
    id: 'MIGRATION_2_3',
    from: 2,
    to: 3,
    summary: 'Added card network enum (CardConverters) and visual card surface preset.',
    sql: [
      "ALTER TABLE credit_cards ADD COLUMN card_network TEXT NOT NULL DEFAULT 'UNIONPAY'",
      "ALTER TABLE credit_cards ADD COLUMN card_style TEXT NOT NULL DEFAULT 'DEEP_OCEAN'",
    ],
  },
  {
    id: 'MIGRATION_3_4',
    from: 3,
    to: 4,
    summary: 'Added pre-app initial swipe count toward annual fee waiver progress.',
    sql: [
      'ALTER TABLE credit_cards ADD COLUMN initial_waiver_swipes INTEGER NOT NULL DEFAULT 0',
    ],
  },
  {
    id: 'MIGRATION_4_5',
    from: 4,
    to: 5,
    summary: 'Added per-card ISO currency code for multi-currency limits & balances.',
    sql: [
      "ALTER TABLE credit_cards ADD COLUMN currency_code TEXT NOT NULL DEFAULT 'CNY'",
    ],
  },
];

interface ScenarioConfig {
  id: StrategyScenarioId;
  label: string;
  strategy: 'Auto-Migration' | 'Auto-Migration + Spec' | 'Manual Migration' | 'Manual Table Rebuild';
  badgeClass: string;
  whenToUse: string;
  exampleTitle: string;
  code: string;
}

const STRATEGY_SCENARIOS: ScenarioConfig[] = [
  {
    id: 'add_column',
    label: 'Add Column w/ Default',
    strategy: 'Auto-Migration',
    badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    whenToUse:
      'Best when adding a new nullable field or non-null field with @ColumnInfo(defaultValue = "..."), or creating a brand-new @Entity table (e.g. adding grace_period_days to CreditCardEntity).',
    exampleTitle: 'CreditCardEntity.kt + AppDatabase.kt (v5 → v6)',
    code: `// 1. In CreditCardEntity.kt — ALWAYS provide defaultValue for NOT NULL columns
@ColumnInfo(name = "grace_period_days", defaultValue = "20")
val gracePeriodDays: Int = 20

// 2. In AppDatabase.kt — enable exportSchema & register AutoMigration
@Database(
    entities = [CreditCardEntity::class, TransactionEntity::class],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6)
    ]
)
abstract class AppDatabase : RoomDatabase()`,
  },
  {
    id: 'rename_delete',
    label: 'Rename / Delete Column',
    strategy: 'Auto-Migration + Spec',
    badgeClass: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30',
    whenToUse:
      'Room cannot guess whether a removed+added column is a rename or a drop+create. Pair @AutoMigration with an AutoMigrationSpec annotated with @RenameColumn or @DeleteColumn.',
    exampleTitle: 'RenameNoteToMemoSpec (TransactionEntity v5 → v6)',
    code: `@RenameColumn(
    tableName = "transactions",
    fromColumnName = "note",
    toColumnName = "memo"
)
class RenameNoteToMemoSpec : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Optional post-migration SQL hook
        db.execSQL("UPDATE transactions SET memo = TRIM(memo)")
    }
}

@Database(
    entities = [CreditCardEntity::class, TransactionEntity::class],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = RenameNoteToMemoSpec::class)
    ]
)`,
  },
  {
    id: 'backfill_data',
    label: 'Backfill / Transform Rows',
    strategy: 'Manual Migration',
    badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    whenToUse:
      'Use an explicit Migration(5, 6) object whenever schema changes require data transformation, conditional backfilling, creating indexes, or multi-step SQL updates.',
    exampleTitle: 'MIGRATION_5_6 in AppDatabase.kt',
    code: `val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Example: Add statement note & index transactions by timestamp
        db.execSQL(
            "ALTER TABLE credit_cards ADD COLUMN statement_note TEXT NOT NULL DEFAULT ''"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_transactions_date_timestamp ON transactions(date_timestamp)"
        )
        // Backfill legacy Chinese category labels to standardized keys if needed
        db.execSQL(
            "UPDATE transactions SET category = 'Repayment' WHERE category = '还款'"
        )
    }
}

// Register inside AppDatabase.getInstance(context):
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)`,
  },
  {
    id: 'rebuild_table',
    label: 'Alter Constraints / Types',
    strategy: 'Manual Table Rebuild',
    badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
    whenToUse:
      'SQLite does not support ALTER COLUMN TYPE or adding/modifying ForeignKey constraints in-place. Perform Room’s 4-step shadow table rebuild inside a manual Migration.',
    exampleTitle: '4-Step Shadow Table Swap (Zero Data Loss)',
    code: `val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create temporary table with exact new Room schema
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                card_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                note TEXT NOT NULL,
                date_timestamp INTEGER NOT NULL,
                FOREIGN KEY(card_id) REFERENCES credit_cards(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // 2. Copy all existing rows
        db.execSQL("""
            INSERT INTO transactions_new (id, card_id, amount, category, note, date_timestamp)
            SELECT id, card_id, amount, category, note, date_timestamp FROM transactions
        """.trimIndent())

        // 3. Drop old table and rename shadow table
        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // 4. Recreate ALL @Entity indices verbatim (Room validates exact index names!)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_card_id ON transactions(card_id)")
    }
}`,
  },
];

const AUTO_MIGRATION_GRADLE_SNIPPET = `// app/build.gradle.kts
plugins {
    id("com.google.devtools.ksp")
}

ksp {
    // Exports JSON schema snapshots (e.g. app/schemas/com.example...AppDatabase/5.json)
    // Required by Room to diff versions for @AutoMigration and MigrationTestHelper.
    arg("room.schemaLocation", "\$projectDir/schemas")
    arg("room.incremental", "true")
}`;

const MANUAL_MIGRATION_PATTERN_SNIPPET = `// app/src/main/java/com/example/cardmanager/core/database/AppDatabase.kt
@Database(
    entities = [CreditCardEntity::class, TransactionEntity::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(CardConverters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        // Existing chain preserved for users upgrading from v1..v5:
        // MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE credit_cards ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6
                )
                // Prefer fallbackToDestructiveMigrationOnDowngrade() in release builds
                // so missing upgrade paths crash in QA instead of silently wiping user cards.
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}`;

const MIGRATION_TEST_SNIPPET = `// app/src/androidTest/java/com/example/cardmanager/core/database/MigrationTest.kt
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDb = "vault_card_migration_test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate5To6_preservesCardsAndTransactions() {
        // 1. Create database at schema v5 and insert fixture data
        helper.createDatabase(testDb, 5).apply {
            execSQL("""
                INSERT INTO credit_cards (
                    id, bank_name, card_alias, last_4_digits, credit_limit,
                    bill_day, repayment_day, card_color_hex, card_network,
                    card_style, has_annual_fee, annual_fee_amount,
                    annual_fee_waiver_count, initial_waiver_swipes, currency_code
                ) VALUES (
                    1, '招商银行', 'Classic Platinum', '8842', 50000.0,
                    5, 25, '#1E3A8A', 'UNIONPAY', 'DEEP_OCEAN', 0, 0.0, 0, 0, 'CNY'
                )
            """.trimIndent())
            close()
        }

        // 2. Run migration 5 -> 6 and let Room validate every column, type, FK & index
        val migratedDb = helper.runMigrationsAndValidate(
            testDb,
            6,
            true,
            AppDatabase.MIGRATION_5_6
        )

        migratedDb.query("SELECT bank_name, currency_code FROM credit_cards WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("招商银行", cursor.getString(0))
            assertEquals("CNY", cursor.getString(1))
        }
    }
}`;

export const RoomMigrationsView: React.FC = () => {
  const [schemaTab, setSchemaTab] = useState<SchemaTab>('credit_cards');
  const [selectedScenario, setSelectedScenario] = useState<StrategyScenarioId>('add_column');
  const [guideTab, setGuideTab] = useState<GuideTab>('auto');
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  const copyCode = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const activeScenario =
    STRATEGY_SCENARIOS.find((s) => s.id === selectedScenario) || STRATEGY_SCENARIOS[0];
  const activeColumns =
    schemaTab === 'credit_cards' ? CREDIT_CARDS_COLUMNS : TRANSACTIONS_COLUMNS;

  return (
    <div className="space-y-6">
      {/* Hero Header */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <div className="flex flex-wrap items-center gap-2 mb-2">
              <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-mono font-semibold">
                vault_card.db
              </span>
              <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono font-semibold">
                Current Version: v5
              </span>
              <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 font-mono">
                4 Active Migrations (1→5)
              </span>
            </div>
            <h2 className="text-xl font-bold text-white flex items-center gap-2">
              <Database className="w-5 h-5 text-indigo-400 shrink-0" />
              Room Database Migrations & Schema Evolution Guide
            </h2>
            <p className="text-sm text-slate-400 mt-1 max-w-3xl">
              Step-by-step patterns for evolving VaultCard’s Room schema (<code className="text-indigo-300">AppDatabase.kt</code>) using{' '}
              <code className="text-emerald-300">@AutoMigration</code> or manual{' '}
              <code className="text-amber-300">Migration(from, to)</code> classes without losing credit card or transaction history.
            </p>
          </div>
        </div>

        {/* Destructive Fallback Callout */}
        <div className="mt-5 p-4 rounded-xl bg-amber-500/5 border border-amber-500/25 flex items-start gap-3">
          <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
          <div className="text-xs text-slate-300 leading-relaxed">
            <span className="font-semibold text-amber-300">Production Safety Note in AppDatabase.kt:</span>{' '}
            Currently <code className="text-amber-200">AppDatabase.getInstance(context)</code> chains both{' '}
            <code className="text-indigo-300">.addMigrations(MIGRATION_1_2 .. MIGRATION_4_5)</code> and{' '}
            <code className="text-amber-200">.fallbackToDestructiveMigration()</code>, with{' '}
            <code className="text-amber-200">exportSchema = false</code>. Before adopting{' '}
            <code className="text-emerald-300">@AutoMigration</code> for <code className="text-white">v6+</code>, switch{' '}
            <code className="text-emerald-300">exportSchema = true</code>, configure KSP{' '}
            <code className="text-emerald-300">room.schemaLocation</code>, and consider replacing{' '}
            <code className="text-amber-200">fallbackToDestructiveMigration()</code> with{' '}
            <code className="text-emerald-300">fallbackToDestructiveMigrationOnDowngrade()</code> in release builds so a forgotten migration path never wipes user cards.
          </div>
        </div>
      </div>

      {/* Schema Blueprint + Existing Migration Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Current Schema Inspector */}
        <div className="lg:col-span-7 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-800">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Table2 className="w-4 h-4 text-indigo-400" />
              Current Database Schema (v5)
            </h3>
            <div className="flex items-center gap-1.5 bg-slate-950 p-1 rounded-xl border border-slate-800">
              <button
                onClick={() => setSchemaTab('credit_cards')}
                className={`px-3 py-1 rounded-lg text-xs font-mono transition-colors ${
                  schemaTab === 'credit_cards'
                    ? 'bg-indigo-600 text-white font-semibold'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                credit_cards (15)
              </button>
              <button
                onClick={() => setSchemaTab('transactions')}
                className={`px-3 py-1 rounded-lg text-xs font-mono transition-colors ${
                  schemaTab === 'transactions'
                    ? 'bg-indigo-600 text-white font-semibold'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                transactions (6)
              </button>
            </div>
          </div>

          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] uppercase tracking-wider text-slate-500">
                  <th className="py-2 pr-3 font-medium">SQL Column</th>
                  <th className="py-2 pr-3 font-medium">Kotlin Field</th>
                  <th className="py-2 pr-3 font-medium">SQLite Affinity</th>
                  <th className="py-2 pr-3 font-medium">Default</th>
                  <th className="py-2 font-medium text-right">Since</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {activeColumns.map((col) => (
                  <tr key={col.column} className="hover:bg-slate-800/30">
                    <td className="py-2 pr-3 text-slate-200 font-semibold whitespace-nowrap">
                      {col.column}
                      {col.notes && (
                        <span className="ml-1.5 text-[10px] font-sans px-1.5 py-0.5 rounded bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                          {col.notes}
                        </span>
                      )}
                    </td>
                    <td className="py-2 pr-3 text-slate-400 whitespace-nowrap">
                      {col.kotlinField}: <span className="text-purple-300">{col.kotlinType}</span>
                    </td>
                    <td className="py-2 pr-3 text-emerald-300/90 whitespace-nowrap">{col.sqliteType}</td>
                    <td className="py-2 pr-3 text-amber-300 whitespace-nowrap">
                      {col.defaultVal ?? <span className="text-slate-600">—</span>}
                    </td>
                    <td className="py-2 text-right">
                      <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-300">
                        {col.addedIn}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Existing v1 -> v5 Migration History */}
        <div className="lg:col-span-5 bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
          <div className="pb-4 border-b border-slate-800">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <GitBranch className="w-4 h-4 text-emerald-400" />
              Existing AppDatabase History (v1 → v5)
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Every historical upgrade path must stay registered in <code className="text-indigo-300">addMigrations(...)</code> forever so older installs jump cleanly across versions.
            </p>
          </div>

          <div className="mt-4 space-y-3">
            {EXISTING_MIGRATIONS.map((m) => (
              <div
                key={m.id}
                className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800/90 space-y-2"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="font-mono text-xs font-bold text-indigo-300">{m.id}</span>
                  <span className="inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    v{m.from} <ArrowRight className="w-3 h-3" /> v{m.to}
                  </span>
                </div>
                <p className="text-xs text-slate-400">{m.summary}</p>
                <div className="bg-[#070b14] rounded-lg p-2.5 border border-slate-800/70 font-mono text-[11px] text-slate-300 space-y-1 overflow-x-auto">
                  {m.sql.map((stmt, idx) => (
                    <div key={idx} className="whitespace-nowrap">
                      {stmt};
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Interactive Migration Strategy Chooser */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-4 border-b border-slate-800">
          <div>
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-400" />
              Interactive Strategy Decision Guide (Preparing v5 → v6)
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Select the schema change you are planning to see whether Room can auto-migrate it or requires a manual SQL <code className="text-indigo-300">Migration</code>.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap gap-2 mt-4">
          {STRATEGY_SCENARIOS.map((scenario) => {
            const isSelected = scenario.id === selectedScenario;
            return (
              <button
                key={scenario.id}
                onClick={() => setSelectedScenario(scenario.id)}
                className={`px-3.5 py-2 rounded-xl text-xs font-medium transition-all border ${
                  isSelected
                    ? 'bg-indigo-600 text-white border-indigo-500 shadow-lg shadow-indigo-600/25'
                    : 'bg-slate-950/70 text-slate-300 border-slate-800 hover:border-slate-700 hover:text-white'
                }`}
              >
                {scenario.label}
              </button>
            );
          })}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 mt-5">
          <div className="lg:col-span-4 bg-slate-950/70 border border-slate-800 rounded-xl p-4 flex flex-col justify-between space-y-4">
            <div className="space-y-3">
              <div className="flex items-center justify-between gap-2">
                <span className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
                  Recommended Strategy
                </span>
                <span className={`text-[11px] px-2.5 py-0.5 rounded-full border font-semibold ${activeScenario.badgeClass}`}>
                  {activeScenario.strategy}
                </span>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">{activeScenario.whenToUse}</p>
            </div>

            <div className="pt-3 border-t border-slate-800/80 text-[11px] text-slate-400 space-y-1.5">
              <div className="flex items-center gap-2 text-slate-300 font-semibold">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                Room Validation Rules
              </div>
              <p>
                Room verifies exact column names, SQLite affinity (<code className="text-indigo-300">INTEGER</code>, <code className="text-indigo-300">REAL</code>, <code className="text-indigo-300">TEXT</code>), <code className="text-indigo-300">NOT NULL</code>, foreign keys, and <code className="text-indigo-300">@Index</code> names on startup.
              </p>
            </div>
          </div>

          <div className="lg:col-span-8 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
            <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
              <span className="font-mono text-xs text-slate-300">{activeScenario.exampleTitle}</span>
              <button
                onClick={() => copyCode(activeScenario.code, `scenario-${activeScenario.id}`)}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
              >
                {copiedKey === `scenario-${activeScenario.id}` ? (
                  <>
                    <Check className="w-3 h-3 text-emerald-400" />
                    <span className="text-emerald-400">Copied</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3 h-3" />
                    <span>Copy Kotlin</span>
                  </>
                )}
              </button>
            </div>
            <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed">
              <code>{activeScenario.code}</code>
            </pre>
          </div>
        </div>
      </div>

      {/* Deep-Dive Reference Tabs: Auto-Migration Setup / Manual Strategy / Instrumented Testing */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-800">
          <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
            <Layers className="w-4 h-4 text-indigo-400" />
            Implementation Playbook
          </h3>

          <div className="flex flex-wrap items-center gap-1.5 bg-slate-950 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setGuideTab('auto')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                guideTab === 'auto'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <FileJson className="w-3.5 h-3.5" />
              1. KSP Schema & Auto-Migration
            </button>
            <button
              onClick={() => setGuideTab('manual')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                guideTab === 'manual'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Wrench className="w-3.5 h-3.5" />
              2. Manual Migration & Upgrades
            </button>
            <button
              onClick={() => setGuideTab('testing')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                guideTab === 'testing'
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <FlaskConical className="w-3.5 h-3.5" />
              3. MigrationTestHelper & Checklist
            </button>
          </div>
        </div>

        {guideTab === 'auto' && (
          <div className="mt-5 grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-5 space-y-3 text-xs">
              <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                <div className="font-semibold text-white mb-1">Step 1: Export Schemas via KSP</div>
                <p className="text-slate-400 leading-relaxed">
                  Room Auto-Migration compares JSON schema snapshots between <code className="text-indigo-300">from</code> and <code className="text-indigo-300">to</code> versions. Configure <code className="text-emerald-300">room.schemaLocation</code> in <code className="text-indigo-300">app/build.gradle.kts</code> and commit the generated <code className="text-indigo-300">app/schemas/</code> JSON files to Git.
                </p>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                <div className="font-semibold text-white mb-1">Step 2: Flip exportSchema = true</div>
                <p className="text-slate-400 leading-relaxed">
                  In <code className="text-indigo-300">AppDatabase.kt</code>, change <code className="text-amber-300">exportSchema = false</code> to <code className="text-emerald-300">exportSchema = true</code> before incrementing <code className="text-white">version</code>.
                </p>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                <div className="font-semibold text-white mb-1">Step 3: Always Set @ColumnInfo(defaultValue)</div>
                <p className="text-slate-400 leading-relaxed">
                  Notice how <code className="text-indigo-300">CreditCardEntity</code> already sets <code className="text-emerald-300">defaultValue = "'CNY'"</code> and <code className="text-emerald-300">defaultValue = "0"</code> on v2–v5 columns—this same annotation lets Room generate <code className="text-indigo-300">ALTER TABLE ... DEFAULT ...</code> automatically.
                </p>
              </div>
            </div>

            <div className="lg:col-span-7 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
              <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
                <span className="font-mono text-xs text-slate-300">app/build.gradle.kts — KSP Schema Export</span>
                <button
                  onClick={() => copyCode(AUTO_MIGRATION_GRADLE_SNIPPET, 'auto-gradle')}
                  className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
                >
                  {copiedKey === 'auto-gradle' ? (
                    <>
                      <Check className="w-3 h-3 text-emerald-400" />
                      <span className="text-emerald-400">Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3 h-3" />
                      <span>Copy Gradle</span>
                    </>
                  )}
                </button>
              </div>
              <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed">
                <code>{AUTO_MIGRATION_GRADLE_SNIPPET}</code>
              </pre>
            </div>
          </div>
        )}

        {guideTab === 'manual' && (
          <div className="mt-5 grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-5 space-y-3 text-xs">
              <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                <div className="font-semibold text-white mb-1">Chaining Sequential & Jump Migrations</div>
                <p className="text-slate-400 leading-relaxed">
                  Room automatically chains sequential migrations (<code className="text-indigo-300">1→2→3→4→5→6</code>) for users upgrading across multiple skipped releases. Never delete <code className="text-indigo-300">MIGRATION_1_2</code>.. <code className="text-indigo-300">MIGRATION_4_5</code>.
                </p>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                <div className="font-semibold text-white mb-1">Exact SQLite vs Kotlin Mapping</div>
                <p className="text-slate-400 leading-relaxed">
                  <code className="text-purple-300">Boolean</code> maps to <code className="text-emerald-300">INTEGER</code> (<code className="text-amber-300">0/1</code>), <code className="text-purple-300">Double</code> maps to <code className="text-emerald-300">REAL</code>, <code className="text-purple-300">Long/Int</code> map to <code className="text-emerald-300">INTEGER</code>, and <code className="text-purple-300">CardNetwork</code> maps via <code className="text-indigo-300">CardConverters</code> to <code className="text-emerald-300">TEXT</code>.
                </p>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                <div className="font-semibold text-white mb-1">Sync JSON/WebDAV Backups</div>
                <p className="text-slate-400 leading-relaxed">
                  Remember to update <code className="text-indigo-300">BackupManager.kt</code> when adding new columns to <code className="text-indigo-300">CreditCardEntity</code> or <code className="text-indigo-300">TransactionEntity</code> with safe <code className="text-emerald-300">opt*</code> fallback defaults for older JSON backups.
                </p>
              </div>
            </div>

            <div className="lg:col-span-7 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
              <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
                <span className="font-mono text-xs text-slate-300">AppDatabase.kt — Registering MIGRATION_5_6</span>
                <button
                  onClick={() => copyCode(MANUAL_MIGRATION_PATTERN_SNIPPET, 'manual-db')}
                  className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
                >
                  {copiedKey === 'manual-db' ? (
                    <>
                      <Check className="w-3 h-3 text-emerald-400" />
                      <span className="text-emerald-400">Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3 h-3" />
                      <span>Copy Kotlin</span>
                    </>
                  )}
                </button>
              </div>
              <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed">
                <code>{MANUAL_MIGRATION_PATTERN_SNIPPET}</code>
              </pre>
            </div>
          </div>
        )}

        {guideTab === 'testing' && (
          <div className="mt-5 grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-5 space-y-2.5 text-xs">
              <div className="text-xs font-semibold text-white mb-1">Pre-Release Migration Checklist</div>
              {[
                'Increment version in @Database(version = ...) and keep all historical Migration(N, N+1) objects.',
                'Verify exportSchema = true generated schemas/com.example.cardmanager.core.database.AppDatabase/<ver>.json.',
                'Confirm every NOT NULL column added via ALTER TABLE matches @ColumnInfo(defaultValue = ...).',
                'Check all @Entity(indices = [...]) indexes exist with Room’s naming convention (index_<table>_<col>).',
                'Run MigrationTestHelper instrumentation tests against realistic CreditCardEntity + TransactionEntity rows.',
              ].map((item, i) => (
                <div
                  key={i}
                  className="p-3 rounded-xl bg-slate-950/70 border border-slate-800 flex items-start gap-2.5"
                >
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                  <span className="text-slate-300 leading-relaxed">{item}</span>
                </div>
              ))}
            </div>

            <div className="lg:col-span-7 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
              <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
                <span className="font-mono text-xs text-slate-300">androidTest — MigrationTestHelper Verification</span>
                <button
                  onClick={() => copyCode(MIGRATION_TEST_SNIPPET, 'test-helper')}
                  className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
                >
                  {copiedKey === 'test-helper' ? (
                    <>
                      <Check className="w-3 h-3 text-emerald-400" />
                      <span className="text-emerald-400">Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3 h-3" />
                      <span>Copy Test</span>
                    </>
                  )}
                </button>
              </div>
              <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed">
                <code>{MIGRATION_TEST_SNIPPET}</code>
              </pre>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
