/**
 * Utility to calculate remaining days until the next credit card repayment date.
 * Mirrors the production Kotlin LocalDate logic.
 */

export interface RepaymentCalculationResult {
  remainingDays: number;
  nextRepaymentDate: Date;
  currentBillDate: Date;
  isGracePeriod: boolean; // Between bill day and repayment day
  cycleDescription: string;
}

export function getDaysInMonth(year: number, monthZeroIndexed: number): number {
  return new Date(year, monthZeroIndexed + 1, 0).getDate();
}

/**
 * Calculates remaining days from `currentDate` until the next repayment date.
 * Clamps days to valid month end (e.g. 31st in February -> 28th/29th).
 */
export function calculateDaysUntilRepayment(
  billDay: number,
  repaymentDay: number,
  currentDate: Date = new Date()
): RepaymentCalculationResult {
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth(); // 0-11
  const todayDate = currentDate.getDate();

  // Determine which repayment date is closest in the future (or today)
  // Let's find this month's repayment date
  const daysInCurrentMonth = getDaysInMonth(currentYear, currentMonth);
  const clampedRepaymentThisMonth = Math.min(repaymentDay, daysInCurrentMonth);
  const repaymentThisMonth = new Date(currentYear, currentMonth, clampedRepaymentThisMonth);

  // Strip time for exact day calculation
  const todayNormalized = new Date(currentYear, currentMonth, todayDate);

  let targetRepaymentDate: Date;

  if (todayNormalized.getTime() <= repaymentThisMonth.getTime()) {
    // Upcoming repayment is this month
    targetRepaymentDate = repaymentThisMonth;
  } else {
    // Passed this month's repayment date; next one is next month
    const nextMonthYear = currentMonth === 11 ? currentYear + 1 : currentYear;
    const nextMonth = (currentMonth + 1) % 12;
    const daysInNextMonth = getDaysInMonth(nextMonthYear, nextMonth);
    const clampedRepaymentNextMonth = Math.min(repaymentDay, daysInNextMonth);
    targetRepaymentDate = new Date(nextMonthYear, nextMonth, clampedRepaymentNextMonth);
  }

  // Calculate day difference
  const diffTime = targetRepaymentDate.getTime() - todayNormalized.getTime();
  const remainingDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

  // Also determine current cycle's bill statement date
  // If billDay < repaymentDay: statement issued this month on billDay
  // If billDay >= repaymentDay: statement was issued last month on billDay
  const targetYear = targetRepaymentDate.getFullYear();
  const targetMonth = targetRepaymentDate.getMonth();
  let billYear = targetYear;
  let billMonth = targetMonth;

  if (repaymentDay <= billDay) {
    // Statement is generated in previous month
    if (billMonth === 0) {
      billMonth = 11;
      billYear -= 1;
    } else {
      billMonth -= 1;
    }
  }

  const daysInBillMonth = getDaysInMonth(billYear, billMonth);
  const clampedBillDay = Math.min(billDay, daysInBillMonth);
  const currentBillDate = new Date(billYear, billMonth, clampedBillDay);

  const isGracePeriod = todayNormalized.getTime() >= currentBillDate.getTime() &&
                        todayNormalized.getTime() <= targetRepaymentDate.getTime();

  const cycleDescription = repaymentDay > billDay
    ? `Same-month settlement (Bill: ${clampedBillDay}th, Due: ${targetRepaymentDate.getDate()}th)`
    : `Cross-month settlement (Bill: ${clampedBillDay}th of prev month, Due: ${targetRepaymentDate.getDate()}th)`;

  return {
    remainingDays,
    nextRepaymentDate: targetRepaymentDate,
    currentBillDate,
    isGracePeriod,
    cycleDescription,
  };
}
