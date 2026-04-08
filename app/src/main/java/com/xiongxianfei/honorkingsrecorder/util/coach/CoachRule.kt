package com.xiongxianfei.honorkingsrecorder.util.coach

/**
 * Data extracted from a single video frame via OCR.
 * [timestampMs] is the frame's position in the video in milliseconds (e.g. 270_000 = 4:30).
 */
data class FrameData(
    val timestampMs: Long,
    val economy: Int?,
    val deaths: Int?,
    val kills: Int?,
    val assists: Int?
)

/**
 * A single piece of coaching feedback tied to a specific video timestamp.
 * [isPositive] = true → green "good job" tip; false → orange/red warning.
 */
data class CoachTip(
    val timestampMs: Long,
    val message: String,
    val isPositive: Boolean
)

/**
 * A single coaching rule. Implement this interface and add the object to
 * [CoachRules.all] — the engine picks it up automatically.
 *
 * Return null when the rule does not apply to the given frame
 * (e.g. wrong timestamp, or required field is missing from OCR).
 */
interface CoachRule {
    fun evaluate(frame: FrameData): CoachTip?
}
