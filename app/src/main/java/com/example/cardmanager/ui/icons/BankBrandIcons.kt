package com.example.cardmanager.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
    val secondaryColor: Color = primaryColor
) {
    // China Major Banks
    ICBC("工商银行", "ICBC", Color(0xFFC8102E), Color(0xFF9E0B22)),
    CCB("建设银行", "CCB", Color(0xFF003B8E), Color(0xFF0066B3)),
    ABC("农业银行", "ABC", Color(0xFF008B6B), Color(0xFF006D54)),
    BOC("中国银行", "BOC", Color(0xFFB81C22), Color(0xFF8C1418)),
    CMB("招商银行", "CMB", Color(0xFFC8102E), Color(0xFFD97706)),
    BOCOM("交通银行", "BOCOM", Color(0xFF00338D), Color(0xFF0284C7)),
    PSBC("邮储银行", "PSBC", Color(0xFF007F3E), Color(0xFF059669)),
    CITIC("中信银行", "CITIC", Color(0xFFD71920), Color(0xFF991B1B)),
    CEB("光大银行", "CEB", Color(0xFF6B2D8B), Color(0xFFF59E0B)),
    SPDB("浦发银行", "SPDB", Color(0xFF003876), Color(0xFF2563EB)),
    CMBC("民生银行", "CMBC", Color(0xFF007B85), Color(0xFF10B981)),
    CIB("兴业银行", "CIB", Color(0xFF004B91), Color(0xFF0EA5E9)),
    PAB("平安银行", "PAB", Color(0xFFF36F21), Color(0xFFEA580C)),
    CGB("广发银行", "CGB", Color(0xFFC8161D), Color(0xFFE11D48)),
    BOSHANG("上海银行", "Bank of Shanghai", Color(0xFFC89B3C), Color(0xFF1E3A8A)),
    BOBEIJING("北京银行", "Bank of Beijing", Color(0xFFD62828), Color(0xFF9B1C1C)),

    // Global & Regional Major Banks
    HSBC("汇丰银行", "HSBC", Color(0xFFDB0011), Color(0xFF991B1B)),
    CITI("花旗银行", "Citi", Color(0xFF003B70), Color(0xFFEE1C25)),
    CHASE("摩根大通", "Chase", Color(0xFF117ACA), Color(0xFF0B4F8A)),
    BOA("美国银行", "Bank of America", Color(0xFF012169), Color(0xFFE31837)),
    WELLS_FARGO("富国银行", "Wells Fargo", Color(0xFFD71E28), Color(0xFFFBBF24)),
    CAPITAL_ONE("Capital One", "Capital One", Color(0xFF004879), Color(0xFFD03027)),
    DBS("星展银行", "DBS", Color(0xFFED1C24), Color(0xFF1E293B)),
    OCBC("华侨银行", "OCBC", Color(0xFFE4002B), Color(0xFF9F1239)),
    UOB("大华银行", "UOB", Color(0xFF00377B), Color(0xFFED1C24)),
    STANDARD_CHARTERED("渣打银行", "Standard Chartered", Color(0xFF0072AA), Color(0xFF00A859)),
    BARCLAYS("巴克莱银行", "Barclays", Color(0xFF00AEEF), Color(0xFF00395D)),
    SANTANDER("桑坦德银行", "Santander", Color(0xFFEC0000), Color(0xFFB91C1C)),
    MUFG("三菱UFJ银行", "MUFG", Color(0xFFE60012), Color(0xFF6B7280)),
    SMBC("三井住友银行", "SMBC", Color(0xFF00A847), Color(0xFF004831));

    companion object {
        val popularQuickBanks: List<BankBrand> = listOf(
            CMB, ICBC, CCB, ABC, BOC, BOCOM, CITIC, PAB, CIB, HSBC, CITI, CHASE, DBS
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

        // Strip common Chinese bank suffixes first
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

    if (brand != null) {
        val primary = monochromeTint ?: brand.primaryColor
        val secondary = monochromeTint?.copy(alpha = 0.72f) ?: brand.secondaryColor
        val containerColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.14f)
        } else {
            brand.primaryColor.copy(alpha = 0.12f)
        }
        val borderColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.30f)
        } else {
            brand.primaryColor.copy(alpha = 0.28f)
        }

        val containerMod = if (showContainer) {
            modifier
                .size(size)
                .clip(CircleShape)
                .background(containerColor)
                .border(0.75.dp, borderColor, CircleShape)
                .padding(size * 0.18f)
        } else {
            modifier.size(size)
        }

        Box(modifier = containerMod, contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBrandEmblem(brand = brand, primary = primary, secondary = secondary)
            }
        }
    } else {
        // Polished Monogram Geometric Emblem Fallback
        val tint = monochromeTint ?: MaterialTheme.colorScheme.primary
        val bgColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.14f)
        } else {
            tint.copy(alpha = 0.12f)
        }
        val ringColor = if (monochromeTint != null) {
            monochromeTint.copy(alpha = 0.42f)
        } else {
            tint.copy(alpha = 0.36f)
        }

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Subtle inner metallic ring
                drawCircle(
                    color = ringColor.copy(alpha = ringColor.alpha * 0.55f),
                    radius = this.size.minDimension * 0.40f,
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
                color = tint,
                fontSize = textSize,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = textSize,
                maxLines = 1
            )
        }
    }
}

