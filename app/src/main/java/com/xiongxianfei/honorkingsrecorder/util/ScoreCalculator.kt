package com.xiongxianfei.honorkingsrecorder.util

object ScoreCalculator {

    fun calculate(
        economy: Int,
        deaths: Int,
        killedBaron: Boolean,
        threeQuestionCheck: Boolean,
        reliedOnTeam: Boolean,
        pushedTower: Boolean,
        engagedStrongest: Boolean,
        mentalStability: Boolean,
        notes: String
    ): Int {
        var score = 0
        if (economy >= 6500) score += 15
        if (deaths <= 2) score += 10
        if (killedBaron) score += 10
        if (threeQuestionCheck) score += 10
        if (reliedOnTeam) score += 10
        if (pushedTower) score += 10
        if (engagedStrongest) score += 10
        if (mentalStability) score += 15
        if (notes.isNotBlank()) score += 10
        return score
    }
}
