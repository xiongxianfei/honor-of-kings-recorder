package com.xiongxianfei.honorkingsrecorder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotParserTest {

    private fun parse(text: String) = ScreenshotParser.parse(text)

    // ── Win / Loss ──────────────────────────────────────────────────────────

    @Test
    fun isWin_victorySingleWord_returnsTrue() {
        assertTrue(parse("胜利").isWin!!)
    }

    @Test
    fun isWin_ourSideVictory_returnsTrue() {
        assertTrue(parse("我方胜利").isWin!!)
    }

    @Test
    fun isWin_defeatWord_returnsFalse() {
        assertFalse(parse("失败").isWin!!)
    }

    @Test
    fun isWin_ourSideDefeat_returnsFalse() {
        assertFalse(parse("我方失败").isWin!!)
    }

    @Test
    fun isWin_noResultWord_returnsNull() {
        assertNull(parse("经济 8000").isWin)
    }

    // ── Hero ────────────────────────────────────────────────────────────────

    @Test
    fun hero_houyiInText_recognized() {
        assertEquals("后羿", parse("后羿 经济 8000").hero)
    }

    @Test
    fun hero_gouyaInText_recognized() {
        assertEquals("戈娅", parse("战绩 戈娅").hero)
    }

    @Test
    fun hero_notInText_returnsNull() {
        // HoK 数据 tab shows hero icon graphically — no text name
        assertNull(parse("胜利\n11/1/5\n经济: 13.1k").hero)
    }

    @Test
    fun hero_unknownHero_returnsNull() {
        assertNull(parse("韩信 胜利").hero)
    }

    // ── Economy — k-suffix format (real screenshot) ─────────────────────────

    @Test
    fun economy_kSuffix_13point1k_returns13100() {
        assertEquals(13100, parse("经济: 13.1k").economy)
    }

    @Test
    fun economy_kSuffix_fullWidthColon() {
        assertEquals(8500, parse("经济：8.5k").economy)
    }

    @Test
    fun economy_kSuffix_integerK() {
        assertEquals(9000, parse("经济: 9k").economy)
    }

    @Test
    fun economy_kSuffix_uppercaseK() {
        assertEquals(12000, parse("经济: 12K").economy)
    }

    @Test
    fun economy_kSuffix_lowValue_belowThreshold() {
        // 5.2k = 5200, below the ≥6500 threshold
        assertEquals(5200, parse("经济: 5.2k").economy)
    }

    // ── Economy — plain integer format (fallback) ────────────────────────────

    @Test
    fun economy_plainInteger_withColon() {
        assertEquals(7200, parse("经济:7200").economy)
    }

    @Test
    fun economy_plainInteger_onSeparateLine() {
        assertEquals(9300, parse("经济\n9300").economy)
    }

    @Test
    fun economy_noLabel_returnsNull() {
        assertNull(parse("胜利 11/1/5").economy)
    }

    // ── Deaths — KDA format (real screenshot) ────────────────────────────────

    @Test
    fun deaths_kdaFormat_11_1_5_returnsMiddle() {
        assertEquals(1, parse("11/1/5").deaths)
    }

    @Test
    fun deaths_kdaFormat_zeroDeaths() {
        assertEquals(0, parse("8/0/10").deaths)
    }

    @Test
    fun deaths_kdaFormat_highDeaths() {
        assertEquals(9, parse("2/9/3").deaths)
    }

    @Test
    fun deaths_kdaFormat_singleDigitAll() {
        assertEquals(2, parse("5/2/7").deaths)
    }

    // ── Deaths — explicit label format (fallback) ───────────────────────────

    @Test
    fun deaths_explicitLabel_withColon() {
        assertEquals(3, parse("死亡:3").deaths)
    }

    @Test
    fun deaths_explicitLabel_withFullWidthColon() {
        assertEquals(2, parse("死亡：2").deaths)
    }

    @Test
    fun deaths_explicitCountLabel() {
        assertEquals(4, parse("死亡次数:4").deaths)
    }

    @Test
    fun deaths_noIndicator_returnsNull() {
        assertNull(parse("胜利 经济: 8.0k").deaths)
    }

    // ── Real screenshot simulation ────────────────────────────────────────────

    @Test
    fun realScreenshot_winMvpGame_parsesCorrectly() {
        // Simulates OCR output from the provided screenshot
        val ocr = """
            胜利
            MVP 金牌发育路
            20 vs 10 巅峰赛
            不败、菜鸟 13.1 11/1/5
            关键团战输出
            总览 数据 复盘
            个人数据 同队对比 对位对比
            我方胜利
            对英雄输出: 171.2k 输出占比: 35.3%
            总输出: 611.1k 输出转化率: 1.5
            经济: 13.1k 经济占比: 24%
            打野经济: 1.4k 补刀数: 50
        """.trimIndent()

        val result = parse(ocr)
        assertTrue(result.isWin!!)
        assertEquals(13100, result.economy)
        assertEquals(1, result.deaths)
        // Hero name not in text — null is correct for this screenshot tab
        assertNull(result.hero)
    }

    @Test
    fun realScreenshot_lossGame_parsesCorrectly() {
        val ocr = """
            失败
            孙尚香
            5/4/2
            我方失败
            经济: 7.8k 经济占比: 18%
        """.trimIndent()

        val result = parse(ocr)
        assertFalse(result.isWin!!)
        assertEquals(7800, result.economy)
        assertEquals(4, result.deaths)
        assertEquals("孙尚香", result.hero)
    }
}
