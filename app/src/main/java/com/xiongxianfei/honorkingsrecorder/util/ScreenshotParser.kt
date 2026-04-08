package com.xiongxianfei.honorkingsrecorder.util

import com.xiongxianfei.honorkingsrecorder.ui.screens.record.HEROES

/**
 * Parses raw OCR text from a Honor of Kings post-game result screenshot and
 * extracts fields that can auto-fill the Record form.
 *
 * Observed screenshot layout (数据 tab):
 *   - "胜利" / "失败" near the top
 *   - KDA shown as "11/1/5" (kills/deaths/assists) — deaths is the middle number
 *   - Economy shown as "经济: 13.1k" — value in thousands with 'k' suffix
 *   - Hero name may or may not appear as plain text
 *
 * Designed as a pure function with no Android dependencies for easy unit testing.
 */
object ScreenshotParser {

    data class ParsedMatch(
        val hero: String? = null,
        val isWin: Boolean? = null,
        val economy: Int? = null,
        val deaths: Int? = null
    )

    fun parse(ocrText: String): ParsedMatch {
        var hero: String? = null
        var isWin: Boolean? = null
        var economy: Int? = null
        var deaths: Int? = null

        // ── Win / Loss ──────────────────────────────────────────────────────
        when {
            ocrText.contains("胜利") || ocrText.contains("我方胜利") -> isWin = true
            ocrText.contains("失败") || ocrText.contains("我方失败") || ocrText.contains("败北") -> isWin = false
        }

        // ── Hero name ────────────────────────────────────────────────────────
        // Hero names appear as plain text if the game lobby / result header shows them.
        // In the 数据 tab the hero icon is graphical only, so this may be null.
        hero = HEROES.firstOrNull { ocrText.contains(it) }

        // ── Economy ──────────────────────────────────────────────────────────
        // Primary: "经济: 13.1k"  or  "经济：8.5k"  or  "经济: 9000"
        // The value uses a 'k' suffix meaning ×1000 in HoK's stats screen.
        val econKRegex = Regex("""经济\s*[:\uff1a]\s*([0-9]+\.?[0-9]*)\s*[kK]""")
        econKRegex.find(ocrText)?.let {
            economy = it.groupValues[1].toDoubleOrNull()?.times(1000)?.toInt()
        }

        // Secondary: plain integer after label (e.g. "经济:9300" without k)
        if (economy == null) {
            val econPlainRegex = Regex("""经济\s*[:\uff1a]?\s*\n?\s*([0-9]{4,6})""")
            econPlainRegex.find(ocrText)?.let {
                economy = it.groupValues[1].toIntOrNull()
            }
        }

        // Tertiary: large isolated integer in plausible range [3000, 25000]
        if (economy == null) {
            Regex("""(?<![0-9.])([3-9][0-9]{3}|[1-2][0-9]{4})(?![0-9k])""")
                .findAll(ocrText).firstOrNull()?.let {
                    economy = it.groupValues[1].toIntOrNull()
                }
        }

        // ── Deaths ───────────────────────────────────────────────────────────
        // Primary: KDA format "kills/deaths/assists" — deaths is the middle number.
        // This is the main format in the HoK result 数据 tab (e.g. "11/1/5").
        val kdaRegex = Regex("""([0-9]{1,2})/([0-9]{1,2})/([0-9]{1,2})""")
        kdaRegex.find(ocrText)?.let {
            deaths = it.groupValues[2].toIntOrNull()
        }

        // Secondary: explicit "死亡" label (appears on some older or different screens)
        if (deaths == null) {
            val deathsLabelRegex = Regex("""死亡(?:次数)?\s*[:\uff1a]?\s*\n?\s*([0-9]{1,2})""")
            deathsLabelRegex.find(ocrText)?.let {
                deaths = it.groupValues[1].toIntOrNull()
            }
        }

        return ParsedMatch(hero, isWin, economy, deaths)
    }
}
