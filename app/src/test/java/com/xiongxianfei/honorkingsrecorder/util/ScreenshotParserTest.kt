package com.xiongxianfei.honorkingsrecorder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotParserTest {

    private fun parse(text: String) = ScreenshotParser.parse(text)

    // ── Win / Loss ──────────────────────────────────────────────────────────

    @Test fun isWin_victorySingleWord_returnsTrue()  = assertTrue(parse("胜利").isWin!!)
    @Test fun isWin_ourSideVictory_returnsTrue()     = assertTrue(parse("我方胜利").isWin!!)
    @Test fun isWin_defeatWord_returnsFalse()        = assertFalse(parse("失败").isWin!!)
    @Test fun isWin_ourSideDefeat_returnsFalse()     = assertFalse(parse("我方失败").isWin!!)
    @Test fun isWin_noResultWord_returnsNull()       = assertNull(parse("经济 8000").isWin)

    // ── Hero ────────────────────────────────────────────────────────────────

    @Test fun hero_houyiInText()     = assertEquals("后羿",  parse("后羿 经济 8000").hero)
    @Test fun hero_goyaInText()      = assertEquals("戈娅",  parse("战绩 戈娅").hero)
    @Test fun hero_notInText()       = assertNull(parse("胜利\n11/1/5").hero)
    @Test fun hero_unknownHero()     = assertNull(parse("韩信 胜利").hero)

    // ── Economy — Strategy 1: decimal before KDA in header ────────────────
    // e.g. "不败、菜鸟 13.1 11/1/5" — the most reliable pattern for this game

    @Test
    fun economy_s1_decimalBeforeKda_returnsThousands() {
        assertEquals(13100, parse("不败、菜鸟 13.1 11/1/5").economy)
    }

    @Test
    fun economy_s1_smallValue_returnsThousands() {
        assertEquals(5200, parse("player 5.2 3/2/1").economy)
    }

    @Test
    fun economy_s1_zeroPointFiveK() {
        assertEquals(500, parse("x 0.5 1/0/2").economy)
    }

    @Test
    fun economy_s1_withSpacesAroundKdaSlashes() {
        assertEquals(13100, parse("13.1 11 / 1 / 5").economy)
    }

    // ── Economy — Strategy 2: "经济" label + k-suffix ─────────────────────

    @Test
    fun economy_s2_labelWithSpace_13point1k() {
        assertEquals(13100, parse("经济: 13.1k").economy)
    }

    @Test
    fun economy_s2_labelFullWidthColon() {
        assertEquals(8500, parse("经济：8.5k").economy)
    }

    @Test
    fun economy_s2_labelNoColon() {
        assertEquals(9000, parse("经济 9k").economy)
    }

    @Test
    fun economy_s2_uppercaseK() {
        assertEquals(12000, parse("经济: 12K").economy)
    }

    @Test
    fun economy_s2_ocrSpaceInsideNumber() {
        // OCR may insert a space: "13. 1k"
        assertEquals(13100, parse("经济: 13. 1k").economy)
    }

    @Test
    fun economy_s2_ocrSpaceBeforeK() {
        assertEquals(13100, parse("经济: 13.1 k").economy)
    }

    // ── Economy — Strategy 3: "经济" label + decimal, k dropped ──────────

    @Test
    fun economy_s3_decimalAfterLabelNoK() {
        assertEquals(7800, parse("经济: 7.8").economy)
    }

    @Test
    fun economy_s3_decimalOnNewLine() {
        assertEquals(9300, parse("经济\n9.3").economy)
    }

    // ── Economy — Strategy 4: "经济" label + plain integer ───────────────

    @Test
    fun economy_s4_plainInteger() {
        assertEquals(7200, parse("经济:7200").economy)
    }

    @Test
    fun economy_s4_integerOnNewLine() {
        assertEquals(9300, parse("经济\n9300").economy)
    }

    // ── Economy — no match → rawOcrHint populated ────────────────────────

    @Test
    fun economy_noMatch_rawOcrHintNotNull() {
        val result = parse("胜利 5/1/3")
        assertNull(result.economy)
        assertNotNull(result.rawOcrHint)
    }

    @Test
    fun economy_matched_rawOcrHintNull() {
        val result = parse("胜利 13.1 11/1/5")
        assertNotNull(result.economy)
        assertNull(result.rawOcrHint)
    }

    // ── KDA ───────────────────────────────────────────────────────────────

    @Test
    fun kda_11_1_5_allFieldsCorrect() {
        val r = parse("11/1/5")
        assertEquals(11, r.kills); assertEquals(1, r.deaths); assertEquals(5, r.assists)
    }

    @Test
    fun kda_withSpacesAroundSlashes() {
        val r = parse("11 / 1 / 5")
        assertEquals(11, r.kills); assertEquals(1, r.deaths); assertEquals(5, r.assists)
    }

    @Test
    fun kda_zeroDeaths() {
        val r = parse("8/0/10")
        assertEquals(8, r.kills); assertEquals(0, r.deaths); assertEquals(10, r.assists)
    }

    @Test
    fun kda_noPattern_allNull() {
        val r = parse("胜利 经济: 8.0k")
        assertNull(r.kills); assertNull(r.deaths); assertNull(r.assists)
    }

    // ── Deaths fallback label ─────────────────────────────────────────────

    @Test fun deaths_explicitLabel()      = assertEquals(3, parse("死亡:3").deaths)
    @Test fun deaths_explicitCountLabel() = assertEquals(4, parse("死亡次数:4").deaths)

    // ── Full real-screenshot simulation ───────────────────────────────────

    @Test
    fun realScreenshot_winMvpGame_parsesCorrectly() {
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

        val r = parse(ocr)
        assertTrue(r.isWin!!)
        assertEquals(13100, r.economy)
        assertEquals(11, r.kills)
        assertEquals(1,  r.deaths)
        assertEquals(5,  r.assists)
        assertNull(r.hero)           // hero not shown as text in this tab
        assertNull(r.rawOcrHint)     // economy was found → no debug hint
    }

    @Test
    fun realScreenshot_lossGame_parsesCorrectly() {
        val ocr = """
            失败
            孙尚香
            7.8 5/4/2
            我方失败
            经济: 7.8k 经济占比: 18%
        """.trimIndent()

        val r = parse(ocr)
        assertFalse(r.isWin!!)
        assertEquals(7800, r.economy)
        assertEquals(5, r.kills); assertEquals(4, r.deaths); assertEquals(2, r.assists)
        assertEquals("孙尚香", r.hero)
    }

    @Test
    fun realScreenshot_ocrNoise_spacesEverywhere() {
        // OCR may fragment: "13. 1 k" and "11 / 1 / 5"
        val ocr = "胜利\n不败 13.1 11 / 1 / 5\n经济: 13. 1 k"
        val r = parse(ocr)
        // Strategy 1 catches it from the header
        assertEquals(13100, r.economy)
        assertEquals(1, r.deaths)
    }

    @Test
    fun realScreenshot_kDropped_strategy3Catches() {
        // k suffix dropped by OCR
        val ocr = "胜利\n经济: 13.1\n4/2/7"
        val r = parse(ocr)
        assertEquals(13100, r.economy)
    }
}
