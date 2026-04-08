package com.xiongxianfei.honorkingsrecorder.util

import com.xiongxianfei.honorkingsrecorder.ui.screens.record.HEROES

/**
 * Parses raw OCR text from a Honor of Kings post-game result screenshot.
 *
 * Observed screenshot layout (数据 tab):
 *   Header row: "[player name]  13.1  11/1/5"
 *     → The decimal number immediately before the KDA IS the economy in thousands
 *   Stats section: "经济: 13.1k"  (label + k-suffix value)
 *   Win indicator: "胜利" / "失败" at the top
 *
 * Economy extraction uses four strategies in order:
 *   1. Decimal/integer immediately before KDA in header ("13.1 11/1/5" → 13100)
 *   2. "经济" label + k-suffix number ("经济: 13.1k" → 13100)
 *   3. "经济" label + any decimal, k possibly missing ("经济: 13.1" → 13100)
 *   4. "经济" label + plain 4-6 digit integer ("经济: 9300" → 9300)
 *
 * Pure function — no Android dependencies, fully unit-testable.
 */
object ScreenshotParser {

    data class ParsedMatch(
        val hero: String? = null,
        val isWin: Boolean? = null,
        val economy: Int? = null,
        val kills: Int? = null,
        val deaths: Int? = null,
        val assists: Int? = null,
        /** First ~120 chars of raw OCR, shown when economy can't be parsed. */
        val rawOcrHint: String? = null,
    )

    fun parse(ocrText: String): ParsedMatch {
        var hero: String? = null
        var isWin: Boolean? = null
        var economy: Int? = null
        var kills: Int? = null
        var deaths: Int? = null
        var assists: Int? = null

        // ── Win / Loss ──────────────────────────────────────────────────────
        when {
            ocrText.contains("胜利") || ocrText.contains("我方胜利") -> isWin = true
            ocrText.contains("失败") || ocrText.contains("我方失败") || ocrText.contains("败北") -> isWin = false
        }

        // ── Hero name ────────────────────────────────────────────────────────
        hero = HEROES.firstOrNull { ocrText.contains(it) }

        // ── KDA (kills / deaths / assists) ────────────────────────────────────
        // Allow optional whitespace around slashes — OCR sometimes adds spaces.
        val kdaRegex = Regex("""([0-9]{1,2})\s*/\s*([0-9]{1,2})\s*/\s*([0-9]{1,2})""")
        kdaRegex.find(ocrText)?.let {
            kills   = it.groupValues[1].toIntOrNull()
            deaths  = it.groupValues[2].toIntOrNull()
            assists = it.groupValues[3].toIntOrNull()
        }

        // Deaths fallback: explicit "死亡" label (some screen variants)
        if (deaths == null) {
            Regex("""死亡(?:次数)?\s*[:\uff1a]?\s*\n?\s*([0-9]{1,2})""")
                .find(ocrText)?.let { deaths = it.groupValues[1].toIntOrNull() }
        }

        // ── Economy — Strategy 1 (most reliable for this screenshot) ─────────
        // The header row in HoK shows: "[name]  13.1  11/1/5"
        // The decimal immediately before the KDA is the economy in thousands.
        // Require a decimal point to avoid matching kill/assist counts (integers).
        val headerEconRegex = Regex(
            """([0-9]+\.[0-9]+)\s+[0-9]{1,2}\s*/\s*[0-9]{1,2}\s*/\s*[0-9]{1,2}"""
        )
        headerEconRegex.find(ocrText)?.let {
            val raw = it.groupValues[1].toDoubleOrNull()
            // Plausible range for economy in k: 0.5k–500k
            if (raw != null && raw in 0.5..500.0) economy = (raw * 1000).toInt()
        }

        // ── Economy — Strategy 2: "经济" label + k-suffix ────────────────────
        // "经济: 13.1k", "经济:13.1 k", OCR noise with spaces inside the number.
        if (economy == null) {
            val econKRegex = Regex(
                """经济\s*[:\uff1a]?\s*([0-9]+\s*\.?\s*[0-9]*)\s*[kK]"""
            )
            econKRegex.find(ocrText)?.let {
                val raw = it.groupValues[1].replace(Regex("""\s"""), "").toDoubleOrNull()
                if (raw != null && raw > 0) economy = (raw * 1000).toInt()
            }
        }

        // ── Economy — Strategy 3: "经济" label + decimal, k dropped by OCR ───
        // Some OCR runs drop the 'k': "经济: 13.1" — treat any decimal < 500 as k.
        if (economy == null) {
            val econDecimalRegex = Regex("""经济[^0-9]*([0-9]+\.[0-9]+)""")
            econDecimalRegex.find(ocrText)?.let {
                val raw = it.groupValues[1].toDoubleOrNull()
                if (raw != null && raw in 0.5..500.0) economy = (raw * 1000).toInt()
            }
        }

        // ── Economy — Strategy 4: "经济" label + plain 4-6 digit integer ─────
        if (economy == null) {
            Regex("""经济[^0-9]*([0-9]{4,6})""").find(ocrText)?.let {
                economy = it.groupValues[1].toIntOrNull()
            }
        }

        // Raw OCR hint for debugging when economy was not found
        val rawOcrHint = if (economy == null) {
            ocrText.replace('\n', ' ').take(150).trimEnd()
        } else null

        return ParsedMatch(hero, isWin, economy, kills, deaths, assists, rawOcrHint)
    }
}
