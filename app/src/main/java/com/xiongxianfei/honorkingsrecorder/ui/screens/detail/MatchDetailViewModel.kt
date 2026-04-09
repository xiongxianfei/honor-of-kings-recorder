package com.xiongxianfei.honorkingsrecorder.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MatchRepository
) : ViewModel() {

    private val matchId: Long = savedStateHandle["matchId"]!!

    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match.asStateFlow()

    init {
        viewModelScope.launch {
            _match.value = repository.getById(matchId)
        }
    }
}
