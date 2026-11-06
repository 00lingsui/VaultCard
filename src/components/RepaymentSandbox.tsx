import React, { useState } from 'react';
import { calculateDaysUntilRepayment } from '../utils/repaymentCalculator';
import { Calendar, Clock, AlertCircle, CheckCircle2, ArrowRight, RotateCcw } from 'lucide-react';

export const RepaymentSandbox: React.FC = () => {
  const [billDay, setBillDay] = useState<number>(5);
  const [repaymentDay, setRepaymentDay] = useState<number>(25);
  const [selectedDate, setSelectedDate] = useState<string>(() => {
    const now = new Date();
    return now.toISOString().split('T')[0];
  });

  const currentDateObj = new Date(selectedDate + 'T00:00:00');
  const result = calculateDaysUntilRepayment(billDay, repaymentDay, currentDateObj);

  const presets = [
    { name: 'Same-Month Settlement', bill: 5, repay: 25, label: 'Bill: 5th, Due: 25th' },
    { name: 'Cross-Month Settlement', bill: 20, repay: 10, label: 'Bill: 20th, Due: 10th (Next Month)' },
    { name: 'End-of-Month Clamping', bill: 1, repay: 31, label: 'Repay 31st (Clamped in 28/30-day months)' },
    { name: 'Due Tomorrow', bill: 5, repay: (currentDateObj.getDate() + 1) > 28 ? 28 : currentDateObj.getDate() + 1, label: 'Due in 1 day' },
  ];

  const getUrgencyBadge = (days: number) => {
    if (days === 0) {
      return {
        bg: 'bg-red-500/20 text-red-400 border-red-500/40',
        text: 'Due Today (0 Days)',
        icon: AlertCircle,
      };
    }
    if (days <= 3) {
      return {
        bg: 'bg-red-500/15 text-red-400 border-red-500/30',
        text: `Critical: ${days} ${days === 1 ? 'day' : 'days'} left`,
        icon: AlertCircle,
      };
    }
    if (days <= 7) {
      return {
        bg: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
        text: `Approaching: ${days} days left`,
        icon: Clock,
      };
    }
    return {
      bg: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
      text: `Comfortable: ${days} days left`,
      icon: CheckCircle2,
    };
  };

  const badge = getUrgencyBadge(result.remainingDays);
  const BadgeIcon = badge.icon;

  return (
    <div className="space-y-6">
      {/* Intro header */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-white flex items-center gap-2">
              <Clock className="w-5 h-5 text-indigo-400" />
              Repayment Calculation Engine (Live Simulator)
            </h2>
            <p className="text-sm text-slate-400 mt-1">
              Verifies the Kotlin <code className="text-indigo-300 bg-indigo-950/60 px-1.5 py-0.5 rounded text-xs">calculateDaysUntilRepayment()</code> extension logic against calendar boundaries and month transitions.
            </p>
          </div>
          <button
            onClick={() => {
              setBillDay(5);
              setRepaymentDay(25);
              setSelectedDate(new Date().toISOString().split('T')[0]);
            }}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 transition-colors w-fit"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Reset to Default
          </button>
        </div>

        {/* Quick Presets */}
        <div className="mt-5 pt-4 border-t border-slate-800/80">
          <span className="text-xs uppercase tracking-wider text-slate-400 font-semibold block mb-2">
            Quick Test Scenarios:
          </span>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2">
            {presets.map((p, idx) => (
              <button
                key={idx}
                onClick={() => {
                  setBillDay(p.bill);
                  setRepaymentDay(p.repay);
                }}
                className={`text-left p-2.5 rounded-xl border text-xs transition-all ${
                  billDay === p.bill && repaymentDay === p.repay
                    ? 'border-indigo-500 bg-indigo-500/10 text-white font-medium shadow-sm shadow-indigo-500/20'
                    : 'border-slate-800 bg-slate-900/40 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                }`}
              >
                <div className="font-semibold text-slate-200">{p.name}</div>
                <div className="text-[11px] text-slate-400 mt-0.5">{p.label}</div>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Main Interactive Controls + Results */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Controls Column */}
        <div className="lg:col-span-5 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-6">
          <h3 className="text-sm uppercase tracking-wider text-slate-400 font-semibold">
            Input Parameters
          </h3>

          {/* Reference Date Picker */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Evaluation Reference Date (Current Date)
            </label>
            <div className="relative">
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
              />
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              Represents <code className="text-slate-300">currentDate: LocalDate = LocalDate.now()</code>
            </p>
          </div>

          {/* Bill Day Slider */}
          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="text-xs font-medium text-slate-300">
                Monthly Statement Bill Day (<code className="text-indigo-300">billDay</code>)
              </label>
              <span className="text-xs font-bold px-2 py-0.5 rounded bg-indigo-950 text-indigo-300 border border-indigo-800">
                Day {billDay}
              </span>
            </div>
            <input
              type="range"
              min="1"
              max="31"
              value={billDay}
              onChange={(e) => setBillDay(Number(e.target.value))}
              className="w-full accent-indigo-500 cursor-pointer"
            />
            <div className="flex justify-between text-[10px] text-slate-400 mt-1">
              <span>Day 1</span>
              <span>Day 15</span>
              <span>Day 31</span>
            </div>
          </div>

          {/* Repayment Day Slider */}
          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="text-xs font-medium text-slate-300">
                Repayment Due Day (<code className="text-indigo-300">repaymentDay</code>)
              </label>
              <span className="text-xs font-bold px-2 py-0.5 rounded bg-indigo-950 text-indigo-300 border border-indigo-800">
                Day {repaymentDay}
              </span>
            </div>
            <input
              type="range"
              min="1"
              max="31"
              value={repaymentDay}
              onChange={(e) => setRepaymentDay(Number(e.target.value))}
              className="w-full accent-indigo-500 cursor-pointer"
            />
            <div className="flex justify-between text-[10px] text-slate-400 mt-1">
              <span>Day 1</span>
              <span>Day 15</span>
              <span>Day 31</span>
            </div>
          </div>

          {/* Formula Summary Box */}
          <div className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800/80 text-xs space-y-1.5 text-slate-300">
            <div className="font-semibold text-indigo-400 flex items-center gap-1.5">
              <span>Settlement Cycle Architecture:</span>
            </div>
            <p className="text-[11px] text-slate-400 leading-relaxed">
              {result.cycleDescription}
            </p>
          </div>
        </div>

        {/* Results & Visual Timeline Column */}
        <div className="lg:col-span-7 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col justify-between space-y-6">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h3 className="text-sm uppercase tracking-wider text-slate-400 font-semibold">
                Calculation Output
              </h3>
              <div className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium border ${badge.bg}`}>
                <BadgeIcon className="w-3.5 h-3.5" />
                {badge.text}
              </div>
            </div>

            {/* Big Metrics Display */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 mt-6">
              <div className="bg-slate-950/60 border border-slate-800 rounded-xl p-4">
                <div className="text-xs text-slate-400">Remaining Days</div>
                <div className="text-3xl font-extrabold text-white mt-1">
                  {result.remainingDays}
                  <span className="text-sm font-normal text-slate-400 ml-1">days</span>
                </div>
                <div className="text-[10px] text-indigo-400 mt-1 font-mono">
                  ChronoUnit.DAYS
                </div>
              </div>

              <div className="bg-slate-950/60 border border-slate-800 rounded-xl p-4">
                <div className="text-xs text-slate-400">Next Due Date</div>
                <div className="text-lg font-bold text-white mt-1 truncate">
                  {result.nextRepaymentDate.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric'
                  })}
                </div>
                <div className="text-[10px] text-slate-400 mt-1">
                  Day {result.nextRepaymentDate.getDate()}
                </div>
              </div>

              <div className="col-span-2 sm:col-span-1 bg-slate-950/60 border border-slate-800 rounded-xl p-4">
                <div className="text-xs text-slate-400">Grace Period</div>
                <div className="text-lg font-bold mt-1 flex items-center gap-1.5">
                  {result.isGracePeriod ? (
                    <span className="text-emerald-400 flex items-center gap-1">
                      <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                      Active
                    </span>
                  ) : (
                    <span className="text-slate-400">Pre-Billing</span>
                  )}
                </div>
                <div className="text-[10px] text-slate-400 mt-1">
                  {result.isGracePeriod ? 'Post-Statement' : 'Accrual Phase'}
                </div>
              </div>
            </div>

            {/* Timeline representation */}
            <div className="mt-8">
              <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3">
                Statement Cycle Timeline
              </h4>
              <div className="relative p-4 rounded-xl bg-slate-950/80 border border-slate-800/80">
                <div className="flex items-center justify-between text-xs text-slate-300">
                  {/* Bill Date */}
                  <div className="text-center">
                    <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300 font-bold mx-auto mb-1">
                      {result.currentBillDate.getDate()}
                    </div>
                    <span className="font-semibold text-slate-300">Bill Date</span>
                    <p className="text-[10px] text-slate-400">
                      {result.currentBillDate.toLocaleDateString('en-US', { month: 'short' })}
                    </p>
                  </div>

                  <ArrowRight className="w-4 h-4 text-slate-600" />

                  {/* Today */}
                  <div className="text-center">
                    <div className="w-8 h-8 rounded-full bg-indigo-600 border border-indigo-400 flex items-center justify-center text-white font-bold mx-auto mb-1 shadow-lg shadow-indigo-500/30">
                      {currentDateObj.getDate()}
                    </div>
                    <span className="font-semibold text-indigo-300">Evaluation Date</span>
                    <p className="text-[10px] text-slate-400">
                      {currentDateObj.toLocaleDateString('en-US', { month: 'short' })}
                    </p>
                  </div>

                  <ArrowRight className="w-4 h-4 text-slate-600" />

                  {/* Repayment Date */}
                  <div className="text-center">
                    <div className="w-8 h-8 rounded-full bg-emerald-600 border border-emerald-400 flex items-center justify-center text-white font-bold mx-auto mb-1 shadow-lg shadow-emerald-500/30">
                      {result.nextRepaymentDate.getDate()}
                    </div>
                    <span className="font-semibold text-emerald-300">Repayment Due</span>
                    <p className="text-[10px] text-slate-400">
                      {result.nextRepaymentDate.toLocaleDateString('en-US', { month: 'short' })}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Kotlin Code equivalence callout */}
          <div className="p-3 bg-indigo-950/30 border border-indigo-900/50 rounded-xl text-xs text-indigo-300">
            <span className="font-mono text-[11px] block">
              // Kotlin Equivalent Evaluation:
              <br />
              val daysRemaining: Long = card.calculateDaysUntilRepayment(LocalDate.of({currentDateObj.getFullYear()}, {currentDateObj.getMonth() + 1}, {currentDateObj.getDate()}))
              <br />
              // returns {result.remainingDays}L
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
