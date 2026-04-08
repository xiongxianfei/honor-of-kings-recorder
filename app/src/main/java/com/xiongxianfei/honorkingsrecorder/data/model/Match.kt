package com.xiongxianfei.honorkingsrecorder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hero: String,
    val timestamp: Long,
    val isWin: Boolean,
    val economy: Int,
    val kills: Int = 0,
    val deaths: Int,
    val assists: Int = 0,
    val killedBaron: Boolean,
    val threeQuestionCheck: Boolean,
    val reliedOnTeam: Boolean,
    val pushedTower: Boolean,
    val engagedStrongest: Boolean,
    val mentalStability: Boolean,
    val notes: String = "",
    val score: Int
)
