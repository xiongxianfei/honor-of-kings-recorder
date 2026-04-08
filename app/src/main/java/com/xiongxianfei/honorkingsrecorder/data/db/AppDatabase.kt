package com.xiongxianfei.honorkingsrecorder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xiongxianfei.honorkingsrecorder.data.model.Match

@Database(
    entities = [Match::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}
