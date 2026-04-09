package com.xiongxianfei.honorkingsrecorder.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.ui.theme.LossRed
import com.xiongxianfei.honorkingsrecorder.ui.theme.WinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onMatchClick: (Long) -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle(initialValue = HomeUiState())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "王者录 — 主页",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            StatsRow(state)
        }

        if (state.recentMatches.isNotEmpty()) {
            item {
                Text(
                    text = "最近对局",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.recentMatches) { match ->
                MatchSummaryCard(match, onClick = { onMatchClick(match.id) })
            }
        } else {
            item {
                Text(
                    text = "还没有记录，快去记录一局吧！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatsRow(state: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "总场次",
            value = state.totalMatches.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "胜率",
            value = "%.0f%%".format(state.winRate),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "均分",
            value = "%.1f".format(state.avgScore),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchSummaryCard(match: Match, onClick: () -> Unit = {}) {
    val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        .format(Date(match.timestamp))

    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = match.hero, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.score}分",
                    style = MaterialTheme.typography.titleMedium
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(if (match.isWin) "胜" else "负") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (match.isWin) WinGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f),
                        labelColor = if (match.isWin) WinGreen else LossRed
                    )
                )
            }
        }
    }
}
