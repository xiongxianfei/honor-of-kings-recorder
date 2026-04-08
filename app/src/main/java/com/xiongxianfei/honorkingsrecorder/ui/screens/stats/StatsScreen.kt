package com.xiongxianfei.honorkingsrecorder.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries

@Composable
fun StatsScreen(vm: StatsViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle(initialValue = StatsUiState())

    if (state.recentScores.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "数据不足，先去记录一局吧！",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("数据统计", style = MaterialTheme.typography.headlineMedium)

        // 1. Score trend (line chart)
        ChartCard(title = "近期得分走势") {
            val producer = remember { CartesianChartModelProducer() }
            LaunchedEffect(state.recentScores) {
                producer.runTransaction {
                    lineSeries { series(state.recentScores) }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = producer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // 2. Win rate per hero (bar chart)
        if (state.heroWinRates.isNotEmpty()) {
            ChartCard(title = "各英雄胜率") {
                val producer = remember { CartesianChartModelProducer() }
                val entries = state.heroWinRates.values.toList()
                LaunchedEffect(state.heroWinRates) {
                    producer.runTransaction {
                        columnSeries { series(entries) }
                    }
                }
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(),
                    ),
                    modelProducer = producer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        // 3. Score distribution (bar chart)
        ChartCard(title = "得分分布（0-39 / 40-59 / 60-79 / 80-100）") {
            val producer = remember { CartesianChartModelProducer() }
            LaunchedEffect(state.scoreDistribution) {
                producer.runTransaction {
                    columnSeries { series(state.scoreDistribution.map { it.toFloat() }) }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = producer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // 4. Avg score contribution per category (bar chart)
        ChartCard(title = "各项平均得分贡献") {
            val producer = remember { CartesianChartModelProducer() }
            val values = state.categoryAvgContributions.values.toList()
            LaunchedEffect(state.categoryAvgContributions) {
                producer.runTransaction {
                    columnSeries { series(values) }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = producer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}
