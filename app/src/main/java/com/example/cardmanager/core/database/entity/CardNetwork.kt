package com.example.cardmanager.core.database.entity

enum class CardNetwork(
    val displayName: String,
    val shortLabel: String
) {
    UNIONPAY("银联", "银联"),
    VISA("VISA", "VISA"),
    MASTERCARD("万事达", "Mastercard"),
    AMEX("美国运通", "AMEX"),
    JCB("JCB", "JCB"),
    UNIONPAY_VISA("银联 + VISA", "银联·VISA"),
    UNIONPAY_MASTERCARD("银联 + 万事达", "银联·MC"),
    OTHER("其他", "CARD");

    companion object {
        fun fromString(value: String?): CardNetwork {
            if (value.isNullOrBlank()) return UNIONPAY
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNIONPAY
        }
    }
}

fun detectCardNetwork(cardNumber: String): CardNetwork {
    val digits = cardNumber.filter { it.isDigit() }
    if (digits.isEmpty()) return CardNetwork.OTHER

    // 1. VISA: starts with 4 (covers classic 4xxx and legacy VISA Electron 4026/417500/4508/4844/4913/4917)
    if (digits.startsWith("4")) {
        return CardNetwork.VISA
    }

    // 2. Mastercard: 2-digit prefix 51..55 or 4-digit prefix 2221..2720
    if (digits.length >= 2) {
        val prefix2 = digits.substring(0, 2).toIntOrNull()
        if (prefix2 != null && prefix2 in 51..55) {
            return CardNetwork.MASTERCARD
        }
    }
    if (digits.length >= 4) {
        val prefix4 = digits.substring(0, 4).toIntOrNull()
        if (prefix4 != null && prefix4 in 2221..2720) {
            return CardNetwork.MASTERCARD
        }
    }

    // 3. American Express: starts with 34 or 37
    if (digits.startsWith("34") || digits.startsWith("37")) {
        return CardNetwork.AMEX
    }

    // 4. JCB: 4-digit prefix 3528..3589 or legacy 1800 / 2131
    if (digits.startsWith("1800") || digits.startsWith("2131")) {
        return CardNetwork.JCB
    }
    if (digits.length >= 4) {
        val prefix4 = digits.substring(0, 4).toIntOrNull()
        if (prefix4 != null && prefix4 in 3528..3589) {
            return CardNetwork.JCB
        }
    }

    // 5. UnionPay: starts with 62, 81, 60 (legacy 601382/602969), or 9 (legacy domestic 9-series)
    if (digits.startsWith("62") ||
        digits.startsWith("81") ||
        digits.startsWith("60") ||
        digits.startsWith("9")
    ) {
        return CardNetwork.UNIONPAY
    }

    return CardNetwork.OTHER
}
