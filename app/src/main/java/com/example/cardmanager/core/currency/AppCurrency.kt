package com.example.cardmanager.core.currency

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val fractionDigits: Int
) {
    CNY("CNY", "¥", 2),
    USD("USD", "$", 2),
    HKD("HKD", "HK$", 2),
    EUR("EUR", "€", 2),
    JPY("JPY", "¥", 0),
    GBP("GBP", "£", 2),
    TWD("TWD", "NT$", 0),
    KRW("KRW", "₩", 0),
    SGD("SGD", "S$", 2),
    AUD("AUD", "A$", 2),
    CAD("CAD", "C$", 2),
    CHF("CHF", "CHF", 2),
    NZD("NZD", "NZ$", 2),
    SEK("SEK", "kr", 2),
    THB("THB", "฿", 2),
    MYR("MYR", "RM", 2),
    INR("INR", "₹", 2),
    BRL("BRL", "R$", 2),
    AED("AED", "AED", 2),
    RUB("RUB", "₽", 2);

    fun format(amount: Double, trimWholeDecimals: Boolean = false): String {
        val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        val isNegative = safeAmount < 0.0
        val absValue = kotlin.math.abs(safeAmount)
        val useDecimals = fractionDigits > 0 && !(trimWholeDecimals && absValue % 1.0 == 0.0)
        val pattern = if (useDecimals) "#,##0.00" else "#,##0"
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        val formatted = formatter.format(absValue)
        val displayPrefix = if (symbol.last().isLetter()) "$symbol " else symbol
        return if (isNegative) "-$displayPrefix$formatted" else "$displayPrefix$formatted"
    }

    companion object {
        fun fromCode(code: String?): AppCurrency {
            if (code.isNullOrBlank()) return CNY
            return entries.find { it.code.equals(code.trim(), ignoreCase = true) } ?: CNY
        }
    }
}

object CurrencyPreferenceManager {
    private const val PREFS_NAME = "vaultcard_currency_prefs"
    private const val KEY_DEFAULT_CURRENCY = "default_currency"

    private val _defaultCurrencyFlow = MutableStateFlow(AppCurrency.CNY)
    val defaultCurrencyFlow: StateFlow<AppCurrency> = _defaultCurrencyFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun getDefaultCurrency(context: Context): AppCurrency {
        if (!initialized) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_DEFAULT_CURRENCY, AppCurrency.CNY.code)
            _defaultCurrencyFlow.value = AppCurrency.fromCode(saved)
            initialized = true
        }
        return _defaultCurrencyFlow.value
    }

    fun setDefaultCurrency(context: Context, currency: AppCurrency) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEFAULT_CURRENCY, currency.code).apply()
        initialized = true
        _defaultCurrencyFlow.value = currency
    }
}
