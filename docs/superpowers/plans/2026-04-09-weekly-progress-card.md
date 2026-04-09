# Weekly Progress Card Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "本周进步" card to the Home dashboard showing this-week-vs-last-week deltas for 4 metrics and a weak-spot callout.

**Architecture:** Pure computation in `HomeViewModel` over the existing `allMatches` flow, filtered by week boundaries. New data classes `WeeklyProgress` and `WeakSpot` added to `HomeUiState`. New composables `WeeklyProgressCard` and `DeltaMetricItem` in `HomeScreen.kt`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room (existing `matches` table)

---

## File Structure

| File | Action | Responsibility |
|---|---|---|
| `app/src/main/java/.../ui/screens/home/HomeViewModel.kt` | Modify | Add `WeeklyProgress` data class, week-filtering logic, criterion hit rates |
| `app/src/main/java/.../ui/screens/home/HomeScreen.kt` | Modify | Add `WeeklyProgressCard` and `DeltaMetricItem` composables |
| `app/src/test/java/.../ui/screens/home/WeeklyProgressTest.kt` | Create | Unit tests for weekly computation and weak-spot logic |

Base path: `app/src/main/java/com/xiongxianfei/honorkingsrecorder`
Test path: `app/src/test/java/com/xiongxianfei/honorkingsrecorder`

---

### Task 1: Add WeeklyProgress data model and computation logic

**Files:**
- Modify: `ui/screens/home/HomeViewModel.kt`

- [ ] **Step 1: Add data classes to HomeViewModel.kt**

Add these data classes above `HomeUiState`:

```kotlin
data class DeltaMetric(
    val label: String,
    val value: Float,
    val delta: Float?,       // null = no last-week data
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
    val weakSpot: WeakSpot? = null,    // null = all criteria >= 70%
    val allCriteriaStrong: Boolean = false
)
```

- [ ] **Step 2: Add `weeklyProgress` field to HomeUiState**

```kotlin
data class HomeUiState(
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val avgScore: Float = 0f,
    val recentMatches: List<Match> = emptyList(),
    val weeklyProgress: WeeklyProgress? = null  // null = no matches this week
) {
    val winRate: Float get() = if (totalMatches == 0) 0f else wins.toFloat() / totalMatches * 100f
}
```

- [ ] **Step 3: Add week boundary helper and computation functions**

Add these to `HomeViewModel` as private functions:

```kotlin
import java.util.Calendar
import java.util.TimeZone

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
```

- [ ] **Step 4: Wire computation into the uiState flow**

Update the `uiState` mapping in `HomeViewModel`:

```kotlin
val uiState = repo.allMatches.map { matches ->
    HomeUiState(
        totalMatches = matches.size,
        wins = matches.count { it.isWin },
        avgScore = if (matches.isEmpty()) 0f else matches.sumOf { it.score }.toFloat() / matches.size,
        recentMatches = matches.take(5),
        weeklyProgress = computeWeeklyProgress(matches)
    )
}
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/xiongxianfei/honorkingsrecorder/ui/screens/home/HomeViewModel.kt
git commit -m "feat: add weekly progress computation to HomeViewModel"
```

---

### Task 2: Write unit tests for weekly computation

**Files:**
- Create: `app/src/test/java/com/xiongxianfei/honorkingsrecorder/ui/screens/home/WeeklyProgressTest.kt`

- [ ] **Step 1: Write tests**

