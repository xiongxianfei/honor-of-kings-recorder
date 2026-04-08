package com.xiongxianfei.honorkingsrecorder.ui.screens.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import com.xiongxianfei.honorkingsrecorder.util.ScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val HEROES = listOf("后羿", "莱西奥", "艾琳", "戈娅", "孙尚香", "公孙离")

data class RecordFormState(
    val hero: String = HEROES.first(),
    val isWin: Boolean = true,
    val economyText: String = "",
    val deathsText: String = "",
    val killedBaron: Boolean = false,
    val threeQuestionCheck: Boolean = false,
    val reliedOnTeam: Boolean = false,
    val pushedTower: Boolean = false,
    val engagedStrongest: Boolean = false,
    val mentalStability: Boolean = false,
    val notes: String = "",
    val saved: Boolean = false
) {
    val economy: Int get() = economyText.toIntOrNull() ?: 0
    val deaths: Int get() = deathsText.toIntOrNull() ?: 0
    val score: Int get() = ScoreCalculator.calculate(
        economy, deaths, killedBaron, threeQuestionCheck, reliedOnTeam,
        pushedTower, engagedStrongest, mentalStability, notes
    )
}

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repo: MatchRepository
) : ViewModel() {

    private val _form = MutableStateFlow(RecordFormState())
    val form: StateFlow<RecordFormState> = _form.asStateFlow()

    fun onHeroChange(hero: String) = _form.update { it.copy(hero = hero) }
    fun onWinChange(isWin: Boolean) = _form.update { it.copy(isWin = isWin) }
    fun onEconomyChange(text: String) = _form.update { it.copy(economyText = text.filter(Char::isDigit)) }
    fun onDeathsChange(text: String) = _form.update { it.copy(deathsText = text.filter(Char::isDigit)) }
    fun onKilledBaronChange(v: Boolean) = _form.update { it.copy(killedBaron = v) }
    fun onThreeQuestionChange(v: Boolean) = _form.update { it.copy(threeQuestionCheck = v) }
    fun onReliedOnTeamChange(v: Boolean) = _form.update { it.copy(reliedOnTeam = v) }
    fun onPushedTowerChange(v: Boolean) = _form.update { it.copy(pushedTower = v) }
    fun onEngagedStrongestChange(v: Boolean) = _form.update { it.copy(engagedStrongest = v) }
    fun onMentalStabilityChange(v: Boolean) = _form.update { it.copy(mentalStability = v) }
    fun onNotesChange(text: String) = _form.update { it.copy(notes = text) }

    fun save() {
        val f = _form.value
        viewModelScope.launch {
            repo.insert(
                Match(
                    hero = f.hero,
                    timestamp = System.currentTimeMillis(),
                    isWin = f.isWin,
                    economy = f.economy,
                    deaths = f.deaths,
                    killedBaron = f.killedBaron,
                    threeQuestionCheck = f.threeQuestionCheck,
                    reliedOnTeam = f.reliedOnTeam,
                    pushedTower = f.pushedTower,
                    engagedStrongest = f.engagedStrongest,
                    mentalStability = f.mentalStability,
                    notes = f.notes,
                    score = f.score
                )
            )
            _form.value = RecordFormState(saved = true)
        }
    }

    fun onSaveAcknowledged() = _form.update { it.copy(saved = false) }
}
