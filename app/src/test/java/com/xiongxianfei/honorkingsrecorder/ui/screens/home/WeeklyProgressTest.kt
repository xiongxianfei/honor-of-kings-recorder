package com.xiongxianfei.honorkingsrecorder.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyProgressTest {

    // ── DeltaMetric ─────────────────────────────────────────────────────

    @Test
    fun deltaMetric_higherIsBetter_positiveIsGood() {
        val m = DeltaMetric("均分", 70f, 5f)
        assertFalse(m.lowerIsBetter)
        assertEquals(5f, m.delta!!, 0.01f)
    }

    @Test
    fun deltaMetric_lowerIsBetter_negativeIsGood() {
        val m = DeltaMetric("死亡", 2f, -1f, lowerIsBetter = true)
        assertTrue(m.lowerIsBetter)
        assertEquals(-1f, m.delta!!, 0.01f)
    }

    @Test
    fun deltaMetric_noDelta_lastWeekMissing() {
        val m = DeltaMetric("均分", 70f, null)
        assertNull(m.delta)
    }

    // ── WeakSpot ────────────────────────────────────────────────────────

    @Test
    fun weakSpot_showsHitsAndTotal() {
        val ws = WeakSpot("推塔", 2, 7)
        assertEquals("推塔", ws.label)
        assertEquals(2, ws.hits)
        assertEquals(7, ws.total)
    }

    // ── WeeklyProgress ──────────────────────────────────────────────────

    @Test
    fun weeklyProgress_matchCount_correct() {
        val wp = WeeklyProgress(matchCount = 5)
        assertEquals(5, wp.matchCount)
    }

    @Test
    fun weeklyProgress_allCriteriaStrong_noWeakSpot() {
        val wp = WeeklyProgress(
            matchCount = 5,
            allCriteriaStrong = true,
            weakSpot = null
        )
        assertTrue(wp.allCriteriaStrong)
        assertNull(wp.weakSpot)
    }

    @Test
    fun weeklyProgress_hasWeakSpot_notAllStrong() {
        val wp = WeeklyProgress(
            matchCount = 5,
            allCriteriaStrong = false,
            weakSpot = WeakSpot("推塔", 1, 5)
        )
        assertFalse(wp.allCriteriaStrong)
        assertNotNull(wp.weakSpot)
        assertEquals("推塔", wp.weakSpot!!.label)
    }

    @Test
    fun defaultWeeklyProgress_isEmpty() {
        val wp = WeeklyProgress()
        assertEquals(0, wp.matchCount)
        assertTrue(wp.metrics.isEmpty())
        assertNull(wp.weakSpot)
        assertFalse(wp.allCriteriaStrong)
    }

    // ── HomeUiState with weeklyProgress ─────────────────────────────────

    @Test
    fun homeUiState_noWeeklyProgress_isNull() {
        val state = HomeUiState()
        assertNull(state.weeklyProgress)
    }

    @Test
    fun homeUiState_withWeeklyProgress() {
        val wp = WeeklyProgress(matchCount = 3)
        val state = HomeUiState(weeklyProgress = wp)
        assertNotNull(state.weeklyProgress)
        assertEquals(3, state.weeklyProgress!!.matchCount)
    }
}
