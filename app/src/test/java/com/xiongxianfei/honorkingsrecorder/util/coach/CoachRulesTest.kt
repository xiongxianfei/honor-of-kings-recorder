package com.xiongxianfei.honorkingsrecorder.util.coach

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class CoachRulesTest {

    private fun frame(
        tsMs: Long,
        economy: Int? = null,
        deaths: Int? = null,
        kills: Int? = null,
        assists: Int? = null
    ) = FrameData(tsMs, economy, deaths, kills, assists)

    private val ts430  = 270_000L
    private val ts730  = 450_000L
    private val ts1030 = 630_000L

    // ── EconomyAt430 ─────────────────────────────────────────────────────────

    @Test fun economyAt430_belowTarget_isWarning() {
        val tip = CoachRules.EconomyAt430.evaluate(frame(ts430, economy = 3000))
        assertNotNull(tip)
        assertFalse(tip!!.isPositive)
    }

    @Test fun economyAt430_atTarget_isPositive() {
        val tip = CoachRules.EconomyAt430.evaluate(frame(ts430, economy = 3200))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun economyAt430_aboveTarget_isPositive() {
        val tip = CoachRules.EconomyAt430.evaluate(frame(ts430, economy = 4000))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun economyAt430_wrongTimestamp_returnsNull() {
        assertNull(CoachRules.EconomyAt430.evaluate(frame(ts730, economy = 3000)))
    }

    @Test fun economyAt430_missingEconomy_returnsNull() {
        assertNull(CoachRules.EconomyAt430.evaluate(frame(ts430, economy = null)))
    }

    // ── DeathsAt430 ──────────────────────────────────────────────────────────

    @Test fun deathsAt430_oneOrMore_isWarning() {
        val tip = CoachRules.DeathsAt430.evaluate(frame(ts430, deaths = 1))
        assertNotNull(tip)
        assertFalse(tip!!.isPositive)
    }

    @Test fun deathsAt430_zero_isPositive() {
        val tip = CoachRules.DeathsAt430.evaluate(frame(ts430, deaths = 0))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun deathsAt430_wrongTimestamp_returnsNull() {
        assertNull(CoachRules.DeathsAt430.evaluate(frame(ts730, deaths = 2)))
    }

    @Test fun deathsAt430_missingDeaths_returnsNull() {
        assertNull(CoachRules.DeathsAt430.evaluate(frame(ts430, deaths = null)))
    }

    // ── EconomyAt730 ─────────────────────────────────────────────────────────

    @Test fun economyAt730_belowTarget_isWarning() {
        val tip = CoachRules.EconomyAt730.evaluate(frame(ts730, economy = 6000))
        assertNotNull(tip)
        assertFalse(tip!!.isPositive)
    }

    @Test fun economyAt730_atTarget_isPositive() {
        val tip = CoachRules.EconomyAt730.evaluate(frame(ts730, economy = 6500))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun economyAt730_wrongTimestamp_returnsNull() {
        assertNull(CoachRules.EconomyAt730.evaluate(frame(ts430, economy = 6000)))
    }

    // ── DeathsAt730 ──────────────────────────────────────────────────────────

    @Test fun deathsAt730_twoOrMore_isWarning() {
        val tip = CoachRules.DeathsAt730.evaluate(frame(ts730, deaths = 2))
        assertNotNull(tip)
        assertFalse(tip!!.isPositive)
    }

    @Test fun deathsAt730_oneDeath_isPositive() {
        val tip = CoachRules.DeathsAt730.evaluate(frame(ts730, deaths = 1))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun deathsAt730_wrongTimestamp_returnsNull() {
        assertNull(CoachRules.DeathsAt730.evaluate(frame(ts430, deaths = 3)))
    }

    // ── EconomyAt1030 ────────────────────────────────────────────────────────

    @Test fun economyAt1030_belowTarget_isWarning() {
        val tip = CoachRules.EconomyAt1030.evaluate(frame(ts1030, economy = 8000))
        assertNotNull(tip)
        assertFalse(tip!!.isPositive)
    }

    @Test fun economyAt1030_atTarget_isPositive() {
        val tip = CoachRules.EconomyAt1030.evaluate(frame(ts1030, economy = 9000))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun economyAt1030_wrongTimestamp_returnsNull() {
        assertNull(CoachRules.EconomyAt1030.evaluate(frame(ts730, economy = 8000)))
    }

    // ── DeathsAt1030 ─────────────────────────────────────────────────────────

    @Test fun deathsAt1030_threeOrMore_isWarning() {
        val tip = CoachRules.DeathsAt1030.evaluate(frame(ts1030, deaths = 3))
        assertNotNull(tip)
        assertFalse(tip!!.isPositive)
    }

    @Test fun deathsAt1030_twoDeaths_isPositive() {
        val tip = CoachRules.DeathsAt1030.evaluate(frame(ts1030, deaths = 2))
        assertNotNull(tip)
        assertTrue(tip!!.isPositive)
    }

    @Test fun deathsAt1030_wrongTimestamp_returnsNull() {
        assertNull(CoachRules.DeathsAt1030.evaluate(frame(ts730, deaths = 4)))
    }

    // ── CoachRuleEngine ───────────────────────────────────────────────────────

    @Test fun engine_sortsByTimestamp() {
        val frames = listOf(
            frame(ts1030, economy = 9500, deaths = 1),
            frame(ts430,  economy = 3500, deaths = 0),
            frame(ts730,  economy = 7000, deaths = 1),
        )
        val tips = CoachRuleEngine.analyze(frames)
        assertTrue(tips.isNotEmpty())
        // All tips should be in ascending timestamp order
        tips.zipWithNext().forEach { (a, b) ->
            assertTrue("Tips not sorted: ${a.timestampMs} > ${b.timestampMs}", a.timestampMs <= b.timestampMs)
        }
    }

    @Test fun engine_wrongTimestampFramesProduceNoTips() {
        // A frame at a timestamp no rule covers should yield nothing
        val frames = listOf(frame(99_999L, economy = 500, deaths = 5))
        val tips = CoachRuleEngine.analyze(frames)
        assertTrue(tips.isEmpty())
    }

    @Test fun engine_missingFieldsYieldNoTips() {
        // All fields null — no rule can fire
        val frames = listOf(
            frame(ts430),
            frame(ts730),
            frame(ts1030),
        )
        val tips = CoachRuleEngine.analyze(frames)
        assertTrue(tips.isEmpty())
    }

    @Test fun engine_allCheckpoints_allFieldsPresent_produces6Tips() {
        val frames = listOf(
            frame(ts430,  economy = 3500, deaths = 0),
            frame(ts730,  economy = 7000, deaths = 1),
            frame(ts1030, economy = 9500, deaths = 2),
        )
        val tips = CoachRuleEngine.analyze(frames)
        // 2 rules per checkpoint × 3 checkpoints = 6 tips
        assertEquals(6, tips.size)
    }

    @Test fun engine_tipTimestampMatchesFrameTimestamp() {
        val frames = listOf(frame(ts430, economy = 2000, deaths = 1))
        val tips = CoachRuleEngine.analyze(frames)
        assertTrue(tips.isNotEmpty())
        tips.forEach { assertEquals(ts430, it.timestampMs) }
    }
}
