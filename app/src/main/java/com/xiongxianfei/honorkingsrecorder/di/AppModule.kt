package com.xiongxianfei.honorkingsrecorder.di

import android.content.Context
import androidx.room.Room
import com.xiongxianfei.honorkingsrecorder.data.db.AppDatabase
import com.xiongxianfei.honorkingsrecorder.data.db.MatchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "honor_kings_recorder.db"
        ).build()

    @Provides
    @Singleton
    fun provideMatchDao(db: AppDatabase): MatchDao = db.matchDao()
}
