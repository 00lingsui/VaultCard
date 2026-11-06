import React, { useState } from 'react';
import { calculateDaysUntilRepayment } from '../utils/repaymentCalculator';
import { CreditCard, Plus, Trash2, Edit3, Activity, Check, Sparkles, X, ShieldAlert } from 'lucide-react';

export interface CreditCardData {
  id: number;
  bankName: string;
  cardAlias: string;
  last4Digits: string;
  creditLimit: number;
  billDay: number;
  repaymentDay: number;
  cardColorHex: string;
}

const INITIAL_CARDS: CreditCardData[] = [
  {
    id: 1,
    bankName: 'Chase',
    cardAlias: 'Sapphire Reserve',
    last4Digits: '4821',
    creditLimit: 20000.0,
    billDay: 5,
    repaymentDay: 25,
    cardColorHex: '#1E3A8A',
  },
  {
    id: 2,
    bankName: 'American Express',
    cardAlias: 'Gold Dining & Travel',
    last4Digits: '9002',
    creditLimit: 30000.0,
    billDay: 20,
    repaymentDay: 10,
    cardColorHex: '#B45309',
  },
  {
    id: 3,
    bankName: 'Capital One',
    cardAlias: 'Venture X Rewards',
    last4Digits: '3119',
    creditLimit: 15000.0,
    billDay: 1,
    repaymentDay: 28,
    cardColorHex: '#0F172A',
  },
];

