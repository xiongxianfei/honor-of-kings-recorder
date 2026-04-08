package com.xiongxianfei.honorkingsrecorder.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun winRate_zeroMatches_returnsZero() {
        val state = HomeUiState(totalMatches = 0, wins = 0)
        assertEquals(0f, state.winRate, 0.001f)
    }

    @Test
    fun winRate_allWins_returns100() {
        val state = HomeUiState(totalMatches = 5, wins = 5)
        assertEquals(100f, state.winRate, 0.001f)
    }

    @Test
    fun winRate_halfWins_returns50() {
        val state = HomeUiState(totalMatches = 10, wins = 5)
        assertEquals(50f, state.winRate, 0.001f)
    }

    @Test
    fun winRate_oneWin_returnsCorrectPercentage() {
        val state = HomeUiState(totalMatches = 4, wins = 1)
        assertEquals(25f, state.winRate, 0.001f)
    }

    @Test
    fun winRate_noWins_returnsZero() {
        val state = HomeUiState(totalMatches = 3, wins = 0)
        assertEquals(0f, state.winRate, 0.001f)
    }

    @Test
    fun defaults_areZeroAndEmpty() {
        val state = HomeUiState()
        assertEquals(0, state.totalMatches)
        assertEquals(0, state.wins)
        assertEquals(0f, state.avgScore, 0.001f)
        assertEquals(emptyList<Any>(), state.recentMatches)
        assertEquals(0f, state.winRate, 0.001f)
    }
}