private fun DrawScope.drawBrandEmblem(
    brand: BankBrand,
    primary: Color,
    secondary: Color
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) / 2f
    val strokeW = (minOf(w, h) * 0.13f).coerceAtLeast(1.2f)

    when (brand) {
        BankBrand.ICBC -> {
            // Ancient coin ring with broken horizontal bars forming stylized "工"
            drawCircle(color = primary, radius = r * 0.88f, style = Stroke(width = strokeW))
            drawLine(primary, Offset(cx - r * 0.38f, cy - r * 0.30f), Offset(cx + r * 0.38f, cy - r * 0.30f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx, cy - r * 0.30f), Offset(cx, cy + r * 0.30f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx - r * 0.38f, cy + r * 0.30f), Offset(cx + r * 0.38f, cy + r * 0.30f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        BankBrand.CCB -> {
            // Circular coin motif with inner square cutout and tilted diagonal channel
            drawCircle(color = primary, radius = r * 0.88f, style = Stroke(width = strokeW * 1.15f))
            rotate(degrees = 45f, pivot = Offset(cx, cy)) {
                drawRect(
                    color = secondary,
                    topLeft = Offset(cx - r * 0.28f, cy - r * 0.28f),
                    size = Size(r * 0.56f, r * 0.56f),
                    style = Stroke(width = strokeW * 0.9f)
                )
            }
        }

        BankBrand.ABC -> {
            // Split circular emblem with rising twin wheat leaves
            drawCircle(color = primary, radius = r * 0.88f, style = Stroke(width = strokeW))
            val leaf = Path().apply {
                moveTo(cx, cy - r * 0.52f)
                lineTo(cx + r * 0.42f, cy + r * 0.12f)
                lineTo(cx, cy + r * 0.48f)
                lineTo(cx - r * 0.42f, cy + r * 0.12f)
                close()
            }
            drawPath(leaf, color = primary, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))
            drawLine(primary, Offset(cx, cy - r * 0.45f), Offset(cx, cy + r * 0.45f), strokeWidth = strokeW * 0.85f)
        }

        BankBrand.BOC -> {
            // Traditional Chinese coin ring with vertical bar ("中")
            drawCircle(color = primary, radius = r * 0.86f, style = Stroke(width = strokeW * 1.15f))
            drawRect(
                color = primary,
                topLeft = Offset(cx - r * 0.26f, cy - r * 0.26f),
                size = Size(r * 0.52f, r * 0.52f),
                style = Stroke(width = strokeW * 0.85f)
            )
            drawLine(primary, Offset(cx, cy - r * 0.82f), Offset(cx, cy + r * 0.82f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        BankBrand.CMB -> {
            // Iconic radiating sunflower petal crown ("招")
            drawArc(
                color = primary,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.72f, cy - r * 0.46f),
                size = Size(r * 1.44f, r * 1.44f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawLine(primary, Offset(cx - r * 0.82f, cy + r * 0.36f), Offset(cx + r * 0.82f, cy + r * 0.36f), strokeWidth = strokeW, cap = StrokeCap.Round)
            val rays = listOf(-42f, -14f, 14f, 42f)
            for (deg in rays) {
                val rad = (deg - 90f) * PI / 180.0
                drawLine(
                    color = secondary,
                    start = Offset(cx + (r * 0.36f * cos(rad)).toFloat(), cy + (r * 0.36f * sin(rad)).toFloat()),
                    end = Offset(cx + (r * 0.82f * cos(rad)).toFloat(), cy + (r * 0.82f * sin(rad)).toFloat()),
                    strokeWidth = strokeW * 0.78f,
                    cap = StrokeCap.Round
                )
            }
        }

        BankBrand.BOCOM -> {
            // Intersecting twin diamond-wing triangles
            val leftWing = Path().apply {
                moveTo(cx - r * 0.84f, cy)
                lineTo(cx + r * 0.10f, cy - r * 0.68f)
                lineTo(cx + r * 0.10f, cy + r * 0.68f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(cx + r * 0.84f, cy)
                lineTo(cx - r * 0.10f, cy - r * 0.68f)
                lineTo(cx - r * 0.10f, cy + r * 0.68f)
                close()
            }
            drawPath(leftWing, color = primary, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))
            drawPath(rightWing, color = secondary, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))
        }

        BankBrand.PSBC -> {
            // Forward winged postal chevron
            val wing = Path().apply {
                moveTo(cx - r * 0.82f, cy - r * 0.48f)
                lineTo(cx + r * 0.82f, cy - r * 0.16f)
                lineTo(cx - r * 0.12f, cy + r * 0.56f)
                lineTo(cx + r * 0.82f, cy + r * 0.40f)
            }
            drawPath(wing, color = primary, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        BankBrand.CITIC -> {
            // Bold geometric interlocking CITIC shield/bars
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.78f, cy - r * 0.62f),
                size = Size(r * 1.56f, r * 1.24f),
                cornerRadius = CornerRadius(r * 0.24f, r * 0.24f),
                style = Stroke(width = strokeW * 0.95f)
            )
            drawLine(primary, Offset(cx - r * 0.35f, cy - r * 0.25f), Offset(cx + r * 0.35f, cy - r * 0.25f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx - r * 0.35f, cy + r * 0.25f), Offset(cx + r * 0.35f, cy + r * 0.25f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        BankBrand.CEB -> {
            // Everbright rising solar ellipse
            drawArc(
                color = primary,
                startAngle = 35f,
                sweepAngle = 290f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.82f, cy - r * 0.68f),
                size = Size(r * 1.64f, r * 1.36f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawCircle(color = secondary, radius = r * 0.28f, center = Offset(cx + r * 0.12f, cy))
        }

        BankBrand.SPDB -> {
            // Interlocking S-curve waves in circle
            drawCircle(color = primary, radius = r * 0.86f, style = Stroke(width = strokeW * 0.9f))
            val sPath = Path().apply {
                moveTo(cx + r * 0.42f, cy - r * 0.44f)
                cubicTo(cx - r * 0.58f, cy - r * 0.44f, cx + r * 0.58f, cy + r * 0.44f, cx - r * 0.42f, cy + r * 0.44f)
            }
            drawPath(sPath, color = secondary, style = Stroke(width = strokeW * 1.05f, cap = StrokeCap.Round))
        }

        BankBrand.CMBC -> {
            // Staggered horizon ribbon bars
            drawLine(primary, Offset(cx - r * 0.72f, cy - r * 0.46f), Offset(cx + r * 0.48f, cy - r * 0.46f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx - r * 0.48f, cy), Offset(cx + r * 0.72f, cy), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx - r * 0.72f, cy + r * 0.46f), Offset(cx + r * 0.48f, cy + r * 0.46f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        BankBrand.CIB -> {
            // Rising crescent arc / horn silhouette
            drawArc(
                color = primary,
                startAngle = 125f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.78f, cy - r * 0.78f),
                size = Size(r * 1.56f, r * 1.56f),
                style = Stroke(width = strokeW * 1.15f, cap = StrokeCap.Round)
            )
            drawCircle(color = secondary, radius = r * 0.22f, center = Offset(cx + r * 0.24f, cy + r * 0.16f))
        }

        BankBrand.PAB -> {
            // Rounded Ping An shield crest with stylized peak
            val shield = Path().apply {
                moveTo(cx, cy - r * 0.84f)
                lineTo(cx + r * 0.74f, cy - r * 0.44f)
                lineTo(cx + r * 0.58f, cy + r * 0.34f)
                lineTo(cx, cy + r * 0.86f)
                lineTo(cx - r * 0.58f, cy + r * 0.34f)
                lineTo(cx - r * 0.74f, cy - r * 0.44f)
                close()
            }
            drawPath(shield, color = primary, style = Stroke(width = strokeW, join = StrokeJoin.Round))
            drawLine(primary, Offset(cx - r * 0.28f, cy + r * 0.18f), Offset(cx, cy - r * 0.32f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx, cy - r * 0.32f), Offset(cx + r * 0.28f, cy + r * 0.18f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
        }

        BankBrand.CGB -> {
            // Three sweeping flame arcs
            for (i in -1..1) {
                val offsetY = i * r * 0.34f
                drawArc(
                    color = if (i == 0) secondary else primary,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.72f, cy - r * 0.38f + offsetY),
                    size = Size(r * 1.44f, r * 0.78f),
                    style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
                )
            }
        }

        BankBrand.BOSHANG -> {
            // Coin medallion with stylized geometric "上"
            drawCircle(color = primary, radius = r * 0.86f, style = Stroke(width = strokeW))
            drawLine(secondary, Offset(cx, cy - r * 0.46f), Offset(cx, cy + r * 0.38f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx, cy - r * 0.06f), Offset(cx + r * 0.38f, cy - r * 0.06f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx - r * 0.44f, cy + r * 0.38f), Offset(cx + r * 0.44f, cy + r * 0.38f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        BankBrand.BOBEIJING -> {
            // Imperial gate / stylized square seal
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.76f, cy - r * 0.76f),
                size = Size(r * 1.52f, r * 1.52f),
                cornerRadius = CornerRadius(r * 0.22f, r * 0.22f),
                style = Stroke(width = strokeW)
            )
            drawLine(primary, Offset(cx, cy - r * 0.44f), Offset(cx, cy + r * 0.44f), strokeWidth = strokeW * 0.85f, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx - r * 0.40f, cy), Offset(cx + r * 0.40f, cy), strokeWidth = strokeW * 0.85f, cap = StrokeCap.Round)
        }

        BankBrand.HSBC -> {
            // Signature 4-triangle hexagon bowtie emblem
            val halfH = r * 0.56f
            val midX = r * 0.46f
            val tipX = r * 0.94f
            val leftTri = Path().apply {
                moveTo(cx - tipX, cy)
                lineTo(cx - midX, cy - halfH)
                lineTo(cx - midX, cy + halfH)
                close()
            }
            val rightTri = Path().apply {
                moveTo(cx + tipX, cy)
                lineTo(cx + midX, cy - halfH)
                lineTo(cx + midX, cy + halfH)
                close()
            }
            val topTri = Path().apply {
                moveTo(cx - midX, cy - halfH)
                lineTo(cx + midX, cy - halfH)
                lineTo(cx, cy)
                close()
            }
            val bottomTri = Path().apply {
                moveTo(cx - midX, cy + halfH)
                lineTo(cx + midX, cy + halfH)
                lineTo(cx, cy)
                close()
            }
            drawPath(leftTri, color = primary)
            drawPath(rightTri, color = primary)
            drawPath(topTri, color = primary.copy(alpha = primary.alpha * 0.78f))
            drawPath(bottomTri, color = primary.copy(alpha = primary.alpha * 0.78f))
        }

        BankBrand.CITI -> {
            // Iconic Citi red umbrella arc over twin pillar bars
            drawArc(
                color = secondary,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.82f, cy - r * 0.78f),
                size = Size(r * 1.64f, r * 0.96f),
                style = Stroke(width = strokeW * 1.05f, cap = StrokeCap.Round)
            )
            drawLine(primary, Offset(cx - r * 0.52f, cy + r * 0.04f), Offset(cx - r * 0.52f, cy + r * 0.60f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx, cy - r * 0.10f), Offset(cx, cy + r * 0.60f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx + r * 0.52f, cy + r * 0.04f), Offset(cx + r * 0.52f, cy + r * 0.60f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }

        BankBrand.CHASE -> {
            // Hollow octagonal pinwheel
            val outer = r * 0.86f
            val inner = r * 0.34f
            val gap = r * 0.08f
            val wedges = listOf(
                Path().apply {
                    moveTo(cx - inner, cy - outer)
                    lineTo(cx + inner, cy - outer)
                    lineTo(cx + outer, cy - inner)
                    lineTo(cx + inner, cy - inner + gap)
                    lineTo(cx - inner, cy - inner + gap)
                    close()
                },
                Path().apply {
                    moveTo(cx + outer, cy - inner)
                    lineTo(cx + outer, cy + inner)
                    lineTo(cx + inner, cy + outer)
                    lineTo(cx + inner - gap, cy + inner)
                    lineTo(cx + inner - gap, cy - inner)
                    close()
                },
                Path().apply {
                    moveTo(cx + inner, cy + outer)
                    lineTo(cx - inner, cy + outer)
                    lineTo(cx - outer, cy + inner)
                    lineTo(cx - inner, cy + inner - gap)
                    lineTo(cx + inner, cy + inner - gap)
                    close()
                },
                Path().apply {
                    moveTo(cx - outer, cy + inner)
                    lineTo(cx - outer, cy - inner)
                    lineTo(cx - inner, cy - outer)
                    lineTo(cx - inner + gap, cy - inner)
                    lineTo(cx - inner + gap, cy + inner)
                    close()
                }
            )
            wedges.forEach { drawPath(it, color = primary) }
        }

        BankBrand.BOA -> {
            // Slanted alternating flag ribbons
            for (i in -1..1) {
                val dy = i * r * 0.42f
                val color = if (i >= 0) secondary else primary
                drawLine(
                    color = color,
                    start = Offset(cx - r * 0.72f, cy + dy + r * 0.16f),
                    end = Offset(cx + r * 0.72f, cy + dy - r * 0.16f),
                    strokeWidth = strokeW * 1.15f,
                    cap = StrokeCap.Round
                )
            }
        }

        BankBrand.WELLS_FARGO -> {
            // Classic square crest with bold inner double-bar motif
            drawRoundRect(
                color = primary,
                topLeft = Offset(cx - r * 0.82f, cy - r * 0.82f),
                size = Size(r * 1.64f, r * 1.64f),
                cornerRadius = CornerRadius(r * 0.18f, r * 0.18f),
                style = Stroke(width = strokeW)
            )
            drawLine(secondary, Offset(cx - r * 0.44f, cy - r * 0.34f), Offset(cx - r * 0.18f, cy + r * 0.34f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx - r * 0.18f, cy + r * 0.34f), Offset(cx, cy - r * 0.10f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx, cy - r * 0.10f), Offset(cx + r * 0.18f, cy + r * 0.34f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
            drawLine(secondary, Offset(cx + r * 0.18f, cy + r * 0.34f), Offset(cx + r * 0.44f, cy - r * 0.34f), strokeWidth = strokeW * 0.9f, cap = StrokeCap.Round)
        }

        BankBrand.CAPITAL_ONE -> {
            // Bold circle ring crossed by crimson boomerang swoosh
            drawCircle(color = primary, radius = r * 0.72f, style = Stroke(width = strokeW))
            drawArc(
                color = secondary,
                startAngle = 215f,
                sweepAngle = 165f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.88f, cy - r * 0.56f),
                size = Size(r * 1.76f, r * 1.12f),
                style = Stroke(width = strokeW * 1.15f, cap = StrokeCap.Round)
            )
        }

        BankBrand.DBS -> {
            // Iconic 4-point blooming DBS star
            val star = Path().apply {
                val outerR = r * 0.92f
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
            }
            drawPath(star, color = primary)
        }

        BankBrand.OCBC -> {
            // Heritage junk ship sail & crescent hull
            val sail = Path().apply {
                moveTo(cx + r * 0.14f, cy - r * 0.82f)
                lineTo(cx + r * 0.58f, cy + r * 0.24f)
                lineTo(cx - r * 0.46f, cy + r * 0.24f)
                close()
            }
            drawPath(sail, color = primary, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))
            drawArc(
                color = primary,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.82f, cy - r * 0.08f),
                size = Size(r * 1.64f, r * 0.82f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        BankBrand.UOB -> {
            // Signature 5 slanted vertical pillars
            for (i in -2..2) {
                val x = cx + i * r * 0.34f
                val color = if (i % 2 == 0) primary else secondary
                drawLine(
                    color = color,
                    start = Offset(x + r * 0.08f, cy - r * 0.58f),
                    end = Offset(x - r * 0.08f, cy + r * 0.58f),
                    strokeWidth = strokeW * 0.82f,
                    cap = StrokeCap.Round
                )
            }
        }

        BankBrand.STANDARD_CHARTERED -> {
            // Intertwined S-ribbon helix
            val topLoop = Path().apply {
                moveTo(cx + r * 0.48f, cy - r * 0.66f)
                cubicTo(cx - r * 0.68f, cy - r * 0.56f, cx + r * 0.68f, cy + r * 0.06f, cx - r * 0.48f, cy + r * 0.16f)
            }
            val bottomLoop = Path().apply {
                moveTo(cx + r * 0.48f, cy - r * 0.16f)
                cubicTo(cx - r * 0.68f, cy - r * 0.06f, cx + r * 0.68f, cy + r * 0.56f, cx - r * 0.48f, cy + r * 0.66f)
            }
            drawPath(topLoop, color = primary, style = Stroke(width = strokeW * 1.05f, cap = StrokeCap.Round))
            drawPath(bottomLoop, color = secondary, style = Stroke(width = strokeW * 1.05f, cap = StrokeCap.Round))
        }

        BankBrand.BARCLAYS -> {
            // Symmetric spread eagle wings
            val wings = Path().apply {
                moveTo(cx - r * 0.86f, cy - r * 0.44f)
                lineTo(cx, cy + r * 0.56f)
                lineTo(cx + r * 0.86f, cy - r * 0.44f)
                moveTo(cx - r * 0.56f, cy - r * 0.56f)
                lineTo(cx, cy + r * 0.12f)
                lineTo(cx + r * 0.56f, cy - r * 0.56f)
            }
            drawPath(wings, color = primary, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        BankBrand.SANTANDER -> {
            // Rising triple flame plumes
            for (i in -1..1) {
                val fx = cx + i * r * 0.44f
                val fyTop = if (i == 0) cy - r * 0.72f else cy - r * 0.48f
                val flame = Path().apply {
                    moveTo(fx, cy + r * 0.64f)
                    cubicTo(fx - r * 0.24f, cy + r * 0.12f, fx + r * 0.24f, cy - r * 0.12f, fx, fyTop)
                }
                drawPath(flame, color = primary, style = Stroke(width = strokeW * 0.95f, cap = StrokeCap.Round))
            }
        }

        BankBrand.MUFG -> {
            // Iconic 3-diamond triad
            val topDiamond = Path().apply {
                moveTo(cx, cy - r * 0.88f)
                lineTo(cx + r * 0.30f, cy - r * 0.40f)
                lineTo(cx, cy + r * 0.04f)
                lineTo(cx - r * 0.30f, cy - r * 0.40f)
                close()
            }
            val leftDiamond = Path().apply {
                moveTo(cx, cy + r * 0.04f)
                lineTo(cx - r * 0.28f, cy + r * 0.56f)
                lineTo(cx - r * 0.82f, cy + r * 0.56f)
                lineTo(cx - r * 0.52f, cy + r * 0.04f)
                close()
            }
            val rightDiamond = Path().apply {
                moveTo(cx, cy + r * 0.04f)
                lineTo(cx + r * 0.52f, cy + r * 0.04f)
                lineTo(cx + r * 0.82f, cy + r * 0.56f)
                lineTo(cx + r * 0.28f, cy + r * 0.56f)
                close()
            }
            drawPath(topDiamond, color = primary)
            drawPath(leftDiamond, color = primary)
            drawPath(rightDiamond, color = primary)
        }

        BankBrand.SMBC -> {
            // Twin rising vertical green pillars inside shield frame
            drawRoundRect(
                color = secondary,
                topLeft = Offset(cx - r * 0.76f, cy - r * 0.76f),
                size = Size(r * 1.52f, r * 1.52f),
                cornerRadius = CornerRadius(r * 0.24f, r * 0.24f),
                style = Stroke(width = strokeW * 0.9f)
            )
            drawLine(primary, Offset(cx - r * 0.24f, cy - r * 0.42f), Offset(cx - r * 0.24f, cy + r * 0.42f), strokeWidth = strokeW * 1.25f, cap = StrokeCap.Round)
            drawLine(primary, Offset(cx + r * 0.24f, cy - r * 0.42f), Offset(cx + r * 0.24f, cy + r * 0.42f), strokeWidth = strokeW * 1.25f, cap = StrokeCap.Round)
        }
    }
}
