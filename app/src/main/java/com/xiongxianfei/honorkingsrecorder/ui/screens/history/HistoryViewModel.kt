package com.xiongxianfei.honorkingsrecorder.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: MatchRepository
) : ViewModel() {

    val matches = repo.allMatches

    fun delete(match: Match) {
        viewModelScope.launch { repo.delete(match) }
    }
}
