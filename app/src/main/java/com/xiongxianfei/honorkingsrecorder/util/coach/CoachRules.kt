package com.xiongxianfei.honorkingsrecorder.util.coach

/**
 * All coaching rules in one place.
 *
 * Checkpoints (ms):
 *   4:30  → 270_000
 *   7:30  → 450_000
 *   10:30 → 630_000
 *
 * Thresholds come from wzry-marksman-playbook equipment timing checkpoints:
 *   4:30  — 1st item + shoes  ≈ 3 200 gold
 *   7:30  — 2nd item          ≈ 6 500 gold
 *   10:30 — full 3-item set   ≈ 9 000 gold
 *
 * To add a new rule: add an object : CoachRule below and append it to [all].
 */
object CoachRules {

    // ── 4:30 economy ────────────────────────────────────────────────────────

    object EconomyAt430 : CoachRule {
        private const val TS = 270_000L
        private const val TARGET = 3200

        override fun evaluate(frame: FrameData): CoachTip? {
            if (frame.timestampMs != TS) return null
            val eco = frame.economy ?: return null
            return if (eco < TARGET)
                CoachTip(TS, "经济${eco}偏低，第一件装备未完成（目标≥${TARGET}）\n💡 主动迎线，清完即走（法则2）", false)
            else
                CoachTip(TS, "✓ 经济${eco}，4:30装备节奏达标", true)
        }
    }

    // ── 4:30 deaths ─────────────────────────────────────────────────────────

    object DeathsAt430 : CoachRule {
        private const val TS = 270_000L

        override fun evaluate(frame: FrameData): CoachTip? {
            if (frame.timestampMs != TS) return null
            val deaths = frame.deaths ?: return null
            return if (deaths >= 1)
                CoachTip(TS, "4:30已死亡${deaths}次\n💡 血量即权力，HP<60%主动拉开距离（法则4）", false)
            else
                CoachTip(TS, "✓ 4:30未死亡，早期存活良好", true)
        }
    }

    // ── 7:30 economy ────────────────────────────────────────────────────────

    object EconomyAt730 : CoachRule {
        private const val TS = 450_000L
        private const val TARGET = 6500

        override fun evaluate(frame: FrameData): CoachTip? {
            if (frame.timestampMs != TS) return null
            val eco = frame.economy ?: return null
            return if (eco < TARGET)
                CoachTip(TS, "经济${eco}，第二件装备落后（目标≥${TARGET}）\n💡 补刀+打野交替维持经济节奏（法则2经济节奏）", false)
            else
                CoachTip(TS, "✓ 经济${eco}，7:30经济达标", true)
        }
    }

    // ── 7:30 deaths ─────────────────────────────────────────────────────────

    object DeathsAt730 : CoachRule {
        private const val TS = 450_000L

        override fun evaluate(frame: FrameData): CoachTip? {
            if (frame.timestampMs != TS) return null
            val deaths = frame.deaths ?: return null
            return if (deaths >= 2)
                CoachTip(TS, "7:30死亡${deaths}次\n💡 跟随打野，避免孤立，射手孤立=最高风险状态（法则7）", false)
            else
                CoachTip(TS, "✓ 7:30死亡${deaths}次，存活良好", true)
        }
    }

    // ── 10:30 economy ───────────────────────────────────────────────────────

    object EconomyAt1030 : CoachRule {
        private const val TS = 630_000L
        private const val TARGET = 9000

        override fun evaluate(frame: FrameData): CoachTip? {
            if (frame.timestampMs != TS) return null
            val eco = frame.economy ?: return null
            return if (eco < TARGET)
                CoachTip(TS, "经济${eco}，三件套未完成（目标≥${TARGET}）\n💡 10分钟集合推塔，将经济优势转化为地图优势（法则8）", false)
            else
                CoachTip(TS, "✓ 经济${eco}，三件套完成，团战能力达标", true)
        }
    }

    // ── 10:30 deaths ────────────────────────────────────────────────────────

    object DeathsAt1030 : CoachRule {
        private const val TS = 630_000L

        override fun evaluate(frame: FrameData): CoachTip? {
            if (frame.timestampMs != TS) return null
            val deaths = frame.deaths ?: return null
            return if (deaths >= 3)
                CoachTip(TS, "10:30死亡${deaths}次，严重影响经济发展\n💡 有优势集合推塔；劣势时巩固一路资源作支点（法则12）", false)
            else
                CoachTip(TS, "✓ 10:30死亡${deaths}次，生存状况良好", true)
        }
    }

    // ── Registry — append new rules here ────────────────────────────────────

    val all: List<CoachRule> = listOf(
        EconomyAt430,
        DeathsAt430,
        EconomyAt730,
        DeathsAt730,
        EconomyAt1030,
        DeathsAt1030,
    )
}
