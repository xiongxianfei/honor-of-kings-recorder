package com.xiongxianfei.honorkingsrecorder.util

import com.xiongxianfei.honorkingsrecorder.ui.screens.record.HEROES

/**
 * Parses raw OCR text from a Honor of Kings post-game result screenshot and
 * extracts fields that can auto-fill the Record form.
 *
 * Observed screenshot layout (数据 tab):
 *   - "胜利" / "失败" near the top
 *   - KDA shown as "11/1/5" (kills/deaths/assists) — may have spaces around slashes
 *   - Economy shown as "经济: 13.1k" — value in thousands with 'k' suffix;
 *     OCR may produce "13. 1k" or "13.1 k" with extra spaces
 *   - Hero name may or may not appear as plain text in this tab
 *
 * Pure function with no Android dependencies for easy unit testing.
 */
object ScreenshotParser {

    data class ParsedMatch(
        val hero: String? = null,
        val isWin: Boolean? = null,
        val economy: Int? = null,
        val kills: Int? = null,
        val deaths: Int? = null,
        val assists: Int? = null,
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

        // ── Economy ──────────────────────────────────────────────────────────
        // Primary: "经济: 13.1k" — OCR may produce spaces within the number or
        // before 'k', so allow \s* between digits, decimal point, and k-suffix.
        val econKRegex = Regex(
            """经济\s*[:\uff1a]?\s*([0-9]+\s*\.?\s*[0-9]*)\s*[kK]"""
        )
        econKRegex.find(ocrText)?.let {
            // Strip any spaces OCR inserted inside the number before parsing
            val raw = it.groupValues[1].replace(Regex("""\s"""), "")
            economy = raw.toDoubleOrNull()?.times(1000)?.toInt()
        }

        // Secondary: plain 4-6 digit integer after the label (no k suffix)
        if (economy == null) {
            val econPlainRegex = Regex("""经济\s*[:\uff1a]?\s*\n?\s*([0-9]{4,6})""")
            econPlainRegex.find(ocrText)?.let {
                economy = it.groupValues[1].toIntOrNull()
            }
        }

        // Tertiary: any isolated large integer in plausible range [3000, 25000]
        if (economy == null) {
            Regex("""(?<![0-9.])([3-9][0-9]{3}|[1-2][0-9]{4})(?![0-9k])""")
                .findAll(ocrText).firstOrNull()?.let {
                    economy = it.groupValues[1].toIntOrNull()
                }
        }

        // ── KDA (kills / deaths / assists) ────────────────────────────────────
        // Allow optional whitespace around each slash — OCR sometimes adds spaces.
        val kdaRegex = Regex(
            """([0-9]{1,2})\s*/\s*([0-9]{1,2})\s*/\s*([0-9]{1,2})"""
        )
        kdaRegex.find(ocrText)?.let {
            kills   = it.groupValues[1].toIntOrNull()
            deaths  = it.groupValues[2].toIntOrNull()
            assists = it.groupValues[3].toIntOrNull()
        }

        // Deaths fallback: explicit "死亡" label (some screen variants)
        if (deaths == null) {
            val deathsLabelRegex = Regex(
                """死亡(?:次数)?\s*[:\uff1a]?\s*\n?\s*([0-9]{1,2})"""
            )
            deathsLabelRegex.find(ocrText)?.let {
                deaths = it.groupValues[1].toIntOrNull()
            }
        }

        return ParsedMatch(hero, isWin, economy, kills, deaths, assists)
    }
}
