package com.xiongxianfei.honorkingsrecorder.data.repository

import com.xiongxianfei.honorkingsrecorder.data.db.MatchDao
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val dao: MatchDao
) {
    val allMatches: Flow<List<Match>> = dao.getAll()

    suspend fun getById(id: Long): Match? = dao.getById(id)

    suspend fun insert(match: Match) = dao.insert(match)

    suspend fun delete(match: Match) = dao.delete(match)
}
