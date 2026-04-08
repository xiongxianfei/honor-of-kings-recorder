package com.xiongxianfei.honorkingsrecorder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotParserTest {

    private fun parse(text: String) = ScreenshotParser.parse(text)

    // ── Win / Loss ──────────────────────────────────────────────────────────

    @Test fun isWin_victorySingleWord_returnsTrue() = assertTrue(parse("胜利").isWin!!)
    @Test fun isWin_ourSideVictory_returnsTrue()    = assertTrue(parse("我方胜利").isWin!!)
    @Test fun isWin_defeatWord_returnsFalse()       = assertFalse(parse("失败").isWin!!)
    @Test fun isWin_ourSideDefeat_returnsFalse()    = assertFalse(parse("我方失败").isWin!!)
    @Test fun isWin_noResultWord_returnsNull()      = assertNull(parse("经济 8000").isWin)

    // ── Hero ────────────────────────────────────────────────────────────────

    @Test fun hero_houyiInText_recognized()  = assertEquals("后羿",  parse("后羿 经济 8000").hero)
    @Test fun hero_goyaInText_recognized()   = assertEquals("戈娅",  parse("战绩 戈娅").hero)
    @Test fun hero_notInText_returnsNull()   = assertNull(parse("胜利\n11/1/5\n经济: 13.1k").hero)
    @Test fun hero_unknownHero_returnsNull() = assertNull(parse("韩信 胜利").hero)

    // ── Economy — k-suffix format (primary) ──────────────────────────────────

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
    fun economy_kSuffix_ocrSpaceInsideNumber() {
        // OCR may insert a space: "13. 1k" or "13 .1k"
        assertEquals(13100, parse("经济: 13. 1k").economy)
    }

    @Test
    fun economy_kSuffix_ocrSpaceBeforeK() {
        assertEquals(13100, parse("经济: 13.1 k").economy)
    }

    @Test
    fun economy_kSuffix_lowValue() {
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

    // ── KDA — kills / deaths / assists ────────────────────────────────────────

    @Test
    fun kda_11_1_5_allFieldsCorrect() {
        val r = parse("11/1/5")
        assertEquals(11, r.kills)
        assertEquals(1,  r.deaths)
        assertEquals(5,  r.assists)
    }

    @Test
    fun kda_withSpacesAroundSlashes_parsedCorrectly() {
        val r = parse("11 / 1 / 5")
        assertEquals(11, r.kills)
        assertEquals(1,  r.deaths)
        assertEquals(5,  r.assists)
    }

    @Test
    fun kda_zeroDeaths_parsedCorrectly() {
        val r = parse("8/0/10")
        assertEquals(8,  r.kills)
        assertEquals(0,  r.deaths)
        assertEquals(10, r.assists)
    }

    @Test
    fun kda_allZeros() {
        val r = parse("0/0/0")
        assertEquals(0, r.kills)
        assertEquals(0, r.deaths)
        assertEquals(0, r.assists)
    }

    @Test
    fun kda_noKdaPattern_allNull() {
        val r = parse("胜利 经济: 8.0k")
        assertNull(r.kills)
        assertNull(r.deaths)
        assertNull(r.assists)
    }

    // ── Deaths — explicit label fallback ────────────────────────────────────

    @Test
    fun deaths_explicitLabel_withColon() {
        assertEquals(3, parse("死亡:3").deaths)
    }

    @Test
    fun deaths_explicitCountLabel() {
        assertEquals(4, parse("死亡次数:4").deaths)
    }

    // ── Real screenshot simulation ────────────────────────────────────────────

    @Test
    fun realScreenshot_winMvpGame_parsesCorrectly() {
        // Simulates OCR output from the provided 王者荣耀 screenshot
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
        // Hero name not in text on this tab — null is correct
        assertNull(r.hero)
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

        val r = parse(ocr)
        assertFalse(r.isWin!!)
        assertEquals(7800, r.economy)
        assertEquals(5, r.kills)
        assertEquals(4, r.deaths)
        assertEquals(2, r.assists)
        assertEquals("孙尚香", r.hero)
    }

    @Test
    fun realScreenshot_ocrNoise_spacesInEconomy() {
        // OCR may fragment "13.1k" into "13. 1 k"
        val ocr = "胜利\n11/1/5\n经济: 13. 1 k"
        val r = parse(ocr)
        assertEquals(13100, r.economy)
        assertEquals(1, r.deaths)
    }
}
