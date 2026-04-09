package com.xiongxianfei.honorkingsrecorder.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.ui.theme.LossRed
import com.xiongxianfei.honorkingsrecorder.ui.theme.WinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit = {},
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val match by viewModel.match.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("对局详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(viewModel.matchId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { innerPadding ->
        val m = match
        if (m == null) {
            // Loading or not found
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header: hero, win/loss, date, score ─────────────────────
            HeaderSection(m)

            HorizontalDivider()

            // ── KDA & Economy ───────────────────────────────────────────
            StatsSection(m)

            HorizontalDivider()

            // ── Score breakdown ─────────────────────────────────────────
            ScoreBreakdownSection(m)

            // ── Notes ───────────────────────────────────────────────────
            if (m.notes.isNotBlank()) {
                HorizontalDivider()
                NotesSection(m.notes)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HeaderSection(match: Match) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        .format(Date(match.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(match.hero, style = MaterialTheme.typography.headlineMedium)
            Text(
                dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            SuggestionChip(
                onClick = {},
                label = { Text(if (match.isWin) "胜" else "负") },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (match.isWin) WinGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f),
                    labelColor = if (match.isWin) WinGreen else LossRed
                )
            )
            Text(
                "${match.score}分",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun StatsSection(match: Match) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("击杀", match.kills.toString())
        StatItem("死亡", match.deaths.toString())
        StatItem("助攻", match.assists.toString())
        StatItem("经济", match.economy.toString())
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScoreBreakdownSection(match: Match) {
    Text("得分明细", style = MaterialTheme.typography.titleMedium)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ScoreRow("经济 \u2265 6500", match.economy >= 6500, 15)
            ScoreRow("死亡 \u2264 2", match.deaths <= 2, 10)
            ScoreRow("击杀主宰", match.killedBaron, 10)
            ScoreRow("灵魂三问", match.threeQuestionCheck, 10)
            ScoreRow("依赖队友", match.reliedOnTeam, 10)
            ScoreRow("推塔", match.pushedTower, 10)
            ScoreRow("打最强", match.engagedStrongest, 10)
            ScoreRow("心态稳定", match.mentalStability, 15)
            ScoreRow("复盘笔记", match.notes.isNotBlank(), 10)
        }
    }
}

@Composable
private fun ScoreRow(label: String, achieved: Boolean, points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (achieved) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (achieved) WinGreen else LossRed.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            if (achieved) "+$points" else "0",
            style = MaterialTheme.typography.bodyMedium,
            color = if (achieved) WinGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotesSection(notes: String) {
    Text("复盘笔记", style = MaterialTheme.typography.titleMedium)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            notes,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}
