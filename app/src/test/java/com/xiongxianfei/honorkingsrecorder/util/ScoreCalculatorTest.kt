package com.xiongxianfei.honorkingsrecorder.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    private fun calc(
        economy: Int = 0,
        deaths: Int = 3,
        killedBaron: Boolean = false,
        threeQuestionCheck: Boolean = false,
        reliedOnTeam: Boolean = false,
        pushedTower: Boolean = false,
        engagedStrongest: Boolean = false,
        mentalStability: Boolean = false,
        notes: String = ""
    ) = ScoreCalculator.calculate(
        economy, deaths, killedBaron, threeQuestionCheck,
        reliedOnTeam, pushedTower, engagedStrongest, mentalStability, notes
    )

    @Test
    fun zeroScore_whenAllFalseAndPoorStats() {
        assertEquals(0, calc())
    }

    @Test
    fun perfectScore_whenAllCriteriaMaxed() {
        val score = calc(
            economy = 6500,
            deaths = 0,
            killedBaron = true,
            threeQuestionCheck = true,
            reliedOnTeam = true,
            pushedTower = true,
            engagedStrongest = true,
            mentalStability = true,
            notes = "great game"
        )
        assertEquals(100, score)
    }

    // --- Economy (+15) ---

    @Test
    fun economy_exactThreshold_adds15() {
        assertEquals(15, calc(economy = 6500))
    }

    @Test
    fun economy_above_adds15() {
        assertEquals(15, calc(economy = 9999))
    }

    @Test
    fun economy_oneBelowThreshold_addsNothing() {
        assertEquals(0, calc(economy = 6499))
    }

    @Test
    fun economy_zero_addsNothing() {
        assertEquals(0, calc(economy = 0))
    }

    // --- Deaths (+10) ---

    @Test
    fun deaths_zero_adds10() {
        assertEquals(10, calc(deaths = 0))
    }

    @Test
    fun deaths_two_adds10() {
        assertEquals(10, calc(deaths = 2))
    }

    @Test
    fun deaths_three_addsNothing() {
        assertEquals(0, calc(deaths = 3))
    }

    @Test
    fun deaths_large_addsNothing() {
        assertEquals(0, calc(deaths = 99))
    }

    // --- KilledBaron (+10) ---

    @Test
    fun killedBaron_true_adds10() {
        assertEquals(10, calc(killedBaron = true))
    }

    @Test
    fun killedBaron_false_addsNothing() {
        assertEquals(0, calc(killedBaron = false))
    }

    // --- ThreeQuestionCheck (+10) ---

    @Test
    fun threeQuestionCheck_true_adds10() {
        assertEquals(10, calc(threeQuestionCheck = true))
    }

    // --- ReliedOnTeam (+10) ---

    @Test
    fun reliedOnTeam_true_adds10() {
        assertEquals(10, calc(reliedOnTeam = true))
    }

    // --- PushedTower (+10) ---

    @Test
    fun pushedTower_true_adds10() {
        assertEquals(10, calc(pushedTower = true))
    }

    // --- EngagedStrongest (+10) ---

    @Test
    fun engagedStrongest_true_adds10() {
        assertEquals(10, calc(engagedStrongest = true))
    }

    // --- MentalStability (+15) ---

    @Test
    fun mentalStability_true_adds15() {
        assertEquals(15, calc(mentalStability = true))
    }

    @Test
    fun mentalStability_false_addsNothing() {
        assertEquals(0, calc(mentalStability = false))
    }

    // --- Notes (+10) ---

    @Test
    fun notes_nonBlank_adds10() {
        assertEquals(10, calc(notes = "good performance"))
    }

    @Test
    fun notes_blank_addsNothing() {
        assertEquals(0, calc(notes = ""))
    }

    @Test
    fun notes_whitespaceOnly_addsNothing() {
        assertEquals(0, calc(notes = "   "))
    }

    @Test
    fun notes_singleChar_adds10() {
        assertEquals(10, calc(notes = "x"))
    }

    // --- Combination tests ---

    @Test
    fun economyAndDeaths_correctSum() {
        // 15 + 10 = 25
        assertEquals(25, calc(economy = 7000, deaths = 1))
    }

    @Test
    fun economyAndMentalStability_correctSum() {
        // 15 + 15 = 30
        assertEquals(30, calc(economy = 8000, mentalStability = true))
    }

    @Test
    fun allBooleans_noNumerics_correctSum() {
        // killedBaron(10) + 3Q(10) + team(10) + tower(10) + engage(10) + mental(15) = 65
        val score = calc(
            killedBaron = true,
            threeQuestionCheck = true,
            reliedOnTeam = true,
            pushedTower = true,
            engagedStrongest = true,
            mentalStability = true
        )
        assertEquals(65, score)
    }

    @Test
    fun score_isCommutative_orderDoesNotMatter() {
        val s1 = calc(economy = 7000, deaths = 0, killedBaron = true, notes = "win")
        // 15 + 10 + 10 + 10 = 45
        assertEquals(45, s1)
    }

    @Test
    fun score_neverExceeds100() {
        val score = calc(
            economy = 99999, deaths = 0,
            killedBaron = true, threeQuestionCheck = true,
            reliedOnTeam = true, pushedTower = true,
            engagedStrongest = true, mentalStability = true,
            notes = "max"
        )
        assertEquals(100, score)
    }
}
