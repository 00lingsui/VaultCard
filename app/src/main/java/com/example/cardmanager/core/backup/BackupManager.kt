package com.example.cardmanager.core.backup

import android.content.Context
import android.net.Uri
import com.example.cardmanager.core.database.AppDatabase
import com.example.cardmanager.core.database.entity.CreditCardEntity
import com.example.cardmanager.core.database.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor() {

    suspend fun createBackupJsonString(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val cards = db.creditCardDao().getAllCardsList()
        val transactions = db.transactionDao().getAllTransactionsList()

        val rootJson = JSONObject().apply {
            put("version", 1)
            put("export_timestamp", System.currentTimeMillis())

            val cardsArray = JSONArray()
            cards.forEach { card ->
                val cardObj = JSONObject().apply {
                    put("id", card.id)
                    put("bank_name", card.bankName)
                    put("card_alias", card.cardAlias)
                    put("last_4_digits", card.last4Digits)
                    put("credit_limit", card.creditLimit)
                    put("bill_day", card.billDay)
                    put("repayment_day", card.repaymentDay)
                    put("card_color_hex", card.cardColorHex)
                    put("card_network", card.cardNetwork.name)
                    put("card_style", card.cardStyle)
                    put("has_annual_fee", card.hasAnnualFee)
                    put("annual_fee_amount", card.annualFeeAmount)
                    put("annual_fee_waiver_count", card.annualFeeWaiverCount)
                }
                cardsArray.put(cardObj)
            }
            put("cards", cardsArray)

            val transactionsArray = JSONArray()
            transactions.forEach { tx ->
                val txObj = JSONObject().apply {
                    put("id", tx.id)
                    put("card_id", tx.cardId)
                    put("amount", tx.amount)
                    put("category", tx.category)
                    put("note", tx.note)
                    put("date_timestamp", tx.dateTimestamp)
                }
                transactionsArray.put(txObj)
            }
            put("transactions", transactionsArray)
        }
        rootJson.toString(2)
    }

    suspend fun exportBackup(context: Context, destinationUri: Uri): kotlin.Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = createBackupJsonString(context)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext kotlin.Result.failure(Exception("无法创建导出文件"))

            val rootJson = JSONObject(jsonString)
            val cardCount = rootJson.optJSONArray("cards")?.length() ?: 0
            val txCount = rootJson.optJSONArray("transactions")?.length() ?: 0
            kotlin.Result.success(cardCount + txCount)
        } catch (e: Exception) {
            e.printStackTrace()
            kotlin.Result.failure(e)
        }
    }

    suspend fun restoreFromJsonString(context: Context, jsonString: String): kotlin.Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val rootJson = JSONObject(jsonString)
            val cardsArray = rootJson.optJSONArray("cards") ?: JSONArray()
            val transactionsArray = rootJson.optJSONArray("transactions") ?: JSONArray()

            val cardsToInsert = mutableListOf<CreditCardEntity>()
            for (i in 0 until cardsArray.length()) {
                val obj = cardsArray.getJSONObject(i)
                val sanitizedLast4 = obj.optString("last_4_digits", "0000")
                    .filter { it.isDigit() }
                    .let { digits ->
                        when {
                            digits.length >= 4 -> digits.takeLast(4)
                            digits.isEmpty() -> "0000"
                            else -> digits.padStart(4, '0')
                        }
                    }
                val safeLimit = obj.optDouble("credit_limit", 1000.0)
                    .let { if (it.isNaN() || it < 0.0) 1000.0 else it }
                val safeBillDay = obj.optInt("bill_day", 1).coerceIn(1, 31)
                val safeRepaymentDay = obj.optInt("repayment_day", 15).coerceIn(1, 31)
                val safeFeeAmount = obj.optDouble("annual_fee_amount", 0.0)
                    .let { if (it.isNaN() || it < 0.0) 0.0 else it }
                val safeWaiverCount = obj.optInt("annual_fee_waiver_count", 0).coerceAtLeast(0)
                val safeColorHex = obj.optString("card_color_hex", "#1E3A8A")
                    .ifBlank { "#1E3A8A" }
                val resolvedPreset = com.example.cardmanager.core.database.entity.CardStylePreset.resolve(
                    obj.optString("card_style", ""),
                    safeColorHex
                )

                cardsToInsert.add(
                    CreditCardEntity(
                        id = obj.optLong("id", 0L),
                        bankName = obj.optString("bank_name", "未知银行").ifBlank { "未知银行" },
                        cardAlias = obj.optString("card_alias", "信用卡").ifBlank { "信用卡" },
                        last4Digits = sanitizedLast4,
                        creditLimit = safeLimit,
                        billDay = safeBillDay,
                        repaymentDay = safeRepaymentDay,
                        cardColorHex = safeColorHex,
                        cardNetwork = com.example.cardmanager.core.database.entity.CardNetwork.fromString(
                            obj.optString("card_network", "UNIONPAY")
                        ),
                        cardStyle = resolvedPreset.name,
                        hasAnnualFee = obj.optBoolean("has_annual_fee", false),
                        annualFeeAmount = safeFeeAmount,
                        annualFeeWaiverCount = safeWaiverCount
                    )
                )
            }

            val transactionsToInsert = mutableListOf<TransactionEntity>()
            for (i in 0 until transactionsArray.length()) {
                val obj = transactionsArray.getJSONObject(i)
                transactionsToInsert.add(
                    TransactionEntity(
                        id = obj.optLong("id", 0L),
                        cardId = obj.optLong("card_id", 0L),
                        amount = obj.optDouble("amount", 0.0),
                        category = obj.optString("category", "日常消费"),
                        note = obj.optString("note", ""),
                        dateTimestamp = obj.optLong("date_timestamp", System.currentTimeMillis())
                    )
                )
            }

            if (cardsToInsert.isNotEmpty()) {
                db.creditCardDao().insertCards(cardsToInsert)
            }
            if (transactionsToInsert.isNotEmpty()) {
                db.transactionDao().insertTransactions(transactionsToInsert)
            }

            kotlin.Result.success(Pair(cardsToInsert.size, transactionsToInsert.size))
        } catch (e: Exception) {
            e.printStackTrace()
            kotlin.Result.failure(e)
        }
    }

    suspend fun importBackup(context: Context, sourceUri: Uri): kotlin.Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext kotlin.Result.failure(Exception("无法打开备份文件"))

            restoreFromJsonString(context, jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            kotlin.Result.failure(e)
        }
    }
}
