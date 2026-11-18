package com.example.cardmanager.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class BankBrand(
    val displayNameZh: String,
    val displayNameEn: String,
    val primaryColor: Color,
    val secondaryColor: Color = primaryColor,
    val hasCoreVectorEmblem: Boolean = false
) {
    // Core 16 Banks with Official Geometric Vector Emblems
    ICBC("工商银行", "ICBC", Color(0xFFC7000B), Color(0xFFA00008), hasCoreVectorEmblem = true),
    BOC("中国银行", "BOC", Color(0xFFB81C22), Color(0xFF8C1418), hasCoreVectorEmblem = true),
    CCB("建设银行", "CCB", Color(0xFF003B8F), Color(0xFF0055B8), hasCoreVectorEmblem = true),
    ABC("农业银行", "ABC", Color(0xFF009174), Color(0xFF006F59), hasCoreVectorEmblem = true),
    CMB("招商银行", "CMB", Color(0xFFC8102E), Color(0xFFF59E0B), hasCoreVectorEmblem = true),
    BOCOM("交通银行", "BOCOM", Color(0xFF003670), Color(0xFF0284C7), hasCoreVectorEmblem = true),
    PSBC("邮储银行", "PSBC", Color(0xFF007F3E), Color(0xFF059669), hasCoreVectorEmblem = true),
    CITIC("中信银行", "CITIC", Color(0xFFD7000F), Color(0xFF991B1B), hasCoreVectorEmblem = true),
    PAB("平安银行", "PAB", Color(0xFFFA6400), Color(0xFFE05200), hasCoreVectorEmblem = true),
    CIB("兴业银行", "CIB", Color(0xFF004098), Color(0xFF0284C7), hasCoreVectorEmblem = true),
    SPDB("浦发银行", "SPDB", Color(0xFF003366), Color(0xFF2563EB), hasCoreVectorEmblem = true),
    HSBC("汇丰银行", "HSBC", Color(0xFFDB0011), Color(0xFF991B1B), hasCoreVectorEmblem = true),
    CITI("花旗银行", "Citi", Color(0xFF003B70), Color(0xFFD9261C), hasCoreVectorEmblem = true),
    DBS("星展银行", "DBS", Color(0xFFEE1C25), Color(0xFF1E293B), hasCoreVectorEmblem = true),
    STANDARD_CHARTERED("渣打银行", "Standard Chartered", Color(0xFF0072AA), Color(0xFF21AA47), hasCoreVectorEmblem = true),
    CHASE("摩根大通", "Chase", Color(0xFF117ACA), Color(0xFF0B4F8A), hasCoreVectorEmblem = true),

    // Additional Recognized Regional & Global Banks (Rendered as Brand-Tinted Monogram Seal Badges)
    CEB("光大银行", "CEB", Color(0xFF6B2D8B), Color(0xFFF59E0B)),
    CMBC("民生银行", "CMBC", Color(0xFF007B85), Color(0xFF10B981)),
    CGB("广发银行", "CGB", Color(0xFFC8161D), Color(0xFFE11D48)),
    BOSHANG("上海银行", "Bank of Shanghai", Color(0xFFC89B3C), Color(0xFF1E3A8A)),
    BOBEIJING("北京银行", "Bank of Beijing", Color(0xFFD62828), Color(0xFF9B1C1C)),
    BOA("美国银行", "Bank of America", Color(0xFF012169), Color(0xFFE31837)),
    WELLS_FARGO("富国银行", "Wells Fargo", Color(0xFFD71E28), Color(0xFFFBBF24)),
    CAPITAL_ONE("Capital One", "Capital One", Color(0xFF004879), Color(0xFFD03027)),
    OCBC("华侨银行", "OCBC", Color(0xFFE4002B), Color(0xFF9F1239)),
    UOB("大华银行", "UOB", Color(0xFF00377B), Color(0xFFED1C24)),
    BARCLAYS("巴克莱银行", "Barclays", Color(0xFF00AEEF), Color(0xFF00395D)),
    SANTANDER("桑坦德银行", "Santander", Color(0xFFEC0000), Color(0xFFB91C1C)),
    MUFG("三菱UFJ银行", "MUFG", Color(0xFFE60012), Color(0xFF6B7280)),
    SMBC("三井住友银行", "SMBC", Color(0xFF00A847), Color(0xFF004831));

    companion object {
        val popularQuickBanks: List<BankBrand> = listOf(
            CMB, ICBC, CCB, ABC, BOC, BOCOM, CITIC, PAB, CIB, SPDB, HSBC, CITI, CHASE, DBS
        )
    }
}

object BankBrandMatcher {

    private data class BankRule(
        val brand: BankBrand,
        val substrings: List<String> = emptyList(),
        val wordTokens: List<String> = emptyList()
    )

