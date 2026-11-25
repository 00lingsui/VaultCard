import React, { useState } from 'react';
import {
  ShieldCheck,
  Lock,
  Copy,
  Check,
  Database,
  Cpu,
  AlertTriangle,
  CheckCircle2,
  FileCode2,
  Wrench,
  Layers,
  Sparkles,
} from 'lucide-react';

type RuleCategoryId = 'room' | 'hilt' | 'workmanager' | 'serialization' | 'full_file';

interface RuleSection {
  id: RuleCategoryId;
  label: string;
  fileScope: string;
  badge: string;
  badgeClass: string;
  whyRequired: string;
  failureIfOmitted: string;
  rules: {
    rule: string;
    purpose: string;
  }[];
  snippet: string;
}

const RULE_SECTIONS: RuleSection[] = [
  {
    id: 'room',
    label: 'Room Database & DAOs',
    fileScope: 'AppDatabase, DAOs, Entities & CardConverters',
    badge: 'Reflection & Generated _Impl',
    badgeClass: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
    whyRequired:
      'Room instantiates AppDatabase_Impl dynamically via reflection (Room.getGeneratedImplementation) and binds query columns to @Entity fields and @TypeConverter methods at runtime.',
    failureIfOmitted:
      'RuntimeException: cannot find implementation for com.example.cardmanager.core.database.AppDatabase. AppDatabase_Impl does not exist.',
    rules: [
      {
        rule: '-keep class * extends androidx.room.RoomDatabase { *; }',
        purpose: 'Preserves AppDatabase and its KSP-generated AppDatabase_Impl subclass from renaming or removal.',
      },
      {
        rule: '-keep @androidx.room.Entity / @androidx.room.Dao',
        purpose: 'Keeps CreditCardEntity, TransactionEntity, CreditCardDao, TransactionDao, and their generated *_Impl classes intact.',
      },
      {
        rule: '-keep class ...core.database.CardConverters { *; }',
        purpose: 'Protects @TypeConverter enum adapters (fromCardNetwork / toCardNetwork) invoked by Room generated SQLite cursors.',
      },
      {
        rule: '-keepclassmembers class * { @androidx.room.* <methods>; }',
        purpose: 'Ensures @Insert, @Update, @Delete, @Query, and @TypeConverter method signatures stay intact under R8 optimization.',
      },
    ],
    snippet: `# Room Database, DAOs, Entities & TypeConverters
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.cardmanager.core.database.AppDatabase* { *; }
-keep class com.example.cardmanager.core.database.CardConverters { *; }
-keep class com.example.cardmanager.core.database.entity.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.**`,
  },
  {
    id: 'hilt',
    label: 'Hilt, Dagger & ViewModels',
    fileScope: '@HiltAndroidApp, @HiltViewModel & GeneratedComponents',
    badge: 'DI Graph & ViewModelProvider',
    badgeClass: 'bg-purple-500/10 text-purple-400 border-purple-500/30',
    whyRequired:
      'Hilt generates Dagger component interfaces, entry-point bridges, and HiltViewModel maps looked up via class tokens and ViewModelProvider.Factory reflection in Compose (hiltViewModel()).',
    failureIfOmitted:
      'IllegalStateException or ClassNotFoundException when MainActivity / SettingsScreen resolves CardListViewModel or SettingsViewModel.',
    rules: [
      {
        rule: '-keep class * extends androidx.lifecycle.ViewModel { *; }',
        purpose: 'Preserves CardListViewModel, SettingsViewModel, and AuthViewModel constructors and StateFlow fields.',
      },
      {
        rule: '-keep interface * extends dagger.hilt.internal.GeneratedComponent',
        purpose: 'Protects Hilt singleton and activity component interfaces used by EntryPoints.get().',
      },
      {
        rule: '-keep @dagger.hilt.android.HiltAndroidApp / @AndroidEntryPoint / @HiltViewModel',
        purpose: 'Safeguards CardManagerApp, MainActivity, and injected ViewModels from R8 dead-code elimination or constructor inlining.',
      },
      {
        rule: '-keep class javax.inject.** { *; }',
        purpose: 'Retains @Inject / Provider<T> annotations and metadata expected by Dagger factories.',
      },
    ],
    snippet: `# Hilt, Dagger & ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keep interface * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class javax.inject.** { *; }`,
  },
  {
    id: 'workmanager',
    label: 'WorkManager & @HiltWorker',
    fileScope: 'RepaymentReminderWorker & HiltWorkerFactory',
    badge: 'AssistedInject Constructors',
    badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    whyRequired:
      'WorkManager resolves Worker classes by fully qualified class name stored in SQLite (androidx.work.impl.WorkDatabase) and instantiates them dynamically via HiltWorkerFactory.',
    failureIfOmitted:
      'NoSuchMethodException or ClassNotFoundException in release APKs when WorkManager triggers periodic or one-time RepaymentReminderWorker jobs.',
    rules: [
      {
        rule: '-keep @androidx.hilt.work.HiltWorker class * { *; }',
        purpose: 'Prevents R8 from obfuscating RepaymentReminderWorker whose name is persisted in WorkManager’s job table.',
      },
      {
        rule: '@dagger.assisted.AssistedInject <init>(...);',
        purpose: 'Keeps the AssistedInject constructor (Context, WorkerParameters, DAOs, ReminderPreferenceManager) callable by HiltWorkerFactory.',
      },
      {
        rule: '-keep class androidx.hilt.work.** { *; }',
        purpose: 'Retains HiltWorkerFactory and WorkerAssistedFactory binding classes.',
      },
      {
        rule: '-keep class com.example.cardmanager.core.notification.** { *; }',
        purpose: 'Protects notification channel helpers, ReminderSettings, and RepaymentReminderWorker.',
      },
    ],
    snippet: `# WorkManager & Hilt Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }
-keep class androidx.hilt.work.** { *; }
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
    @dagger.assisted.Assisted <init>(...);
}
-keep class com.example.cardmanager.core.notification.** { *; }`,
  },
  {
    id: 'serialization',
    label: 'Enums, Backups & Biometric',
    fileScope: 'AppCurrency, AppLanguage, BackupManager & BiometricPrompt',
    badge: 'Enum.values() & Crypto',
    badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    whyRequired:
      'JSON backup export/import, currency resolution, language switching, and AndroidX BiometricPrompt rely on enum values()/valueOf(), entity field names, and platform crypto reflection.',
    failureIfOmitted:
      'Corrupted or unreadable JSON/WebDAV backups or NoSuchMethodException on enum deserialization in release builds.',
    rules: [
      {
        rule: '-keepclassmembers enum * { values(); valueOf(String); }',
        purpose: 'Ensures Kotlin enum lookup methods survive R8 enum unboxing / aggressive shrinking.',
      },
      {
        rule: '-keep class ...core.currency.AppCurrency / ...AppLanguage',
        purpose: 'Keeps persisted currency codes (CNY, USD, HKD...) and language tags intact across app updates.',
      },
      {
        rule: '-keep class ...core.backup.** { *; }',
        purpose: 'Preserves BackupManager serialization contracts for local JSON & WebDAV cloud sync.',
      },
      {
        rule: '-keep class androidx.biometric.** { *; }',
        purpose: 'Protects BiometricPrompt callbacks and prompt hardware capability bridges.',
      },
    ],
    snippet: `# Backup, Serialization & Enums
-keep class com.example.cardmanager.core.backup.** { *; }
-keep class com.example.cardmanager.core.currency.AppCurrency { *; }
-keep class com.example.cardmanager.core.language.AppLanguage { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Biometric & Crypto
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**`,
  },
  {
    id: 'full_file',
    label: 'Full proguard-rules.pro',
    fileScope: 'app/proguard-rules.pro (57 lines)',
    badge: 'Complete Release Config',
    badgeClass: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30',
    whyRequired:
      'Complete production R8 ruleset shipped in VaultCard’s release APK build pipeline (isMinifyEnabled = true & isShrinkResources = true).',
    failureIfOmitted:
      'Omitted rules can trigger either R8 build errors or silent runtime crashes strictly in assembleRelease builds.',
    rules: [
      {
        rule: 'proguard-android-optimize.txt + proguard-rules.pro',
        purpose: 'Combines Android’s standard R8 optimization rules with VaultCard’s Room, Hilt, WorkManager, and Biometric keeps.',
      },
      {
        rule: 'Scoped -dontwarn directives',
        purpose: 'Suppresses harmless compile-only annotation warnings (javax.annotation, org.jetbrains, kotlin.Metadata) without hiding real app errors.',
      },
    ],
    snippet: `# Room Database, DAOs, Entities & TypeConverters
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.cardmanager.core.database.AppDatabase* { *; }
-keep class com.example.cardmanager.core.database.CardConverters { *; }
-keep class com.example.cardmanager.core.database.entity.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.**

# Backup, Serialization & Enums
-keep class com.example.cardmanager.core.backup.** { *; }
-keep class com.example.cardmanager.core.currency.AppCurrency { *; }
-keep class com.example.cardmanager.core.language.AppLanguage { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# WorkManager & Hilt Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }
-keep class androidx.hilt.work.** { *; }
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
    @dagger.assisted.Assisted <init>(...);
}
-keep class com.example.cardmanager.core.notification.** { *; }

# Hilt, Dagger & ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keep interface * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class javax.inject.** { *; }

# Biometric & Crypto
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Suppress harmless missing optional class/annotation warnings under R8
-dontwarn androidx.**
-dontwarn dagger.**
-dontwarn com.google.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.**
-dontwarn kotlin.**
-dontwarn kotlinx.**`,
  },
];

