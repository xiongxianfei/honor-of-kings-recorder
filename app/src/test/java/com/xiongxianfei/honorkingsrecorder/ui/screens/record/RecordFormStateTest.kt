package com.xiongxianfei.honorkingsrecorder.ui.screens.record

import org.junit.Assert.assertEquals
import org.junit.Test

class RecordFormStateTest {

    // --- economy parsed from text ---

    @Test
    fun economy_emptyString_returnsZero() {
        val form = RecordFormState(economyText = "")
        assertEquals(0, form.economy)
    }

    @Test
    fun economy_validNumber_returnsParsed() {
        val form = RecordFormState(economyText = "7500")
        assertEquals(7500, form.economy)
    }

    @Test
    fun economy_aboveThreshold_parsedCorrectly() {
        val form = RecordFormState(economyText = "6500")
        assertEquals(6500, form.economy)
    }

    // --- deaths parsed from text ---

    @Test
    fun deaths_emptyString_returnsZero() {
        val form = RecordFormState(deathsText = "")
        assertEquals(0, form.deaths)
    }

    @Test
    fun deaths_validNumber_returnsParsed() {
        val form = RecordFormState(deathsText = "3")
        assertEquals(3, form.deaths)
    }

    // --- score computed correctly from form state ---

    @Test
    fun score_defaultForm_allBooleansFalse_returnsZero() {
        // default deaths text is empty → deaths=0 → +10; economy empty → 0; no booleans
        // deaths=0 ≤ 2 → +10
        val form = RecordFormState()
        assertEquals(10, form.score)
    }

    @Test
    fun score_fullForm_returns100() {
        val form = RecordFormState(
            economyText = "9000",
            deathsText = "0",
            killedBaron = true,
            threeQuestionCheck = true,
            reliedOnTeam = true,
            pushedTower = true,
            engagedStrongest = true,
            mentalStability = true,
            notes = "great"
        )
        assertEquals(100, form.score)
    }

    @Test
    fun score_economyJustBelow_noEconomyBonus() {
        val form = RecordFormState(
            economyText = "6499",
            deathsText = "5"  // deaths > 2, no bonus
        )
        assertEquals(0, form.score)
    }

    @Test
    fun score_economyAtThreshold_economyBonus() {
        val form = RecordFormState(economyText = "6500", deathsText = "5")
        assertEquals(15, form.score)
    }

    @Test
    fun score_deathsTwo_deathBonus() {
        val form = RecordFormState(economyText = "0", deathsText = "2")
        assertEquals(10, form.score)
    }

    @Test
    fun score_deathsThree_noDeathBonus() {
        val form = RecordFormState(economyText = "0", deathsText = "3")
        assertEquals(0, form.score)
    }

    @Test
    fun defaultHero_isFirstInList() {
        val form = RecordFormState()
        assertEquals(HEROES.first(), form.hero)
    }

    @Test
    fun defaultIsWin_isTrue() {
        val form = RecordFormState()
        assertEquals(true, form.isWin)
    }

    @Test
    fun saved_defaultsFalse() {
        val form = RecordFormState()
        assertEquals(false, form.saved)
    }
}
