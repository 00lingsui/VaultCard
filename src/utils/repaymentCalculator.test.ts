import test from 'node:test';
import assert from 'node:assert/strict';
import { calculateDaysUntilRepayment } from './repaymentCalculator';

test('repayment calculation when today is before repayment date in same month', () => {
  // Today is May 10, bill is May 5, repayment is May 25
  const today = new Date(2026, 4, 10); // Month 4 is May
  const result = calculateDaysUntilRepayment(5, 25, today);

  assert.equal(result.remainingDays, 15);
  assert.equal(result.nextRepaymentDate.getDate(), 25);
  assert.equal(result.nextRepaymentDate.getMonth(), 4);
  assert.equal(result.isGracePeriod, true);
});

test('repayment calculation when today is exactly repayment day', () => {
  const today = new Date(2026, 4, 25);
  const result = calculateDaysUntilRepayment(5, 25, today);

  assert.equal(result.remainingDays, 0);
  assert.equal(result.nextRepaymentDate.getDate(), 25);
  assert.equal(result.isGracePeriod, true);
});

test('repayment calculation when today is after repayment day (rolls into next month)', () => {
  // Today is May 26, repayment was May 25, next is June 25
  const today = new Date(2026, 4, 26);
  const result = calculateDaysUntilRepayment(5, 25, today);

  // May has 31 days. Remaining in May: 5 days (27, 28, 29, 30, 31) + 25 days in June = 30 days
  assert.equal(result.remainingDays, 30);
  assert.equal(result.nextRepaymentDate.getMonth(), 5); // June
  assert.equal(result.nextRepaymentDate.getDate(), 25);
});

test('cross-month settlement (Bill 20th, Repayment 10th of next month)', () => {
  // Today is May 5th, previous bill was April 20th, repayment is May 10th
  const today = new Date(2026, 4, 5);
  const result = calculateDaysUntilRepayment(20, 10, today);

  assert.equal(result.remainingDays, 5);
  assert.equal(result.nextRepaymentDate.getDate(), 10);
  assert.equal(result.nextRepaymentDate.getMonth(), 4); // May
});

test('handles short month February clamping', () => {
  // Repayment day configured as 31st, but in February 2026 (non-leap year has 28 days)
  const today = new Date(2026, 1, 15); // Feb 15
  const result = calculateDaysUntilRepayment(5, 31, today);

  assert.equal(result.nextRepaymentDate.getDate(), 28); // Clamped to Feb 28
  assert.equal(result.remainingDays, 13);
});
