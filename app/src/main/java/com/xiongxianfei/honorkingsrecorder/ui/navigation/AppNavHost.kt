package com.xiongxianfei.honorkingsrecorder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.xiongxianfei.honorkingsrecorder.ui.screens.detail.MatchDetailScreen
import com.xiongxianfei.honorkingsrecorder.ui.screens.history.HistoryScreen
import com.xiongxianfei.honorkingsrecorder.ui.screens.home.HomeScreen
import com.xiongxianfei.honorkingsrecorder.ui.screens.record.RecordScreen
import com.xiongxianfei.honorkingsrecorder.ui.screens.review.VideoReviewScreen
import com.xiongxianfei.honorkingsrecorder.ui.screens.stats.StatsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            val onMatchClick: (Long) -> Unit = { id ->
                navController.navigate("match_detail/$id")
            }
            composable(Screen.Home.route) { HomeScreen(onMatchClick = onMatchClick) }
            composable(Screen.Record.route) { RecordScreen() }
            composable(Screen.History.route) { HistoryScreen(onMatchClick = onMatchClick) }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Review.route) { VideoReviewScreen() }
            composable(
                Screen.MatchDetail.route,
                arguments = listOf(navArgument("matchId") { type = NavType.LongType })
            ) {
                MatchDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