    // Order is tuned so specific multi-character aliases match before short abbreviations
    // (e.g., BOCOM before BOC, CMBC before CMB, Bank of Shanghai before generic terms).
    private val rules: List<BankRule> = listOf(
        // China
        BankRule(
            brand = BankBrand.ICBC,
            substrings = listOf("工商", "工行", "industrial and commercial bank"),
            wordTokens = listOf("icbc")
        ),
        BankRule(
            brand = BankBrand.CCB,
            substrings = listOf("建设", "建行", "china construction bank"),
            wordTokens = listOf("ccb")
        ),
        BankRule(
            brand = BankBrand.ABC,
            substrings = listOf("农业", "农行", "agricultural bank"),
            wordTokens = listOf("abc", "abchina")
        ),
        BankRule(
            brand = BankBrand.BOCOM,
            substrings = listOf("交通", "交行", "bank of communications"),
            wordTokens = listOf("bocom", "bankcomm")
        ),
        BankRule(
            brand = BankBrand.BOC,
            substrings = listOf("中国银行", "中行", "bank of china"),
            wordTokens = listOf("boc")
        ),
        BankRule(
            brand = BankBrand.CMBC,
            substrings = listOf("民生", "china minsheng"),
            wordTokens = listOf("cmbc")
        ),
        BankRule(
            brand = BankBrand.CMB,
            substrings = listOf("招商", "招行", "china merchants"),
            wordTokens = listOf("cmb", "cmbchina")
        ),
        BankRule(
            brand = BankBrand.PSBC,
            substrings = listOf("邮储", "邮政", "postal savings"),
            wordTokens = listOf("psbc")
        ),
        BankRule(
            brand = BankBrand.CITIC,
            substrings = listOf("中信", "citic"),
            wordTokens = listOf("citic")
        ),
        BankRule(
            brand = BankBrand.CEB,
            substrings = listOf("光大", "everbright"),
            wordTokens = listOf("ceb")
        ),
        BankRule(
            brand = BankBrand.SPDB,
            substrings = listOf("浦发", "浦东发展", "pudong development"),
            wordTokens = listOf("spdb")
        ),
        BankRule(
            brand = BankBrand.CIB,
            substrings = listOf("兴业", "industrial bank"),
            wordTokens = listOf("cib")
        ),
        BankRule(
            brand = BankBrand.PAB,
            substrings = listOf("平安", "ping an", "pingan"),
            wordTokens = listOf("pab")
        ),
        BankRule(
            brand = BankBrand.CGB,
            substrings = listOf("广发", "guangfa"),
            wordTokens = listOf("cgb", "gdb")
        ),
        BankRule(
            brand = BankBrand.BOSHANG,
            substrings = listOf("上海银行", "bank of shanghai", "沪银"),
            wordTokens = listOf("bosh", "boshang", "bos")
        ),
        BankRule(
            brand = BankBrand.BOBEIJING,
            substrings = listOf("北京银行", "bank of beijing", "京银"),
            wordTokens = listOf("bobeijing", "bob")
        ),

        // Global / Regional
        BankRule(
            brand = BankBrand.HSBC,
            substrings = listOf("汇丰", "匯豐", "hsbc"),
            wordTokens = listOf("hsbc")
        ),
        BankRule(
            brand = BankBrand.CITI,
            substrings = listOf("花旗", "citibank", "citicorp"),
            wordTokens = listOf("citi")
        ),
        BankRule(
            brand = BankBrand.CHASE,
            substrings = listOf("摩根大通", "大通", "jpmorgan", "jp morgan", "chase"),
            wordTokens = listOf("chase", "jpm")
        ),
        BankRule(
            brand = BankBrand.BOA,
            substrings = listOf("美国银行", "美國銀行", "bank of america", "bofa"),
            wordTokens = listOf("boa", "bofa", "bac")
        ),
        BankRule(
            brand = BankBrand.WELLS_FARGO,
            substrings = listOf("富国", "富國", "wells fargo"),
            wordTokens = listOf("wf", "wellsfargo")
        ),
        BankRule(
            brand = BankBrand.CAPITAL_ONE,
            substrings = listOf("capital one", "capitalone", "第一资本"),
            wordTokens = listOf("cof")
        ),
        BankRule(
            brand = BankBrand.DBS,
            substrings = listOf("星展", "dbs"),
            wordTokens = listOf("dbs")
        ),
        BankRule(
            brand = BankBrand.OCBC,
            substrings = listOf("华侨银行", "華僑銀行", "华侨", "ocbc"),
            wordTokens = listOf("ocbc")
        ),
        BankRule(
            brand = BankBrand.UOB,
            substrings = listOf("大华银行", "大華銀行", "大华", "united overseas"),
            wordTokens = listOf("uob")
        ),
        BankRule(
            brand = BankBrand.STANDARD_CHARTERED,
            substrings = listOf("渣打", "standard chartered"),
            wordTokens = listOf("scb", "stanchart")
        ),
        BankRule(
            brand = BankBrand.BARCLAYS,
            substrings = listOf("巴克莱", "巴克萊", "barclays"),
            wordTokens = listOf("barclays", "barc")
        ),
        BankRule(
            brand = BankBrand.SANTANDER,
            substrings = listOf("桑坦德", "santander"),
            wordTokens = listOf("santander", "san")
        ),
        BankRule(
            brand = BankBrand.MUFG,
            substrings = listOf("三菱", "mitsubishi ufj", "mufg"),
            wordTokens = listOf("mufg")
        ),
        BankRule(
            brand = BankBrand.SMBC,
            substrings = listOf("三井住友", "sumitomo mitsui", "smbc"),
            wordTokens = listOf("smbc")
        )
    )

