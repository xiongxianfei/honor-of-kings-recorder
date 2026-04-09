package com.xiongxianfei.honorkingsrecorder.ui.screens.home

import androidx.lifecycle.ViewModel
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

data class DeltaMetric(
    val label: String,
    val value: Float,
    val delta: Float?,
    val lowerIsBetter: Boolean = false
)

data class WeakSpot(
    val label: String,
    val hits: Int,
    val total: Int
)

data class WeeklyProgress(
    val matchCount: Int = 0,
    val metrics: List<DeltaMetric> = emptyList(),
    val weakSpot: WeakSpot? = null,
    val allCriteriaStrong: Boolean = false
)

data class HomeUiState(
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val avgScore: Float = 0f,
    val recentMatches: List<Match> = emptyList(),
    val weeklyProgress: WeeklyProgress? = null
) {
    val winRate: Float get() = if (totalMatches == 0) 0f else wins.toFloat() / totalMatches * 100f
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    repo: MatchRepository
) : ViewModel() {

    val uiState = repo.allMatches.map { matches ->
        HomeUiState(
            totalMatches = matches.size,
            wins = matches.count { it.isWin },
            avgScore = if (matches.isEmpty()) 0f else matches.sumOf { it.score }.toFloat() / matches.size,
            recentMatches = matches.take(5),
            weeklyProgress = computeWeeklyProgress(matches)
        )
    }

    private fun mondayOfWeek(weeksAgo: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
        return cal.timeInMillis
    }

    private fun computeWeeklyProgress(allMatches: List<Match>): WeeklyProgress? {
        val thisMonday = mondayOfWeek(0)
        val lastMonday = mondayOfWeek(1)

        val thisWeek = allMatches.filter { it.timestamp >= thisMonday }
        val lastWeek = allMatches.filter { it.timestamp >= lastMonday && it.timestamp < thisMonday }

        if (thisWeek.isEmpty()) return null

        val metrics = buildMetrics(thisWeek, lastWeek)
        val (weakSpot, allStrong) = findWeakSpot(thisWeek)

        return WeeklyProgress(
            matchCount = thisWeek.size,
            metrics = metrics,
            weakSpot = weakSpot,
            allCriteriaStrong = allStrong
        )
    }

    private fun buildMetrics(thisWeek: List<Match>, lastWeek: List<Match>): List<DeltaMetric> {
        val thisAvgScore = thisWeek.sumOf { it.score }.toFloat() / thisWeek.size
        val thisWinRate = thisWeek.count { it.isWin }.toFloat() / thisWeek.size * 100f
        val thisAvgDeaths = thisWeek.sumOf { it.deaths }.toFloat() / thisWeek.size
        val thisAvgEconomy = thisWeek.sumOf { it.economy }.toFloat() / thisWeek.size

        val lastAvgScore = if (lastWeek.isEmpty()) null else lastWeek.sumOf { it.score }.toFloat() / lastWeek.size
        val lastWinRate = if (lastWeek.isEmpty()) null else lastWeek.count { it.isWin }.toFloat() / lastWeek.size * 100f
        val lastAvgDeaths = if (lastWeek.isEmpty()) null else lastWeek.sumOf { it.deaths }.toFloat() / lastWeek.size
        val lastAvgEconomy = if (lastWeek.isEmpty()) null else lastWeek.sumOf { it.economy }.toFloat() / lastWeek.size

        return listOf(
            DeltaMetric("均分", thisAvgScore, lastAvgScore?.let { thisAvgScore - it }),
            DeltaMetric("胜率", thisWinRate, lastWinRate?.let { thisWinRate - it }),
            DeltaMetric("死亡", thisAvgDeaths, lastAvgDeaths?.let { thisAvgDeaths - it }, lowerIsBetter = true),
            DeltaMetric("经济", thisAvgEconomy, lastAvgEconomy?.let { thisAvgEconomy - it }),
        )
    }

    private fun findWeakSpot(thisWeek: List<Match>): Pair<WeakSpot?, Boolean> {
        val total = thisWeek.size
        val criteria = listOf(
            "经济达标" to thisWeek.count { it.economy >= 6500 },
            "死亡控制" to thisWeek.count { it.deaths <= 2 },
            "击杀主宰" to thisWeek.count { it.killedBaron },
            "灵魂三问" to thisWeek.count { it.threeQuestionCheck },
            "依托队友" to thisWeek.count { it.reliedOnTeam },
            "推塔" to thisWeek.count { it.pushedTower },
            "打最强" to thisWeek.count { it.engagedStrongest },
            "心态稳定" to thisWeek.count { it.mentalStability },
            "复盘笔记" to thisWeek.count { it.notes.isNotBlank() },
        )
        val allStrong = criteria.all { (_, hits) -> hits.toFloat() / total >= 0.7f }
        if (allStrong) return null to true

        val (label, hits) = criteria.minBy { it.second }
        return WeakSpot(label, hits, total) to false
    }
}
