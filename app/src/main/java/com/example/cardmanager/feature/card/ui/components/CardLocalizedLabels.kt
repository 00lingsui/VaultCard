package com.example.cardmanager.feature.card.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.cardmanager.R
import com.example.cardmanager.core.currency.AppCurrency
import com.example.cardmanager.core.database.entity.CardNetwork
import com.example.cardmanager.core.database.entity.CardStylePreset

@Composable
fun CardNetwork.localizedName(): String = when (this) {
    CardNetwork.UNIONPAY -> stringResource(R.string.network_unionpay)
    CardNetwork.VISA -> stringResource(R.string.network_visa)
    CardNetwork.MASTERCARD -> stringResource(R.string.network_mastercard)
    CardNetwork.AMEX -> stringResource(R.string.network_amex)
    CardNetwork.JCB -> stringResource(R.string.network_jcb)
    CardNetwork.UNIONPAY_VISA -> stringResource(R.string.network_unionpay_visa)
    CardNetwork.UNIONPAY_MASTERCARD -> stringResource(R.string.network_unionpay_mastercard)
    CardNetwork.OTHER -> stringResource(R.string.network_other)
}

@Composable
fun CardStylePreset.localizedName(): String = when (this) {
    CardStylePreset.PEARL_WHITE -> stringResource(R.string.card_style_pearl_white)
    CardStylePreset.GLACIER_MIST -> stringResource(R.string.card_style_glacier_mist)
    CardStylePreset.SAKURA_BLUSH -> stringResource(R.string.card_style_sakura_blush)
    CardStylePreset.MINT_BREEZE -> stringResource(R.string.card_style_mint_breeze)
    CardStylePreset.CHAMPAGNE_GOLD -> stringResource(R.string.card_style_champagne_gold)
    CardStylePreset.AURORA_VIOLET -> stringResource(R.string.card_style_aurora_violet)
    CardStylePreset.DEEP_OCEAN -> stringResource(R.string.card_style_deep_ocean)
    CardStylePreset.OBSIDIAN_TITANIUM -> stringResource(R.string.card_style_obsidian_titanium)
}

@Composable
fun AppCurrency.localizedName(): String = when (this) {
    AppCurrency.CNY -> stringResource(R.string.currency_cny)
    AppCurrency.USD -> stringResource(R.string.currency_usd)
    AppCurrency.HKD -> stringResource(R.string.currency_hkd)
    AppCurrency.EUR -> stringResource(R.string.currency_eur)
    AppCurrency.JPY -> stringResource(R.string.currency_jpy)
    AppCurrency.GBP -> stringResource(R.string.currency_gbp)
    AppCurrency.TWD -> stringResource(R.string.currency_twd)
    AppCurrency.KRW -> stringResource(R.string.currency_krw)
    AppCurrency.SGD -> stringResource(R.string.currency_sgd)
    AppCurrency.AUD -> stringResource(R.string.currency_aud)
    AppCurrency.CAD -> stringResource(R.string.currency_cad)
    AppCurrency.CHF -> stringResource(R.string.currency_chf)
    AppCurrency.NZD -> stringResource(R.string.currency_nzd)
    AppCurrency.SEK -> stringResource(R.string.currency_sek)
    AppCurrency.THB -> stringResource(R.string.currency_thb)
    AppCurrency.MYR -> stringResource(R.string.currency_myr)
    AppCurrency.INR -> stringResource(R.string.currency_inr)
    AppCurrency.BRL -> stringResource(R.string.currency_brl)
    AppCurrency.AED -> stringResource(R.string.currency_aed)
    AppCurrency.RUB -> stringResource(R.string.currency_rub)
}