    private val tokenSplitRegex = Regex("[^\\p{L}\\p{Nd}]+")

    fun match(bankName: String): BankBrand? {
        val normalized = bankName.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) return null

        val tokens = normalized.split(tokenSplitRegex).filter { it.isNotEmpty() }.toSet()
        for (rule in rules) {
            if (rule.substrings.any { normalized.contains(it) }) {
                return rule.brand
            }
            if (rule.wordTokens.any { it in tokens }) {
                return rule.brand
            }
        }
        return null
    }

    fun extractMonogram(bankName: String): String {
        val cleaned = bankName.trim()
        if (cleaned.isEmpty()) return "BK"

        val strippedZh = cleaned
            .replace("股份有限公司", "")
            .replace("银行", "")
            .replace("銀行", "")
            .trim()
        val candidate = strippedZh.ifEmpty { cleaned }

        val firstCjk = candidate.firstOrNull { ch ->
            Character.UnicodeScript.of(ch.code) == Character.UnicodeScript.HAN
        }
        if (firstCjk != null) {
            return firstCjk.toString()
        }

        val ignoreWords = setOf("bank", "of", "the", "and", "co", "ltd", "inc")
        val words = candidate.split(tokenSplitRegex)
            .filter { it.isNotBlank() && it.lowercase(Locale.ROOT) !in ignoreWords }
        if (words.size >= 2) {
            return "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
        }
        val single = words.firstOrNull() ?: candidate.filter { it.isLetterOrDigit() }
        return if (single.length >= 2) {
            single.take(2).uppercase(Locale.ROOT)
        } else if (single.isNotEmpty()) {
            single.uppercase(Locale.ROOT)
        } else {
            "BK"
        }
    }
}

@Composable
fun BankBrandBadge(
    bankName: String,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    monochromeTint: Color? = null,
    showContainer: Boolean = true
) {
    val brand = remember(bankName) { BankBrandMatcher.match(bankName) }
    val monogram = remember(bankName) { BankBrandMatcher.extractMonogram(bankName) }

    if (brand != null && brand.hasCoreVectorEmblem) {
        val primary = monochromeTint ?: brand.primaryColor
        val secondary = monochromeTint?.copy(alpha = 0.78f) ?: brand.secondaryColor

        val containerMod = if (showContainer) {
            val containerBg = if (monochromeTint != null) {
                Color.Transparent
            } else {
                Color.White.copy(alpha = 0.96f)
            }
            val borderColor = if (monochromeTint != null) {
                monochromeTint.copy(alpha = 0.28f)
            } else {
                brand.primaryColor.copy(alpha = 0.22f)
            }
            modifier
                .size(size)
                .clip(CircleShape)
                .background(containerBg)
                .border(0.75.dp, borderColor, CircleShape)
                .padding(size * 0.12f)
        } else {
            modifier.size(size)
        }

        Box(modifier = containerMod, contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCoreBankEmblem(
                    brand = brand,
                    primary = primary,
                    secondary = secondary,
                    isMonochrome = monochromeTint != null
                )
            }
        }
    } else {
        // Consistent Monogram Seal Badge for non-core or custom regional banks
        val sealTint = monochromeTint ?: brand?.primaryColor ?: MaterialTheme.colorScheme.primary
        val sealShape = RoundedCornerShape(size * 0.30f)
        val bgColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.14f)
        } else {
            sealTint.copy(alpha = 0.13f)
        }
        val outerBorderColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.42f)
        } else {
            sealTint.copy(alpha = 0.38f)
        }
        val innerMetallicColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.26f)
        } else {
            sealTint.copy(alpha = 0.22f)
        }

        Box(
            modifier = modifier
                .size(size)
                .clip(sealShape)
                .background(bgColor)
                .border(1.dp, outerBorderColor, sealShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val inset = this.size.minDimension * 0.12f
                val corner = this.size.minDimension * 0.22f
                drawRoundRect(
                    color = innerMetallicColor,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - inset * 2f, this.size.height - inset * 2f),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 0.75.dp.toPx())
                )
            }
            val textSize = when {
                monogram.length == 1 && size >= 32.dp -> (size.value * 0.42f).sp
                monogram.length == 1 -> (size.value * 0.46f).sp
                size >= 32.dp -> (size.value * 0.34f).sp
                else -> (size.value * 0.38f).sp
            }
            Text(
                text = monogram,
                color = sealTint,
                fontSize = textSize,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = textSize,
                maxLines = 1
            )
        }
    }
}