const GRADLE_RELEASE_SNIPPET = `// app/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true      // Enables R8 code shrinking, obfuscation & optimization
            isShrinkResources = true    // Removes unused drawables/strings after R8 pass
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }
}`;

const TROUBLESHOOTING_ITEMS = [
  {
    symptom: 'AppDatabase_Impl does not exist (Release Only)',
    cause: 'R8 stripped or renamed the KSP-generated AppDatabase_Impl class looked up by Room via Class.forName().',
    fix: 'Ensure both `-keep class * extends androidx.room.RoomDatabase { *; }` and `-keep class com.example.cardmanager.core.database.AppDatabase* { *; }` are present.',
  },
  {
    symptom: 'Cannot create an instance of class ...ViewModel',
    cause: 'R8 inlined or obfuscated the @HiltViewModel constructor or Hilt GeneratedComponent interfaces.',
    fix: 'Verify `-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }` and `-keep class * extends androidx.lifecycle.ViewModel { *; }`.',
  },
  {
    symptom: 'WorkManager Could not instantiate RepaymentReminderWorker',
    cause: 'R8 removed the @AssistedInject constructor because only HiltWorkerFactory invokes it reflectively.',
    fix: 'Keep `@dagger.assisted.AssistedInject <init>(...);` and `@androidx.hilt.work.HiltWorker class * { *; }`.',
  },
  {
    symptom: 'Debug vs. Release Behavior Drift',
    cause: 'Debug builds set `isMinifyEnabled = false`, hiding reflection/obfuscation issues until CI runs `./gradlew assembleRelease`.',
    fix: 'Test release APKs locally or download CI artifacts (`VaultCard-v*-arm64-v8a-release.apk`) and preserve `mapping.txt` to de-obfuscate stack traces.',
  },
];

