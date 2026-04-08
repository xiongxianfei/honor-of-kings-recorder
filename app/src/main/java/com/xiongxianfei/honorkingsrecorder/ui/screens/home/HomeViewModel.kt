package com.xiongxianfei.honorkingsrecorder.ui.screens.home

import androidx.lifecycle.ViewModel
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class HomeUiState(
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val avgScore: Float = 0f,
    val recentMatches: List<Match> = emptyList()
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
            recentMatches = matches.take(5)
        )
    }
}