private fun DrawScope.drawCoreBankEmblem(
    brand: BankBrand,
    primary: Color,
    secondary: Color,
    isMonochrome: Boolean
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) / 2f * 0.94f
    val strokeW = (minOf(w, h) * 0.11f).coerceAtLeast(1.2f)

    when (brand) {
        // 1. ICBC (工商银行): Crimson coin with symmetric broken square frame forming "工"
        BankBrand.ICBC -> {
            val disc = Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }
            val cutout = Path().apply {
                val outerSq = r * 0.54f
                addRoundRect(
                    RoundRect(
                        left = cx - outerSq,
                        top = cy - outerSq,
                        right = cx + outerSq,
                        bottom = cy + outerSq,
                        cornerRadius = CornerRadius(r * 0.08f, r * 0.08f)
                    )
                )
            }
            val coinWithHole = Path().apply {
                fillType = PathFillType.EvenOdd
                op(disc, cutout, PathOperation.Difference)
            }
            val innerRestore = Path().apply {
                // Left & right middle bridges breaking the square frame cleanly
                val gapHalfH = r * 0.11f
                addRect(Rect(cx - r * 0.56f, cy - gapHalfH, cx - r * 0.22f, cy + gapHalfH))
                addRect(Rect(cx + r * 0.22f, cy - gapHalfH, cx + r * 0.56f, cy + gapHalfH))
                // Centered solid "工"
                val barHalfW = r * 0.28f
                val barThick = r * 0.14f
                val stemHalfW = r * 0.08f
                addRect(Rect(cx - barHalfW, cy - r * 0.30f, cx + barHalfW, cy - r * 0.30f + barThick))
                addRect(Rect(cx - barHalfW, cy + r * 0.30f - barThick, cx + barHalfW, cy + r * 0.30f))
                addRect(Rect(cx - stemHalfW, cy - r * 0.24f, cx + stemHalfW, cy + r * 0.24f))
            }
            val finalPath = Path().apply {
                fillType = PathFillType.EvenOdd
                op(coinWithHole, innerRestore, PathOperation.Union)
            }
            drawPath(finalPath, color = primary)
        }

        // 2. BOC (中国银行): Crimson coin with EvenOdd ancient "中" (outer ring + vertical bar + square hole)
        BankBrand.BOC -> {
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                // Outer coin disc & inner ring hole (touching flush at r * 0.68f with no overshoot)
                addOval(Rect(cx - r, cy - r, cx + r, cy + r))
                addOval(Rect(cx - r * 0.68f, cy - r * 0.68f, cx + r * 0.68f, cy + r * 0.68f))

                // Unified non-overlapping silhouette of "中" flush with the inner ring
                val stemW = r * 0.12f
                val boxW = r * 0.36f
                val boxH = r * 0.31f
                moveTo(cx - stemW, cy - r * 0.68f)
                lineTo(cx + stemW, cy - r * 0.68f)
                lineTo(cx + stemW, cy - boxH)
                lineTo(cx + boxW, cy - boxH)
                lineTo(cx + boxW, cy + boxH)
                lineTo(cx + stemW, cy + boxH)
                lineTo(cx + stemW, cy + r * 0.68f)
                lineTo(cx - stemW, cy + r * 0.68f)
                lineTo(cx - stemW, cy + boxH)
                lineTo(cx - boxW, cy + boxH)
                lineTo(cx - boxW, cy - boxH)
                lineTo(cx - stemW, cy - boxH)
                close()

                // Transparent square center hole inside "中"
                val hole = r * 0.16f
                addRect(Rect(cx - hole, cy - hole, cx + hole, cy + hole))
            }
            drawPath(path, color = primary)
        }

        // 3. CCB (建设银行): Royal blue coin with EvenOdd square cutout and clean diagonal notch at top-right
        BankBrand.CCB -> {
            val diag = 0.70710677f
            val notchHalf = r * 0.14f
            val disc = Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }
            val subtractions = Path().apply {
                val sq = r * 0.30f
                addRect(Rect(cx - sq, cy - sq, cx + sq, cy + sq))
                // Top-right 45-degree diagonal notch cleanly carved via PathOperation.Difference
                moveTo(cx - notchHalf * diag, cy - notchHalf * diag)
                lineTo(cx + diag * r * 1.15f - notchHalf * diag, cy - diag * r * 1.15f - notchHalf * diag)
                lineTo(cx + diag * r * 1.15f + notchHalf * diag, cy - diag * r * 1.15f + notchHalf * diag)
                lineTo(cx + notchHalf * diag, cy + notchHalf * diag)
                close()
            }
            val finalPath = Path().apply {
                fillType = PathFillType.EvenOdd
                op(disc, subtractions, PathOperation.Difference)
            }
            drawPath(finalPath, color = primary)
        }

        // 4. ABC (农业银行): Emerald circle with symmetrical wheat-ear arcs embracing central cross/window
        BankBrand.ABC -> {
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addOval(Rect(cx - r, cy - r, cx + r, cy + r))

                // Left curved wheat-ear arc cutout
                moveTo(cx - r * 0.08f, cy - r * 0.62f)
                cubicTo(cx - r * 0.74f, cy - r * 0.44f, cx - r * 0.74f, cy + r * 0.36f, cx - r * 0.12f, cy + r * 0.64f)
                cubicTo(cx - r * 0.44f, cy + r * 0.24f, cx - r * 0.44f, cy - r * 0.28f, cx - r * 0.08f, cy - r * 0.62f)
                close()

                // Right curved wheat-ear arc cutout
                moveTo(cx + r * 0.08f, cy - r * 0.62f)
                cubicTo(cx + r * 0.74f, cy - r * 0.44f, cx + r * 0.74f, cy + r * 0.36f, cx + r * 0.12f, cy + r * 0.64f)
                cubicTo(cx + r * 0.44f, cy + r * 0.24f, cx + r * 0.44f, cy - r * 0.28f, cx + r * 0.08f, cy - r * 0.62f)
                close()

                // Clean central cross/window motif cutout
                val bar = r * 0.075f
                val span = r * 0.30f
                moveTo(cx - bar, cy - span)
                lineTo(cx + bar, cy - span)
                lineTo(cx + bar, cy - bar)
                lineTo(cx + span, cy - bar)
                lineTo(cx + span, cy + bar)
                lineTo(cx + bar, cy + bar)
                lineTo(cx + bar, cy + span)
                lineTo(cx - bar, cy + span)
                lineTo(cx - bar, cy + bar)
                lineTo(cx - span, cy + bar)
                lineTo(cx - span, cy - bar)
                lineTo(cx - bar, cy - bar)
                close()
            }
            drawPath(path, color = primary)
        }

        // 5. CMB (招商银行): Crimson disc with sunburst rays fanning upward from stylized "M" base
        BankBrand.CMB -> {
            drawCircle(color = primary, radius = r, center = Offset(cx, cy))
            val rayColor = if (isMonochrome) Color.White.copy(alpha = 0.90f) else Color.White
            val goldAccent = if (isMonochrome) rayColor else Color(0xFFFDE68A)

            val angles = listOf(-54f, -32f, -11f, 11f, 32f, 54f)
            for (deg in angles) {
                val rad = (deg - 90f) * PI / 180.0
                drawLine(
                    color = goldAccent,
                    start = Offset(cx + (r * 0.28f * cos(rad)).toFloat(), cy + r * 0.14f + (r * 0.28f * sin(rad)).toFloat()),
                    end = Offset(cx + (r * 0.76f * cos(rad)).toFloat(), cy + r * 0.14f + (r * 0.76f * sin(rad)).toFloat()),
                    strokeWidth = strokeW * 0.72f,
                    cap = StrokeCap.Round
                )
            }
            val mPath = Path().apply {
                moveTo(cx - r * 0.68f, cy + r * 0.36f)
                lineTo(cx + r * 0.68f, cy + r * 0.36f)
                moveTo(cx - r * 0.50f, cy + r * 0.36f)
                lineTo(cx - r * 0.28f, cy + r * 0.02f)
                lineTo(cx, cy + r * 0.24f)
                lineTo(cx + r * 0.28f, cy + r * 0.02f)
                lineTo(cx + r * 0.50f, cy + r * 0.36f)
            }
            drawPath(
                mPath,
                color = rayColor,
                style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 6. BOCOM (交通银行): Navy badge with two interlocking triangles forming a dynamic diamond ribbon
        BankBrand.BOCOM -> {
            val leftTri = Path().apply {
                moveTo(cx - r * 0.90f, cy)
                lineTo(cx + r * 0.18f, cy - r * 0.72f)
                lineTo(cx + r * 0.18f, cy + r * 0.72f)
                close()
            }
            val rightTri = Path().apply {
                moveTo(cx + r * 0.90f, cy)
                lineTo(cx - r * 0.18f, cy - r * 0.72f)
                lineTo(cx - r * 0.18f, cy + r * 0.72f)
                close()
            }
            drawPath(leftTri, color = primary, style = Stroke(width = strokeW * 1.05f, join = StrokeJoin.Round))
            drawPath(rightTri, color = secondary, style = Stroke(width = strokeW * 1.05f, join = StrokeJoin.Round))
            val core = Path().apply {
                moveTo(cx, cy - r * 0.28f)
                lineTo(cx + r * 0.26f, cy)
                lineTo(cx, cy + r * 0.28f)
                lineTo(cx - r * 0.26f, cy)
                close()
            }
            drawPath(core, color = primary)
        }

        // 7. PSBC (邮储银行): Postal green badge with winged horizontal speed lines forming "中"
        BankBrand.PSBC -> {
            val disc = Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }
            val wingsAndSpine = Path().apply {
                val barH = r * 0.13f
                moveTo(cx - r * 0.72f, cy - r * 0.26f)
                lineTo(cx + r * 0.74f, cy - r * 0.42f)
                lineTo(cx + r * 0.62f, cy - r * 0.42f + barH)
                lineTo(cx - r * 0.72f, cy - r * 0.26f + barH)
                close()

                moveTo(cx - r * 0.64f, cy + r * 0.24f)
                lineTo(cx + r * 0.68f, cy + r * 0.10f)
                lineTo(cx + r * 0.56f, cy + r * 0.10f + barH)
                lineTo(cx - r * 0.64f, cy + r * 0.24f + barH)
                close()

                addRect(Rect(cx - r * 0.08f, cy - r * 0.62f, cx + r * 0.08f, cy + r * 0.62f))
            }
            val finalPath = Path().apply {
                fillType = PathFillType.EvenOdd
                op(disc, wingsAndSpine, PathOperation.Difference)
            }
            drawPath(finalPath, color = primary)
        }

        // 8. CITIC (中信银行): Bold square seal framing interlocking geometric strokes
        BankBrand.CITIC -> {
            val outerSeal = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = cx - r * 0.90f,
                        top = cy - r * 0.90f,
                        right = cx + r * 0.90f,
                        bottom = cy + r * 0.90f,
                        cornerRadius = CornerRadius(r * 0.22f, r * 0.22f)
                    )
                )
            }
            val hole = Path().apply {
                addRect(Rect(cx - r * 0.52f, cy - r * 0.52f, cx + r * 0.52f, cy + r * 0.52f))
            }
            val sealFrame = Path().apply { op(outerSeal, hole, PathOperation.Difference) }
            val interlockingBars = Path().apply {
                addRect(Rect(cx - r * 0.24f, cy - r * 0.52f, cx + r * 0.24f, cy - r * 0.22f))
                addRect(Rect(cx - r * 0.24f, cy + r * 0.22f, cx + r * 0.24f, cy + r * 0.52f))
                addRect(Rect(cx - r * 0.28f, cy - r * 0.10f, cx + r * 0.28f, cy + r * 0.10f))
            }
            val finalPath = Path().apply {
                fillType = PathFillType.EvenOdd
                op(sealFrame, interlockingBars, PathOperation.Union)
            }
            drawPath(finalPath, color = primary)
        }

        // 9. PAB (平安银行): Vibrant orange rounded badge with modern geometric Ping An "A" peak
        BankBrand.PAB -> {
            val outer = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = cx - r * 0.90f,
                        top = cy - r * 0.90f,
                        right = cx + r * 0.90f,
                        bottom = cy + r * 0.90f,
                        cornerRadius = CornerRadius(r * 0.32f, r * 0.32f)
                    )
                )
            }
            val peakCutout = Path().apply {
                moveTo(cx - r * 0.44f, cy + r * 0.44f)
                lineTo(cx - r * 0.04f, cy - r * 0.46f)
                lineTo(cx + r * 0.46f, cy + r * 0.44f)
                lineTo(cx + r * 0.22f, cy + r * 0.44f)
                lineTo(cx - r * 0.04f, cy - r * 0.12f)
                lineTo(cx - r * 0.22f, cy + r * 0.44f)
                close()
                addRect(Rect(cx - r * 0.24f, cy + r * 0.10f, cx + r * 0.24f, cy + r * 0.24f))
            }
            val finalPath = Path().apply {
                fillType = PathFillType.EvenOdd
                op(outer, peakCutout, PathOperation.Difference)
            }
            drawPath(finalPath, color = primary)
        }

        // 10. CIB (兴业银行): Royal blue circular badge with upward-swooping bridging curve
        BankBrand.CIB -> {
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addOval(Rect(cx - r, cy - r, cx + r, cy + r))
                // Dynamic upward-swooping bridging crescent cut out cleanly
                moveTo(cx - r * 0.72f, cy + r * 0.12f)
                cubicTo(cx - r * 0.20f, cy - r * 0.72f, cx + r * 0.56f, cy - r * 0.58f, cx + r * 0.74f, cy + r * 0.22f)
                cubicTo(cx + r * 0.38f, cy - r * 0.26f, cx - r * 0.24f, cy - r * 0.26f, cx - r * 0.72f, cy + r * 0.12f)
                close()
                // Lower counter-arc accent
                moveTo(cx - r * 0.54f, cy + r * 0.44f)
                cubicTo(cx - r * 0.08f, cy + r * 0.08f, cx + r * 0.32f, cy + r * 0.14f, cx + r * 0.56f, cy + r * 0.44f)
                cubicTo(cx + r * 0.22f, cy + r * 0.34f, cx - r * 0.22f, cy + r * 0.34f, cx - r * 0.54f, cy + r * 0.44f)
                close()
            }
            drawPath(path, color = primary)
        }

        // 11. SPDB (浦发银行): Deep blue badge with modern geometric triangular/wave ribbons
        BankBrand.SPDB -> {
            val topRibbon = Path().apply {
                moveTo(cx - r * 0.84f, cy - r * 0.16f)
                lineTo(cx - r * 0.08f, cy - r * 0.76f)
                lineTo(cx + r * 0.84f, cy - r * 0.16f)
                lineTo(cx + r * 0.30f, cy - r * 0.16f)
                lineTo(cx - r * 0.08f, cy - r * 0.44f)
                lineTo(cx - r * 0.36f, cy - r * 0.16f)
                close()
            }
            val bottomRibbon = Path().apply {
                moveTo(cx + r * 0.84f, cy + r * 0.16f)
                lineTo(cx + r * 0.08f, cy + r * 0.76f)
                lineTo(cx - r * 0.84f, cy + r * 0.16f)
                lineTo(cx - r * 0.30f, cy + r * 0.16f)
                lineTo(cx + r * 0.08f, cy + r * 0.44f)
                lineTo(cx + r * 0.36f, cy + r * 0.16f)
                close()
            }
            drawPath(topRibbon, color = primary)
            drawPath(bottomRibbon, color = secondary)
        }

        // 12. HSBC (汇丰银行): Horizontal red hexagonal bowtie (left-pointing, center split hourglass, right-pointing)
        BankBrand.HSBC -> {
            val halfH = r * 0.60f
            val midX = r * 0.48f
            val tipX = r * 0.96f
            // Left outer wing triangle
            val leftWing = Path().apply {
                moveTo(cx - tipX, cy)
                lineTo(cx - midX, cy - halfH)
                lineTo(cx - midX, cy + halfH)
                close()
            }
            // Right outer wing triangle
            val rightWing = Path().apply {
                moveTo(cx + tipX, cy)
                lineTo(cx + midX, cy - halfH)
                lineTo(cx + midX, cy + halfH)
                close()
            }
            // Center top & bottom hourglass triangles (leaving left/right center triangles open)
            val topHourglass = Path().apply {
                moveTo(cx - midX, cy - halfH)
                lineTo(cx + midX, cy - halfH)
                lineTo(cx, cy)
                close()
            }
            val bottomHourglass = Path().apply {
                moveTo(cx - midX, cy + halfH)
                lineTo(cx + midX, cy + halfH)
                lineTo(cx, cy)
                close()
            }
            drawPath(leftWing, color = primary)
            drawPath(rightWing, color = primary)
            drawPath(topHourglass, color = primary)
            drawPath(bottomHourglass, color = primary)
        }

        // 13. Citi (花旗银行): Deep blue base bars topped by iconic bold red umbrella arc
        BankBrand.CITI -> {
            // Iconic bold red umbrella arc bridging cleanly over the base
            drawArc(
                color = secondary,
                startAngle = 198f,
                sweepAngle = 144f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.86f, cy - r * 0.68f),
                size = Size(r * 1.72f, r * 0.94f),
                style = Stroke(width = strokeW * 1.30f, cap = StrokeCap.Round)
            )
            // Deep blue base bar & stylized "iti" pillar bars
            drawLine(
                color = primary,
                start = Offset(cx - r * 0.78f, cy + r * 0.16f),
                end = Offset(cx + r * 0.78f, cy + r * 0.16f),
                strokeWidth = strokeW * 1.15f,
                cap = StrokeCap.Round
            )
            drawLine(primary, Offset(cx - r * 0.44f, cy + r * 0.16f), Offset(cx - r * 0.44f, cy + r * 0.62f), strokeWidth = strokeW * 1.15f, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx, cy - r * 0.04f), Offset(cx, cy + r * 0.62f), strokeWidth = strokeW * 1.15f, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx + r * 0.44f, cy + r * 0.16f), Offset(cx + r * 0.44f, cy + r * 0.62f), strokeWidth = strokeW * 1.15f, cap = StrokeCap.Round)
        }

        // 14. DBS (星展银行): Red & charcoal/contrasting 4-pointed spark star emblem
        BankBrand.DBS -> {
            val outerStar = Path().apply {
                fillType = PathFillType.EvenOdd
                val outerR = r * 0.96f
                val innerR = r * 0.28f
                moveTo(cx, cy - outerR)
                lineTo(cx + innerR, cy - innerR)
                lineTo(cx + outerR, cy)
                lineTo(cx + innerR, cy + innerR)
                lineTo(cx, cy + outerR)
                lineTo(cx - innerR, cy + innerR)
                lineTo(cx - outerR, cy)
                lineTo(cx - innerR, cy - innerR)
                close()
                // Clean hollow diamond core
                val hole = r * 0.14f
                moveTo(cx, cy - hole)
                lineTo(cx + hole, cy)
                lineTo(cx, cy + hole)
                lineTo(cx - hole, cy)
                close()
            }
            drawPath(outerStar, color = primary)

            // Charcoal/secondary contrasting diagonal cross accents
            val cross = strokeW * 0.55f
            drawLine(secondary, Offset(cx - r * 0.34f, cy - r * 0.34f), Offset(cx - r * 0.12f, cy - r * 0.12f), strokeWidth = cross, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx + r * 0.34f, cy - r * 0.34f), Offset(cx + r * 0.12f, cy - r * 0.12f), strokeWidth = cross, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx - r * 0.34f, cy + r * 0.34f), Offset(cx - r * 0.12f, cy + r * 0.12f), strokeWidth = cross, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx + r * 0.34f, cy + r * 0.34f), Offset(cx + r * 0.12f, cy + r * 0.12f), strokeWidth = cross, cap = StrokeCap.Round)
        }

        // 15. Standard Chartered (渣打银行): Intertwining blue (#0072AA) & green (#21AA47) S-curve ribbon strands
        BankBrand.STANDARD_CHARTERED -> {
            val strandBlue = Path().apply {
                moveTo(cx + r * 0.42f, cy - r * 0.78f)
                cubicTo(cx - r * 0.72f, cy - r * 0.56f, cx + r * 0.72f, cy + r * 0.04f, cx - r * 0.42f, cy + r * 0.28f)
            }
            val strandGreen = Path().apply {
                moveTo(cx + r * 0.42f, cy - r * 0.28f)
                cubicTo(cx - r * 0.72f, cy - r * 0.04f, cx + r * 0.72f, cy + r * 0.56f, cx - r * 0.42f, cy + r * 0.78f)
            }
            drawPath(strandBlue, color = primary, style = Stroke(width = strokeW * 1.15f, cap = StrokeCap.Round))
            drawPath(strandGreen, color = secondary, style = Stroke(width = strokeW * 1.15f, cap = StrokeCap.Round))
        }

        // 16. Chase (摩根大通): Cobalt blue octagonal frame of 4 identical trapezoidal wedges around hollow square
        BankBrand.CHASE -> {
            val outer = r * 0.90f
            val inner = r * 0.34f
            val gap = r * 0.08f
            val octagon = Path().apply {
                fillType = PathFillType.EvenOdd
                // Top wedge
                moveTo(cx - inner + gap, cy - outer)
                lineTo(cx + inner, cy - outer)
                lineTo(cx + outer - gap, cy - inner)
                lineTo(cx - inner + gap, cy - inner)
                close()
                // Right wedge
                moveTo(cx + outer, cy - inner + gap)
                lineTo(cx + outer, cy + inner)
                lineTo(cx + inner, cy + outer - gap)
                lineTo(cx + inner, cy - inner + gap)
                close()
                // Bottom wedge
                moveTo(cx + inner - gap, cy + outer)
                lineTo(cx - inner, cy + outer)
                lineTo(cx - outer + gap, cy + inner)
                lineTo(cx + inner - gap, cy + inner)
                close()
                // Left wedge
                moveTo(cx - outer, cy + inner - gap)
                lineTo(cx - outer, cy - inner)
                lineTo(cx - inner, cy - outer + gap)
                lineTo(cx - inner, cy + inner - gap)
                close()
            }
            drawPath(octagon, color = primary)
        }

        else -> {
            // Unreachable since only hasCoreVectorEmblem banks enter drawCoreBankEmblem
            drawCircle(color = accentContrast, radius = r * 0.5f)
        }
    }
}
