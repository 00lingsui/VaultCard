import React, { useState } from 'react';
import { PACKAGE_STRUCTURE_MARKDOWN } from '../data/kotlinCodebase';
import { Layers, FolderTree, Box, GitBranch, Cpu, CheckCircle2, Copy, Check } from 'lucide-react';

export const ArchitectureView: React.FC = () => {
  const [copiedStructure, setCopiedStructure] = useState<boolean>(false);

  const copyStructure = () => {
    navigator.clipboard.writeText(PACKAGE_STRUCTURE_MARKDOWN);
    setCopiedStructure(true);
    setTimeout(() => setCopiedStructure(false), 2000);
  };

  const modules = [
    {
      name: ':core:database',
      type: 'Android Library',
      description: 'Houses Room database definition, entities, DAOs, and Hilt DatabaseModule.',
      contents: ['AppDatabase.kt', 'CreditCardDao.kt', 'CreditCardEntity.kt', 'DatabaseModule.kt'],
      color: 'border-blue-500/30 bg-blue-500/5 text-blue-400',
    },
    {
      name: ':core:common',
      type: 'Kotlin/Android Library',
      description: 'Houses reusable domain models, extension functions, date utilities, and dispatchers.',
      contents: ['CreditCardExtensions.kt (calculateDaysUntilRepayment)', 'DateTimeUtils.kt'],
      color: 'border-emerald-500/30 bg-emerald-500/5 text-emerald-400',
    },
    {
      name: ':feature:card_management',
      type: 'Feature Module',
      description: 'Implements repository, use cases, ViewModels, and Jetpack Compose Material 3 screens.',
      contents: ['CreditCardRepository.kt', 'CardListViewModel.kt', 'CreditCardItem.kt', 'CardListScreen.kt'],
      color: 'border-purple-500/30 bg-purple-500/5 text-purple-400',
    },
    {
      name: ':app',
      type: 'Application Module',
      description: 'Orchestrates Hilt @HiltAndroidApp, navigation graph, and root Application class.',
      contents: ['CardManagerApp.kt', 'MainActivity.kt', 'NavGraph.kt'],
      color: 'border-amber-500/30 bg-amber-500/5 text-amber-400',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Top Architecture Overview */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <h2 className="text-xl font-bold text-white flex items-center gap-2">
          <Layers className="w-5 h-5 text-indigo-400" />
          Clean Architecture & Multi-Module Strategy
        </h2>
        <p className="text-sm text-slate-400 mt-1 max-w-3xl">
          Designed for scalable Android applications following Google's official Architecture Guidelines: strict separation of concerns, unidirectional data flow (UDF), and isolated feature modules coupled only through interfaces.
        </p>

        {/* Modules Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
          {modules.map((mod) => (
            <div
              key={mod.name}
              className={`rounded-2xl border p-4 flex flex-col justify-between ${mod.color}`}
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="font-mono text-xs font-bold">{mod.name}</span>
                  <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-900/80 border border-white/10 uppercase tracking-wider text-slate-300">
                    {mod.type}
                  </span>
                </div>
                <p className="text-xs text-slate-300 mt-2 leading-relaxed">
                  {mod.description}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-white/10">
                <span className="text-[10px] uppercase tracking-wider text-slate-400 block mb-1.5">
                  Key Artifacts:
                </span>
                <ul className="space-y-1 font-mono text-[11px] text-slate-200">
                  {mod.contents.map((c, i) => (
                    <li key={i} className="flex items-center gap-1.5 truncate">
                      <span className="w-1.5 h-1.5 rounded-full bg-current opacity-70"></span>
                      <span className="truncate">{c}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Package Structure & Gradle Config */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Package Structure Box */}
        <div className="lg:col-span-6 bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
              <FolderTree className="w-4 h-4 text-indigo-400" />
              Android Package Hierarchy
            </h3>
            <button
              onClick={copyStructure}
              className="flex items-center gap-1 text-xs text-slate-400 hover:text-indigo-400 transition-colors"
            >
              {copiedStructure ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-emerald-400">Copied</span>
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5" />
                  <span>Copy Tree</span>
                </>
              )}
            </button>
          </div>

          <div className="mt-4 font-mono text-xs text-slate-300 bg-slate-950 p-4 rounded-xl border border-slate-800/80 overflow-x-auto leading-relaxed">
            <pre>{PACKAGE_STRUCTURE_MARKDOWN.replace(/```/g, '').trim()}</pre>
          </div>
        </div>

        {/* Technical Highlights & Gradle */}
        <div className="lg:col-span-6 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-4">
          <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-300 flex items-center gap-2">
            <Cpu className="w-4 h-4 text-indigo-400" />
            Key Technical Decisions
          </h3>

          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-950/70 border border-slate-800 flex items-start gap-3">
              <CheckCircle2 className="w-4 h-4 text-indigo-400 shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-white">Kotlin Coroutines Flow over LiveData</span>
                <p className="text-slate-400 mt-0.5">
                  Room DAO returns reactive <code className="text-indigo-300">Flow&lt;List&lt;CreditCardEntity&gt;&gt;</code>. In Compose, we consume it seamlessly using <code className="text-indigo-300">collectAsStateWithLifecycle()</code>, maintaining lifecycle-awareness without coupling to AndroidX Lifecycle in the data layer.
                </p>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-950/70 border border-slate-800 flex items-start gap-3">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-white">Safe Calendar Arithmetic (java.time.LocalDate)</span>
                <p className="text-slate-400 mt-0.5">
                  Uses <code className="text-emerald-300">YearMonth.atDay(minOf(day, lengthOfMonth()))</code> to automatically prevent crashes or invalid date shifts on February 28/29 or 30-day months when a card has a 31st due date.
                </p>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-950/70 border border-slate-800 flex items-start gap-3">
              <CheckCircle2 className="w-4 h-4 text-purple-400 shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-white">Hilt Singleton Injection & KSP</span>
                <p className="text-slate-400 mt-0.5">
                  <code className="text-purple-300">DatabaseModule</code> exposes <code className="text-purple-300">@Provides @Singleton AppDatabase</code> and DAO, making repository classes easily injectable and unit-testable via mock DAOs or in-memory Room test instances.
                </p>
              </div>
            </div>
          </div>

          <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
            <div className="text-[11px] font-mono text-slate-400 mb-1">
              // core:database/build.gradle.kts
            </div>
            <pre className="text-[11px] font-mono text-indigo-300 overflow-x-auto">
{`dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-compiler:2.51")
}`}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