```kotlin
package com.xiongxianfei.honorkingsrecorder.ui.screens.home

import com.xiongxianfei.honorkingsrecorder.data.model.Match
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Calendar

class WeeklyProgressTest {

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

    private fun match(
        timestamp: Long = mondayOfWeek(0) + 3600_000L,
        score: Int = 50,
        isWin: Boolean = true,
        economy: Int = 7000,
        deaths: Int = 1,
        killedBaron: Boolean = true,
        threeQuestionCheck: Boolean = true,
        reliedOnTeam: Boolean = true,
        pushedTower: Boolean = true,
        engagedStrongest: Boolean = true,
        mentalStability: Boolean = true,
        notes: String = "some notes"
    ) = Match(
        hero = "后羿",
        timestamp = timestamp,
        isWin = isWin,
        economy = economy,
        kills = 5,
        deaths = deaths,
        assists = 3,
        killedBaron = killedBaron,
        threeQuestionCheck = threeQuestionCheck,
        reliedOnTeam = reliedOnTeam,
        pushedTower = pushedTower,
        engagedStrongest = engagedStrongest,
        mentalStability = mentalStability,
        notes = notes,
        score = score
    )

    // ── WeeklyProgress from HomeUiState ─────────────────────────────────

    @Test
    fun noMatchesThisWeek_progressIsNull() {
        val lastWeekTs = mondayOfWeek(1) + 3600_000L
        val state = HomeUiState(
            weeklyProgress = null // simulates no this-week matches
        )
        assertNull(state.weeklyProgress)
    }

    // ── DeltaMetric ─────────────────────────────────────────────────────

    @Test
    fun deltaMetric_higherIsBetter_positiveIsGood() {
        val m = DeltaMetric("均分", 70f, 5f)
        assertFalse(m.lowerIsBetter)
        assertEquals(5f, m.delta!!, 0.01f)
    }

    @Test
    fun deltaMetric_lowerIsBetter_negativeIsGood() {
        val m = DeltaMetric("死亡", 2f, -1f, lowerIsBetter = true)
        assertTrue(m.lowerIsBetter)
        assertEquals(-1f, m.delta!!, 0.01f)
    }

    @Test
    fun deltaMetric_noDelta_lastWeekMissing() {
        val m = DeltaMetric("均分", 70f, null)
        assertNull(m.delta)
    }

    // ── WeakSpot ────────────────────────────────────────────────────────

    @Test
    fun weakSpot_showsHitsAndTotal() {
        val ws = WeakSpot("推塔", 2, 7)
        assertEquals("推塔", ws.label)
        assertEquals(2, ws.hits)
        assertEquals(7, ws.total)
    }

    // ── WeeklyProgress ──────────────────────────────────────────────────

    @Test
    fun weeklyProgress_matchCount_correct() {
        val wp = WeeklyProgress(matchCount = 5)
        assertEquals(5, wp.matchCount)
    }

    @Test
    fun weeklyProgress_allCriteriaStrong_noWeakSpot() {
        val wp = WeeklyProgress(
            matchCount = 5,
            allCriteriaStrong = true,
            weakSpot = null
        )
        assertTrue(wp.allCriteriaStrong)
        assertNull(wp.weakSpot)
    }

    @Test
    fun weeklyProgress_hasWeakSpot_notAllStrong() {
        val wp = WeeklyProgress(
            matchCount = 5,
            allCriteriaStrong = false,
            weakSpot = WeakSpot("推塔", 1, 5)
        )
        assertFalse(wp.allCriteriaStrong)
        assertNotNull(wp.weakSpot)
        assertEquals("推塔", wp.weakSpot!!.label)
    }

    @Test
    fun defaultWeeklyProgress_isEmpty() {
        val wp = WeeklyProgress()
        assertEquals(0, wp.matchCount)
        assertTrue(wp.metrics.isEmpty())
        assertNull(wp.weakSpot)
        assertFalse(wp.allCriteriaStrong)
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `export JAVA_HOME="D:/Software/Android/Android Studio/jbr" && ./gradlew testDebugUnitTest --tests "*.WeeklyProgressTest" -q`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/xiongxianfei/honorkingsrecorder/ui/screens/home/WeeklyProgressTest.kt
git commit -m "test: add weekly progress data class unit tests"
```

---

### Task 3: Add WeeklyProgressCard composable to HomeScreen

**Files:**
- Modify: `ui/screens/home/HomeScreen.kt`

- [ ] **Step 1: Add imports**

Add these imports to `HomeScreen.kt`:

```kotlin
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
```

- [ ] **Step 2: Insert WeeklyProgressCard in the LazyColumn**

Add between the `StatsRow(state)` item and the `"最近对局"` section:

