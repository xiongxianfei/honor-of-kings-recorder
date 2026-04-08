package com.xiongxianfei.honorkingsrecorder.ui.screens.stats

import com.xiongxianfei.honorkingsrecorder.data.model.Match
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the pure stat-computation logic extracted from StatsViewModel.
 * Mirrors the map { } block so we can validate outputs without needing Hilt/Room.
 */
class StatsComputationTest {

    private fun makeMatch(
        id: Long = 0,
        hero: String = "后羿",
        score: Int = 50,
        isWin: Boolean = true,
        economy: Int = 5000,
        deaths: Int = 3,
        killedBaron: Boolean = false,
        threeQuestionCheck: Boolean = false,
        reliedOnTeam: Boolean = false,
        pushedTower: Boolean = false,
        engagedStrongest: Boolean = false,
        mentalStability: Boolean = false,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) = Match(
        id = id, hero = hero, timestamp = timestamp, isWin = isWin,
        economy = economy, deaths = deaths, killedBaron = killedBaron,
        threeQuestionCheck = threeQuestionCheck, reliedOnTeam = reliedOnTeam,
        pushedTower = pushedTower, engagedStrongest = engagedStrongest,
        mentalStability = mentalStability, notes = notes, score = score
    )

    private fun computeStats(matches: List<Match>): StatsUiState {
        if (matches.isEmpty()) return StatsUiState()

        val recentScores = matches.take(10).reversed().map { it.score.toFloat() }

        val heroes = listOf("后羿", "莱西奥", "艾琳", "戈娅", "孙尚香", "公孙离")
        val heroWinRates = heroes.associateWith { hero ->
            val heroMatches = matches.filter { it.hero == hero }
            if (heroMatches.isEmpty()) 0f
            else heroMatches.count { it.isWin }.toFloat() / heroMatches.size
        }.filter { matches.any { m -> m.hero == it.key } }

        val scoreDistribution = listOf(
            matches.count { it.score < 40 },
            matches.count { it.score in 40..59 },
            matches.count { it.score in 60..79 },
            matches.count { it.score >= 80 }
        )

        val total = matches.size.toFloat()
        val categoryAvgContributions = mapOf(
            "经济" to matches.count { it.economy >= 6500 } * 15f / total,
            "死亡" to matches.count { it.deaths <= 2 } * 10f / total,
            "大龙" to matches.count { it.killedBaron } * 10f / total,
            "三问" to matches.count { it.threeQuestionCheck } * 10f / total,
            "依托" to matches.count { it.reliedOnTeam } * 10f / total,
            "推塔" to matches.count { it.pushedTower } * 10f / total,
            "对线" to matches.count { it.engagedStrongest } * 10f / total,
            "心态" to matches.count { it.mentalStability } * 15f / total,
            "备注" to matches.count { it.notes.isNotBlank() } * 10f / total
        )

        return StatsUiState(recentScores, heroWinRates, scoreDistribution, categoryAvgContributions)
    }

    // --- Empty ---

    @Test
    fun emptyMatches_returnsDefaultState() {
        val state = computeStats(emptyList())
        assertTrue(state.recentScores.isEmpty())
        assertTrue(state.heroWinRates.isEmpty())
        assertEquals(listOf(0, 0, 0, 0), state.scoreDistribution)
        assertTrue(state.categoryAvgContributions.isEmpty())
    }

    // --- recentScores ---

    @Test
    fun recentScores_singleMatch_returnsSingleElement() {
        val matches = listOf(makeMatch(score = 75))
        val state = computeStats(matches)
        assertEquals(listOf(75f), state.recentScores)
    }

    @Test
    fun recentScores_fewerThan10_returnsAll() {
        val matches = (1..5).map { makeMatch(score = it * 10) }
        val state = computeStats(matches)
        assertEquals(5, state.recentScores.size)
    }

    @Test
    fun recentScores_moreThan10_takesFirst10AndReverses() {
        // matches list is already sorted desc by timestamp (most recent first)
        // take(10) → last 10 most-recent; reversed() → chronological order for line chart
        val matches = (1..15).map { makeMatch(score = it) }
        val state = computeStats(matches)
        assertEquals(10, state.recentScores.size)
        // take(10) picks scores 1..10; reversed → 10,9,8,...,1
        assertEquals(10f, state.recentScores.first(), 0.001f)
        assertEquals(1f, state.recentScores.last(), 0.001f)
    }

    // --- heroWinRates ---

    @Test
    fun heroWinRates_allWins_returns1() {
        val matches = listOf(
            makeMatch(hero = "后羿", isWin = true),
            makeMatch(hero = "后羿", isWin = true)
        )
        val state = computeStats(matches)
        assertEquals(1f, state.heroWinRates["后羿"]!!, 0.001f)
    }