export const SecurityView: React.FC = () => {
  const [activeSectionId, setActiveSectionId] = useState<RuleCategoryId>('room');
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  const copyCode = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const activeSection = RULE_SECTIONS.find((s) => s.id === activeSectionId) || RULE_SECTIONS[0];

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-wrap items-center gap-2 mb-2">
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-mono font-semibold">
            R8 Full Mode
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono font-semibold">
            isMinifyEnabled = true
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/20 font-mono font-semibold">
            isShrinkResources = true
          </span>
          <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 font-mono">
            proguard-android-optimize.txt
          </span>
        </div>

        <h2 className="text-xl font-bold text-white flex items-center gap-2">
          <ShieldCheck className="w-5 h-5 text-indigo-400 shrink-0" />
          Security & R8 / ProGuard Shrinking Guide (Room + Hilt + WorkManager)
        </h2>
        <p className="text-sm text-slate-400 mt-1 max-w-3xl">
          How VaultCard configures <code className="text-indigo-300">app/proguard-rules.pro</code> so R8 safely shrinks unused bytecode and obfuscates release APKs (~18MB per-ABI) without breaking{' '}
          <code className="text-blue-300">Room</code> reflection,{' '}
          <code className="text-purple-300">Hilt</code> DI graphs,{' '}
          <code className="text-emerald-300">@HiltWorker</code> assisted injection, or{' '}
          <code className="text-amber-300">BiometricPrompt</code> authentication.
        </p>

        {/* 4 Summary Pillars */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
          <div className="rounded-xl border border-blue-500/25 bg-blue-500/5 p-4">
            <div className="flex items-center gap-2 text-blue-300 text-xs font-bold mb-1.5">
              <Database className="w-4 h-4" />
              Room Reflection Safety
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Keeps <code className="text-blue-200">AppDatabase_Impl</code>,{' '}
              <code className="text-blue-200">@Entity</code> fields,{' '}
              <code className="text-blue-200">@Dao</code> queries, and{' '}
              <code className="text-blue-200">CardConverters</code>.
            </p>
          </div>

          <div className="rounded-xl border border-purple-500/25 bg-purple-500/5 p-4">
            <div className="flex items-center gap-2 text-purple-300 text-xs font-bold mb-1.5">
              <Cpu className="w-4 h-4" />
              Hilt & ViewModels
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Preserves <code className="text-purple-200">@HiltAndroidApp</code>,{' '}
              <code className="text-purple-200">@HiltViewModel</code>, and{' '}
              <code className="text-purple-200">GeneratedComponent</code> bindings.
            </p>
          </div>

          <div className="rounded-xl border border-emerald-500/25 bg-emerald-500/5 p-4">
            <div className="flex items-center gap-2 text-emerald-300 text-xs font-bold mb-1.5">
              <Wrench className="w-4 h-4" />
              @HiltWorker Constructors
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Retains <code className="text-emerald-200">@AssistedInject</code> constructors on{' '}
              <code className="text-emerald-200">RepaymentReminderWorker</code> for background execution.
            </p>
          </div>

          <div className="rounded-xl border border-amber-500/25 bg-amber-500/5 p-4">
            <div className="flex items-center gap-2 text-amber-300 text-xs font-bold mb-1.5">
              <Lock className="w-4 h-4" />
              Enums & Biometrics
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Protects <code className="text-amber-200">AppCurrency</code>,{' '}
              <code className="text-amber-200">AppLanguage</code>, JSON backups, and{' '}
              <code className="text-amber-200">androidx.biometric</code>.
            </p>
          </div>
        </div>
      </div>

      {/* Interactive ProGuard / R8 Rule Explorer */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-4 border-b border-slate-800">
          <div>
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-400" />
              1. Interactive R8 / ProGuard Rule Breakdown
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Select a layer to inspect why R8 would otherwise break it, exact keep directives, and copyable rules from <code className="text-indigo-300">app/proguard-rules.pro</code>.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap gap-2 mt-4">
          {RULE_SECTIONS.map((sec) => {
            const isSelected = sec.id === activeSectionId;
            return (
              <button
                key={sec.id}
                onClick={() => setActiveSectionId(sec.id)}
                className={`px-3.5 py-2 rounded-xl text-xs font-medium transition-all border ${
                  isSelected
                    ? 'bg-indigo-600 text-white border-indigo-500 shadow-lg shadow-indigo-600/25'
                    : 'bg-slate-950/70 text-slate-300 border-slate-800 hover:border-slate-700 hover:text-white'
                }`}
              >
                {sec.label}
              </button>
            );
          })}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 mt-5">
          {/* Left Explanation Column */}
          <div className="lg:col-span-5 space-y-4">
            <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-4 space-y-3">
              <div className="flex items-center justify-between gap-2">
                <span className="text-[11px] font-mono text-slate-400">{activeSection.fileScope}</span>
                <span className={`text-[10px] px-2 py-0.5 rounded-full border font-semibold ${activeSection.badgeClass}`}>
                  {activeSection.badge}
                </span>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">{activeSection.whyRequired}</p>

              <div className="p-3 rounded-lg bg-rose-500/5 border border-rose-500/20 text-xs">
                <div className="text-[10px] font-mono uppercase tracking-wider text-rose-400 font-semibold mb-1">
                  Failure Without Rules
                </div>
                <div className="font-mono text-[11px] text-rose-200/90 leading-relaxed">
                  {activeSection.failureIfOmitted}
                </div>
              </div>
            </div>

            <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-4 space-y-2.5">
              <div className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
                Directive-by-Directive Purpose
              </div>
              {activeSection.rules.map((r, idx) => (
                <div key={idx} className="p-2.5 rounded-lg bg-[#070b14] border border-slate-800/80 space-y-1">
                  <div className="font-mono text-[11px] text-indigo-300 break-all">{r.rule}</div>
                  <div className="text-xs text-slate-400 leading-relaxed">{r.purpose}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Right ProGuard Snippet Column */}
          <div className="lg:col-span-7 bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden flex flex-col">
            <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
              <span className="font-mono text-xs text-slate-300">app/proguard-rules.pro — {activeSection.label}</span>
              <button
                onClick={() => copyCode(activeSection.snippet, `sec-${activeSection.id}`)}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
              >
                {copiedKey === `sec-${activeSection.id}` ? (
                  <>
                    <Check className="w-3 h-3 text-emerald-400" />
                    <span className="text-emerald-400">Copied</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3 h-3" />
                    <span>Copy Rules</span>
                  </>
                )}
              </button>
            </div>
            <pre className="p-4 font-mono text-xs text-slate-200 overflow-x-auto leading-relaxed flex-1 max-h-[540px] overflow-y-auto">
              <code>{activeSection.snippet}</code>
            </pre>
          </div>
        </div>
      </div>

      {/* Release Pipeline & Troubleshooting */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Gradle Release Shrinking Config */}
        <div className="lg:col-span-6 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="pb-3 border-b border-slate-800 flex items-center justify-between">
            <div>
              <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
                <FileCode2 className="w-4 h-4 text-indigo-400" />
                2. Release Build Shrinking & Resource Optimization
              </h3>
              <p className="text-xs text-slate-400 mt-1">
                R8 runs automatically during <code className="text-indigo-300">./gradlew assembleRelease</code> alongside per-ABI APK splits.
              </p>
            </div>
          </div>

          <div className="bg-[#0b0f19] border border-slate-800 rounded-xl overflow-hidden">
            <div className="px-4 py-2.5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
              <span className="font-mono text-xs text-slate-300">app/build.gradle.kts</span>
              <button
                onClick={() => copyCode(GRADLE_RELEASE_SNIPPET, 'gradle-release')}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition-colors"
              >
                {copiedKey === 'gradle-release' ? (
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
              <code>{GRADLE_RELEASE_SNIPPET}</code>
            </pre>
          </div>

          <div className="p-3.5 rounded-xl bg-emerald-500/5 border border-emerald-500/20 flex items-start gap-2.5 text-xs">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <div className="text-slate-300 leading-relaxed">
              <span className="font-semibold text-emerald-300">Safe Resource Shrinking:</span> All UI strings, notification channels, and drawable launcher icons in VaultCard are referenced statically via <code className="text-indigo-300">R.string.*</code> / <code className="text-indigo-300">R.drawable.*</code> (no <code className="text-amber-300">Resources.getIdentifier()</code>), so <code className="text-emerald-300">isShrinkResources = true</code> safely strips dead assets without <code className="text-indigo-300">keep.xml</code> overrides.
            </div>
          </div>
        </div>

        {/* Common Release Shrinking Pitfalls */}
        <div className="lg:col-span-6 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="pb-3 border-b border-slate-800">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-400" />
              3. Release Obfuscation Diagnostics
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              Quick symptom-to-fix reference if a feature works in Debug but fails in minified Release APKs.
            </p>
          </div>

          <div className="space-y-3 text-xs">
            {TROUBLESHOOTING_ITEMS.map((item, idx) => (
              <div
                key={idx}
                className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-1.5"
              >
                <div className="font-semibold text-white flex items-center gap-2">
                  <Layers className="w-3.5 h-3.5 text-indigo-400 shrink-0" />
                  {item.symptom}
                </div>
                <p className="text-slate-400 leading-relaxed">{item.cause}</p>
                <div className="pt-1 text-emerald-300/90 leading-relaxed">
                  <span className="font-mono text-[10px] uppercase tracking-wider text-emerald-400 mr-1.5">
                    Fix:
                  </span>
                  {item.fix}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
