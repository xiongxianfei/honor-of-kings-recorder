# Weekly Progress Card (本周进步)

## Purpose

Show the player whether they're improving week-over-week directly on the Home dashboard. Displays 4 key metric deltas (this week vs last week) and identifies the weakest scoring criterion to focus on next.

## Location

Home screen, between the existing 3-stat row and the "最近对局" section.

## Card Content

### Delta Metrics (4 items in a 2x2 grid)

| Metric | Calculation | "Good" direction |
|---|---|---|
| Avg score | mean of `match.score` | higher is better (green up-arrow) |
| Win rate | wins / total | higher is better |
| Avg deaths | mean of `match.deaths` | lower is better (green down-arrow) |
| Avg economy | mean of `match.economy` | higher is better |

Each metric shows: **this week's value**, a colored arrow (green = improving, red = declining), and the delta amount.

Week boundaries: Monday 00:00 to Sunday 23:59 (local timezone).

### Weak Spot Callout

Evaluate all 9 scoring criteria across this week's matches:

| Criterion | Condition |
|---|---|
| Economy >= 6500 | `match.economy >= 6500` |
| Deaths <= 2 | `match.deaths <= 2` |
| Killed Baron | `match.killedBaron` |
| Three Question Check | `match.threeQuestionCheck` |
| Relied On Team | `match.reliedOnTeam` |
| Pushed Tower | `match.pushedTower` |
| Engaged Strongest | `match.engagedStrongest` |
| Mental Stability | `match.mentalStability` |
| Has Notes | `match.notes.isNotBlank()` |

For each criterion, compute the hit rate (matches where condition is true / total matches this week). The criterion with the lowest hit rate is the "weak spot."

Display: `"本周薄弱项：{label} ({hits}/{total}场达标)"`

If all criteria have >= 70% hit rate, display: `"本周表现均衡，继续保持！"`

## Edge Cases

- **No matches this week:** Hide the card entirely.
- **No matches last week:** Show this week's values without delta arrows.
- **Fewer than 3 matches this week:** Show the card with a note `"(本周仅N场)"` below the title.

## Data Source

All data comes from the existing `matches` table filtered by `timestamp`. No new tables, migrations, or dependencies.

## Computation

Done in `HomeViewModel` by extending the existing `allMatches: Flow<List<Match>>`. The flow is mapped to compute:
- `thisWeekMatches`: filter where `timestamp >= mondayOfThisWeek`
- `lastWeekMatches`: filter where `timestamp >= mondayOfLastWeek && timestamp < mondayOfThisWeek`
- Delta metrics from the two lists
- Criterion hit rates from `thisWeekMatches`

## UI Components

- **WeeklyProgressCard** composable — the outer card with `secondaryContainer` background
- **DeltaMetricItem** composable — reusable for each of the 4 metrics (value, arrow icon, delta text)
- Arrow icons: `Icons.Filled.TrendingUp` (green) / `Icons.Filled.TrendingDown` (red) / `Icons.Filled.TrendingFlat` (gray, when delta is 0)

## Files to Modify

| File | Change |
|---|---|
| `ui/screens/home/HomeViewModel.kt` | Add weekly computation to `HomeUiState` |
| `ui/screens/home/HomeScreen.kt` | Add `WeeklyProgressCard` composable between stats row and recent matches |

No new files needed — all logic fits within the existing Home screen module.
