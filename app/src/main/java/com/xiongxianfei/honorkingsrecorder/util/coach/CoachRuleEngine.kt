package com.xiongxianfei.honorkingsrecorder.util.coach

/**
 * Evaluates all registered [CoachRule]s against a list of video frames and
 * returns the resulting tips sorted by timestamp.
 *
 * To add a new rule: implement [CoachRule] in [CoachRules] and append it to
 * [CoachRules.all]. No changes needed here.
 */
object CoachRuleEngine {

    val rules: List<CoachRule> = CoachRules.all

    fun analyze(frames: List<FrameData>): List<CoachTip> =
        frames
            .flatMap { frame -> rules.mapNotNull { rule -> rule.evaluate(frame) } }
            .sortedBy { it.timestampMs }
}