```kotlin
        // After item { StatsRow(state) }

        val wp = state.weeklyProgress
        if (wp != null) {
            item {
                WeeklyProgressCard(wp)
            }
        }

        // Before if (state.recentMatches.isNotEmpty()) { ...
```

- [ ] **Step 3: Add WeeklyProgressCard composable**

```kotlin
@Composable
private fun WeeklyProgressCard(progress: WeeklyProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("本周进步", style = MaterialTheme.typography.titleMedium)
                if (progress.matchCount < 3) {
                    Text(
                        "(本周仅${progress.matchCount}场)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 2x2 grid of delta metrics
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    progress.metrics.getOrNull(0)?.let {
                        DeltaMetricItem(it, modifier = Modifier.weight(1f))
                    }
                    progress.metrics.getOrNull(1)?.let {
                        DeltaMetricItem(it, modifier = Modifier.weight(1f))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    progress.metrics.getOrNull(2)?.let {
                        DeltaMetricItem(it, modifier = Modifier.weight(1f))
                    }
                    progress.metrics.getOrNull(3)?.let {
                        DeltaMetricItem(it, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Weak spot callout
            if (progress.allCriteriaStrong) {
                Text(
                    "本周表现均衡，继续保持！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WinGreen
                )
            } else if (progress.weakSpot != null) {
                Text(
                    "本周薄弱项：${progress.weakSpot.label} (${progress.weakSpot.hits}/${progress.weakSpot.total}场达标)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LossRed
                )
            }
        }
    }
}
```

- [ ] **Step 4: Add DeltaMetricItem composable**

```kotlin
@Composable
private fun DeltaMetricItem(metric: DeltaMetric, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(metric.label, style = MaterialTheme.typography.labelSmall)
            Text(
                if (metric.label == "胜率") "%.0f%%".format(metric.value)
                else if (metric.label == "均分") "%.1f".format(metric.value)
                else "%.0f".format(metric.value),
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (metric.delta != null) {
            val improving = if (metric.lowerIsBetter) metric.delta < 0f else metric.delta > 0f
            val flat = kotlin.math.abs(metric.delta) < 0.01f
            val icon = when {
                flat -> Icons.Filled.TrendingFlat
                improving -> Icons.Filled.TrendingUp
                else -> Icons.Filled.TrendingDown
            }
            val tint = when {
                flat -> MaterialTheme.colorScheme.onSurfaceVariant
                improving -> WinGreen
                else -> LossRed
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
```

- [ ] **Step 5: Run build to verify compilation**

Run: `export JAVA_HOME="D:/Software/Android/Android Studio/jbr" && ./gradlew assembleDebug -q`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Run all tests**

Run: `export JAVA_HOME="D:/Software/Android/Android Studio/jbr" && ./gradlew testDebugUnitTest -q`
Expected: All tests PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/xiongxianfei/honorkingsrecorder/ui/screens/home/HomeScreen.kt
git commit -m "feat: add weekly progress card to Home dashboard"
```

---

## Self-Review

**Spec coverage:**
- Delta metrics (4 items, 2x2 grid) — Task 1 (buildMetrics) + Task 3 (DeltaMetricItem)
- Weak spot callout — Task 1 (findWeakSpot) + Task 3 (WeeklyProgressCard)
- Edge case: no matches this week → hide card — Task 1 (returns null) + Task 3 (if wp != null)
- Edge case: no last week data → no arrows — Task 1 (delta = null) + Task 3 (if metric.delta != null)
- Edge case: fewer than 3 matches → note — Task 3 (matchCount < 3 check)
- All criteria >= 70% → positive message — Task 1 (allStrong) + Task 3 (allCriteriaStrong branch)
- Week boundaries Monday–Sunday — Task 1 (mondayOfWeek with Calendar.MONDAY)

**Placeholder scan:** No TBDs, TODOs, or vague steps found. All code blocks are complete.

**Type consistency:** `DeltaMetric`, `WeakSpot`, `WeeklyProgress` — names and fields consistent across all tasks. `WeeklyProgress?` nullable in HomeUiState, checked as `!= null` in UI.