    @Test
    fun heroWinRates_halfWins_returns0_5() {
        val matches = listOf(
            makeMatch(hero = "艾琳", isWin = true),
            makeMatch(hero = "艾琳", isWin = false)
        )
        val state = computeStats(matches)
        assertEquals(0.5f, state.heroWinRates["艾琳"]!!, 0.001f)
    }

    @Test
    fun heroWinRates_heroNotInMatches_notIncluded() {
        val matches = listOf(makeMatch(hero = "后羿", isWin = true))
        val state = computeStats(matches)
        assertTrue("莱西奥 should not be in heroWinRates", !state.heroWinRates.containsKey("莱西奥"))
    }

    @Test
    fun heroWinRates_multipleHeroes_eachCalculatedIndependently() {
        val matches = listOf(
            makeMatch(hero = "后羿", isWin = true),
            makeMatch(hero = "后羿", isWin = false),
            makeMatch(hero = "戈娅", isWin = true),
            makeMatch(hero = "戈娅", isWin = true),
            makeMatch(hero = "戈娅", isWin = true)
        )
        val state = computeStats(matches)
        assertEquals(0.5f, state.heroWinRates["后羿"]!!, 0.001f)
        assertEquals(1.0f, state.heroWinRates["戈娅"]!!, 0.001f)
    }

    // --- scoreDistribution ---

    @Test
    fun scoreDistribution_bucketBoundaries() {
        val matches = listOf(
            makeMatch(score = 0),   // bucket 0: < 40
            makeMatch(score = 39),  // bucket 0
            makeMatch(score = 40),  // bucket 1: 40-59
            makeMatch(score = 59),  // bucket 1
            makeMatch(score = 60),  // bucket 2: 60-79
            makeMatch(score = 79),  // bucket 2
            makeMatch(score = 80),  // bucket 3: ≥ 80
            makeMatch(score = 100)  // bucket 3
        )
        val state = computeStats(matches)
        assertEquals(listOf(2, 2, 2, 2), state.scoreDistribution)
    }

    @Test
    fun scoreDistribution_allInOneBucket() {
        val matches = (1..5).map { makeMatch(score = 90) }
        val state = computeStats(matches)
        assertEquals(listOf(0, 0, 0, 5), state.scoreDistribution)
    }

    @Test
    fun scoreDistribution_sumEqualsMatchCount() {
        val matches = (1..12).map { makeMatch(score = it * 7) }
        val state = computeStats(matches)
        assertEquals(matches.size, state.scoreDistribution.sum())
    }

    // --- categoryAvgContributions ---

    @Test
    fun categoryAvgContributions_hasAllNineCategories() {
        val matches = listOf(makeMatch())
        val state = computeStats(matches)
        val expected = setOf("经济", "死亡", "大龙", "三问", "依托", "推塔", "对线", "心态", "备注")
        assertEquals(expected, state.categoryAvgContributions.keys)
    }

    @Test
    fun categoryAvgContributions_allQualify_returnsMaxValues() {
        val match = makeMatch(
            economy = 7000, deaths = 1,
            killedBaron = true, threeQuestionCheck = true,
            reliedOnTeam = true, pushedTower = true,
            engagedStrongest = true, mentalStability = true,
            notes = "good"
        )
        val state = computeStats(listOf(match))
        val c = state.categoryAvgContributions
        assertEquals(15f, c["经济"]!!, 0.001f)
        assertEquals(10f, c["死亡"]!!, 0.001f)
        assertEquals(10f, c["大龙"]!!, 0.001f)
        assertEquals(10f, c["三问"]!!, 0.001f)
        assertEquals(10f, c["依托"]!!, 0.001f)
        assertEquals(10f, c["推塔"]!!, 0.001f)
        assertEquals(10f, c["对线"]!!, 0.001f)
        assertEquals(15f, c["心态"]!!, 0.001f)
        assertEquals(10f, c["备注"]!!, 0.001f)
    }

    @Test
    fun categoryAvgContributions_noneQualify_returnsZeros() {
        val match = makeMatch(economy = 100, deaths = 5, notes = "")
        val state = computeStats(listOf(match))
        state.categoryAvgContributions.values.forEach { v ->
            assertEquals(0f, v, 0.001f)
        }
    }

    @Test
    fun categoryAvgContributions_halfQualify_returnsHalfMax() {
        val matches = listOf(
            makeMatch(killedBaron = true),
            makeMatch(killedBaron = false)
        )
        val state = computeStats(matches)
        assertEquals(5f, state.categoryAvgContributions["大龙"]!!, 0.001f)
    }
}
