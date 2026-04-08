package com.xiongxianfei.honorkingsrecorder.ui.screens.stats

import androidx.lifecycle.ViewModel
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import com.xiongxianfei.honorkingsrecorder.ui.screens.record.HEROES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class StatsUiState(
    // Line chart: last 10 match scores in chronological order
    val recentScores: List<Float> = emptyList(),
    // Bar chart: win rate per hero (0..1)
    val heroWinRates: Map<String, Float> = emptyMap(),
    // Bar chart: score distribution buckets [0-39, 40-59, 60-79, 80-100]
    val scoreDistribution: List<Int> = listOf(0, 0, 0, 0),
    // Bar chart: avg score contribution per category
    val categoryAvgContributions: Map<String, Float> = emptyMap()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repo: MatchRepository
) : ViewModel() {

    val uiState = repo.allMatches.map { matches ->
        if (matches.isEmpty()) return@map StatsUiState()

        val recentScores = matches.take(10).reversed().map { it.score.toFloat() }

        val heroWinRates = HEROES.associateWith { hero ->
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

        StatsUiState(recentScores, heroWinRates, scoreDistribution, categoryAvgContributions)
    }
}