export const CardSimulator: React.FC = () => {
  const [cards, setCards] = useState<CreditCardData[]>(INITIAL_CARDS);
  const [logs, setLogs] = useState<string[]>([
    'AppDatabase initialized: Room v2.6.1 in memory',
    'Flow<List<CreditCardEntity>> collected: 3 records emitted',
  ]);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingCard, setEditingCard] = useState<CreditCardData | null>(null);

  // Form State
  const [bankName, setBankName] = useState<string>('');
  const [cardAlias, setCardAlias] = useState<string>('');
  const [last4Digits, setLast4Digits] = useState<string>('');
  const [creditLimit, setCreditLimit] = useState<string>('5000');
  const [billDay, setBillDay] = useState<number>(1);
  const [repaymentDay, setRepaymentDay] = useState<number>(20);
  const [cardColorHex, setCardColorHex] = useState<string>('#2563EB');
  const [formError, setFormError] = useState<string>('');

  const addLog = (msg: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [`[${timestamp}] ${msg}`, ...prev.slice(0, 15)]);
  };

  const openCreateModal = () => {
    setEditingCard(null);
    setBankName('Citibank');
    setCardAlias('Double Cash');
    setLast4Digits('7712');
    setCreditLimit('8000');
    setBillDay(12);
    setRepaymentDay(7);
    setCardColorHex('#0284C7');
    setFormError('');
    setIsModalOpen(true);
  };

  const openEditModal = (card: CreditCardData) => {
    setEditingCard(card);
    setBankName(card.bankName);
    setCardAlias(card.cardAlias);
    setLast4Digits(card.last4Digits);
    setCreditLimit(card.creditLimit.toString());
    setBillDay(card.billDay);
    setRepaymentDay(card.repaymentDay);
    setCardColorHex(card.cardColorHex);
    setFormError('');
    setIsModalOpen(true);
  };

  const handleSaveCard = (e: React.FormEvent) => {
    e.preventDefault();
    if (!bankName.trim() || !cardAlias.trim()) {
      setFormError('Bank Name and Card Alias are required.');
      return;
    }
    if (!/^\d{4}$/.test(last4Digits)) {
      setFormError('last4Digits must be exactly 4 digits.');
      return;
    }
    const limitNum = parseFloat(creditLimit);
    if (isNaN(limitNum) || limitNum <= 0) {
      setFormError('Credit limit must be a positive number.');
      return;
    }
    if (billDay < 1 || billDay > 31 || repaymentDay < 1 || repaymentDay > 31) {
      setFormError('Billing day and Repayment day must be between 1 and 31.');
      return;
    }

    if (editingCard) {
      // Room Update
      const updated = cards.map((c) =>
        c.id === editingCard.id
          ? {
              ...c,
              bankName,
              cardAlias,
              last4Digits,
              creditLimit: limitNum,
              billDay,
              repaymentDay,
              cardColorHex,
            }
          : c
      );
      setCards(updated);
      addLog(`Room @Update executed: Card ID #${editingCard.id} updated -> Flow emitted new List(${updated.length})`);
    } else {
      // Room Insert
      const newId = cards.length > 0 ? Math.max(...cards.map((c) => c.id)) + 1 : 1;
      const newCard: CreditCardData = {
        id: newId,
        bankName,
        cardAlias,
        last4Digits,
        creditLimit: limitNum,
        billDay,
        repaymentDay,
        cardColorHex,
      };
      const updated = [...cards, newCard];
      setCards(updated);
      addLog(`Room @Insert executed: Card ID #${newId} inserted -> Flow emitted new List(${updated.length})`);
    }

    setIsModalOpen(false);
  };

  const handleDeleteCard = (id: number) => {
    const updated = cards.filter((c) => c.id !== id);
    setCards(updated);
    addLog(`Room @Delete executed: Card ID #${id} removed -> Flow emitted new List(${updated.length})`);
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <CreditCard className="w-5 h-5 text-indigo-400" />
            Room Database & Jetpack Compose Card Simulator
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Simulates <code className="text-indigo-300">CreditCardDao.getAllCards(): Flow&lt;List&lt;CreditCardEntity&gt;&gt;</code> collected inside a Jetpack Compose <code className="text-indigo-300">LazyColumn</code> with Material 3 styling.
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-600/30 transition-all shrink-0 w-fit"
        >
          <Plus className="w-4 h-4" />
          Add Credit Card (Room @Insert)
        </button>
      </div>

      {/* Grid: Cards UI on left, Flow stream log on right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Compose Cards Rendered */}
        <div className="lg:col-span-8 space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs uppercase tracking-wider text-slate-400 font-semibold">
              Rendered Cards ({cards.length})
            </span>
            <span className="text-xs text-slate-500 font-mono">
              @Composable fun CreditCardItem(...)
            </span>
          </div>

          {cards.length === 0 ? (
            <div className="p-12 text-center rounded-2xl bg-slate-900/40 border border-slate-800 text-slate-400">
              <CreditCard className="w-12 h-12 mx-auto text-slate-600 mb-3" />
              <p className="text-sm font-medium text-slate-300">No Credit Cards in Room DB</p>
              <p className="text-xs text-slate-500 mt-1">Click "Add Credit Card" to trigger an insert.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {cards.map((card) => {
                const now = new Date();
                const calc = calculateDaysUntilRepayment(card.billDay, card.repaymentDay, now);
                const days = calc.remainingDays;

                return (
                  <div
                    key={card.id}
                    className="relative rounded-2xl p-5 text-white shadow-xl overflow-hidden group transition-transform hover:-translate-y-1"
                    style={{
                      background: `linear-gradient(135deg, ${card.cardColorHex} 0%, #0f172a 120%)`,
                      border: '1px solid rgba(255, 255, 255, 0.15)',
                    }}
                  >
                    {/* Background Chip & Watermark Decorative */}
                    <div className="absolute right-4 bottom-4 opacity-10 font-bold text-5xl select-none uppercase font-mono pointer-events-none">
                      {card.bankName.slice(0, 4)}
                    </div>

                    {/* Top Row: Bank Name, Alias, and Due Countdown Badge */}
                    <div className="flex items-start justify-between relative z-10">
                      <div>
                        <div className="text-[11px] font-bold tracking-widest uppercase opacity-75">
                          {card.bankName}
                        </div>
                        <div className="text-base font-extrabold tracking-tight mt-0.5">
                          {card.cardAlias}
                        </div>
                      </div>

                      {/* Repayment Days Urgency Badge */}
                      <div
                        className={`text-[11px] font-semibold px-2.5 py-1 rounded-full border shadow-sm ${
                          days === 0
                            ? 'bg-red-500/90 text-white border-red-300 animate-pulse'
                            : days <= 3
                            ? 'bg-red-500/80 text-white border-red-300'
                            : days <= 7
                            ? 'bg-amber-500/80 text-white border-amber-300'
                            : 'bg-emerald-500/80 text-white border-emerald-300'
                        }`}
                      >
                        {days === 0 ? 'Due Today!' : `${days} days left`}
                      </div>
                    </div>

                    {/* Sim Card Chip */}
                    <div className="my-4 relative z-10 flex items-center gap-2">
                      <div className="w-9 h-7 rounded-md bg-amber-400/80 border border-amber-300/60 shadow-inner flex flex-col justify-around p-1">
                        <div className="w-full h-0.5 bg-amber-600/40"></div>
                        <div className="w-full h-0.5 bg-amber-600/40"></div>
                      </div>
                      <div className="text-[10px] text-white/50 tracking-widest font-mono">
                        NFC CONTACTLESS
                      </div>
                    </div>

                    {/* Middle: Masked Digits */}
                    <div className="text-lg font-mono tracking-widest text-white/95 my-1 relative z-10">
                      ••••  ••••  ••••  {card.last4Digits}
                    </div>

                    {/* Bottom Row: Credit Limit and Billing/Due Day */}
                    <div className="pt-3 mt-3 border-t border-white/10 flex items-end justify-between text-xs relative z-10">
                      <div>
                        <div className="text-[10px] uppercase tracking-wider text-white/60">
                          Credit Limit
                        </div>
                        <div className="text-sm font-bold text-white">
                          ${card.creditLimit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </div>
                      </div>

                      <div className="text-right">
                        <div className="text-[10px] uppercase tracking-wider text-white/60">
                          Bill / Due
                        </div>
                        <div className="text-xs font-semibold text-white">
                          {card.billDay}th / {card.repaymentDay}th
                        </div>
                      </div>
                    </div>

                    {/* Action overlay on hover */}
                    <div className="absolute top-2 right-2 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity bg-black/60 backdrop-blur-md rounded-lg p-1 z-20">
                      <button
                        onClick={() => openEditModal(card)}
                        className="p-1 hover:text-indigo-300 text-slate-300 transition-colors"
                        title="Edit Card (Room @Update)"
                      >
                        <Edit3 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteCard(card.id)}
                        className="p-1 hover:text-red-400 text-slate-300 transition-colors"
                        title="Delete Card (Room @Delete)"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Reactive Flow Stream Activity Log */}
        <div className="lg:col-span-4 bg-slate-900/60 border border-slate-800 rounded-2xl p-5 flex flex-col h-full">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <div className="flex items-center gap-2">
              <Activity className="w-4 h-4 text-emerald-400 animate-pulse" />
              <h3 className="text-xs uppercase tracking-wider text-slate-300 font-semibold">
                Coroutines Flow Stream
              </h3>
            </div>
            <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800">
              ACTIVE LISTENER
            </span>
          </div>

          <div className="mt-3 flex-1 overflow-y-auto space-y-2 font-mono text-[11px] max-h-[380px] pr-1">
            {logs.map((log, index) => (
              <div
                key={index}
                className="p-2 rounded-lg bg-slate-950/80 border border-slate-800/80 text-slate-300 leading-snug break-all"
              >
                {log}
              </div>
            ))}
          </div>

          <div className="mt-4 pt-3 border-t border-slate-800 text-[11px] text-slate-400">
            Jetpack Compose UI collects this stream via:
            <code className="block mt-1 p-1.5 rounded bg-slate-950 text-indigo-300">
              val cards by viewModel.cardsFlow.collectAsStateWithLifecycle(emptyList())
            </code>
          </div>
        </div>
      </div>

      {/* Modal for Add / Edit */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl relative">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <CreditCard className="w-4 h-4 text-indigo-400" />
                {editingCard ? 'Edit Credit Card' : 'Add New Credit Card'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-white transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {formError && (
              <div className="mt-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center gap-2">
                <ShieldAlert className="w-4 h-4 shrink-0" />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleSaveCard} className="mt-4 space-y-3.5 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Bank Name</label>
                  <input
                    type="text"
                    value={bankName}
                    onChange={(e) => setBankName(e.target.value)}
                    placeholder="e.g. Chase"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Card Alias</label>
                  <input
                    type="text"
                    value={cardAlias}
                    onChange={(e) => setCardAlias(e.target.value)}
                    placeholder="e.g. Sapphire Preferred"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Last 4 Digits</label>
                  <input
                    type="text"
                    maxLength={4}
                    value={last4Digits}
                    onChange={(e) => setLast4Digits(e.target.value.replace(/\D/g, ''))}
                    placeholder="4 digits"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs font-mono focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Credit Limit ($)</label>
                  <input
                    type="number"
                    step="500"
                    value={creditLimit}
                    onChange={(e) => setCreditLimit(e.target.value)}
                    placeholder="10000"
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Bill Day (1-31)</label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    value={billDay}
                    onChange={(e) => setBillDay(Number(e.target.value))}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Repayment Day (1-31)</label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    value={repaymentDay}
                    onChange={(e) => setRepaymentDay(Number(e.target.value))}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1.5">Card Color (Hex)</label>
                <div className="flex items-center gap-2">
                  {['#1E3A8A', '#0F172A', '#B45309', '#0284C7', '#4F46E5', '#059669', '#7C3AED'].map(
                    (color) => (
                      <button
                        type="button"
                        key={color}
                        onClick={() => setCardColorHex(color)}
                        className={`w-7 h-7 rounded-full border-2 transition-transform ${
                          cardColorHex === color ? 'border-white scale-110' : 'border-transparent'
                        }`}
                        style={{ backgroundColor: color }}
                      />
                    )
                  )}
                  <input
                    type="text"
                    value={cardColorHex}
                    onChange={(e) => setCardColorHex(e.target.value)}
                    className="ml-auto w-24 bg-slate-800 border border-slate-700 rounded-lg px-2 py-1 text-white text-xs font-mono"
                  />
                </div>
              </div>

              <div className="pt-4 flex items-center justify-end gap-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition-colors"
                >
                  {editingCard ? 'Save Changes' : 'Save Card to Room'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
