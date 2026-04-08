package com.xiongxianfei.honorkingsrecorder.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "主页", Icons.Filled.Home)
    data object Record : Screen("record", "记录", Icons.Filled.Add)
    data object History : Screen("history", "历史", Icons.Filled.History)
    data object Stats : Screen("stats", "统计", Icons.Filled.BarChart)

    companion object {
        val bottomNavItems = listOf(Home, Record, History, Stats)
    }
}
