package com.xiongxianfei.honorkingsrecorder.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatchDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MatchDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.matchDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun match(
        id: Long = 0,
        hero: String = "后羿",
        timestamp: Long = System.currentTimeMillis(),
        isWin: Boolean = true,
        score: Int = 50
    ) = Match(
        id = id, hero = hero, timestamp = timestamp, isWin = isWin,
        economy = 5000, kills = 0, deaths = 2, assists = 0, killedBaron = false,
        threeQuestionCheck = false, reliedOnTeam = false, pushedTower = false,
        engagedStrongest = false, mentalStability = false, notes = "", score = score
    )

    @Test
    fun getAll_emptyDatabase_returnsEmptyList() = runTest {
        val matches = dao.getAll().first()
        assertTrue(matches.isEmpty())
    }

    @Test
    fun insert_singleMatch_canBeRetrieved() = runTest {
        dao.insert(match(hero = "艾琳", score = 75))
        val matches = dao.getAll().first()
        assertEquals(1, matches.size)
        assertEquals("艾琳", matches[0].hero)
        assertEquals(75, matches[0].score)
    }

    @Test
    fun insert_multipleMatches_allRetrieved() = runTest {
        dao.insert(match(hero = "后羿"))
        dao.insert(match(hero = "莱西奥"))
        dao.insert(match(hero = "戈娅"))
        val matches = dao.getAll().first()
        assertEquals(3, matches.size)
    }

    @Test
    fun getAll_orderedByTimestampDescending() = runTest {
        val now = System.currentTimeMillis()
        dao.insert(match(hero = "后羿", timestamp = now - 2000))
        dao.insert(match(hero = "艾琳", timestamp = now - 1000))
        dao.insert(match(hero = "戈娅", timestamp = now))

        val matches = dao.getAll().first()
        assertEquals(3, matches.size)
        assertEquals("戈娅", matches[0].hero)   // most recent first
        assertEquals("艾琳", matches[1].hero)
        assertEquals("后羿", matches[2].hero)
    }

    @Test
    fun delete_removesCorrectMatch() = runTest {
        dao.insert(match(hero = "后羿"))
        dao.insert(match(hero = "莱西奥"))

        val allMatches = dao.getAll().first()
        assertEquals(2, allMatches.size)

        val toDelete = allMatches.first { it.hero == "后羿" }
        dao.delete(toDelete)

        val remaining = dao.getAll().first()
        assertEquals(1, remaining.size)
        assertEquals("莱西奥", remaining[0].hero)
    }

    @Test
    fun delete_onlyMatchInDb_leavesEmptyList() = runTest {
        dao.insert(match(hero = "孙尚香"))
        val inserted = dao.getAll().first()
        dao.delete(inserted[0])

        val remaining = dao.getAll().first()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun insert_replaceOnConflict_updatesExistingRow() = runTest {
        dao.insert(match(id = 1L, hero = "公孙离", score = 40))
        dao.insert(match(id = 1L, hero = "公孙离", score = 80))

        val matches = dao.getAll().first()
        assertEquals(1, matches.size)
        assertEquals(80, matches[0].score)
    }

    @Test
    fun insert_winFlag_persistedCorrectly() = runTest {
        dao.insert(match(hero = "后羿", isWin = false))
        val matches = dao.getAll().first()
        assertFalse(matches[0].isWin)
    }

    @Test
    fun insert_allFields_persistedCorrectly() = runTest {
        val original = Match(
            id = 0, hero = "莱西奥", timestamp = 1_700_000_000_000L,
            isWin = true, economy = 8500, kills = 11, deaths = 1, assists = 5,
            killedBaron = true, threeQuestionCheck = true,
            reliedOnTeam = false, pushedTower = true,
            engagedStrongest = true, mentalStability = true,
            notes = "very clean game", score = 95
        )
        dao.insert(original)
        val retrieved = dao.getAll().first()[0]

        assertEquals("莱西奥", retrieved.hero)
        assertEquals(1_700_000_000_000L, retrieved.timestamp)
        assertTrue(retrieved.isWin)
        assertEquals(8500, retrieved.economy)
        assertEquals(11, retrieved.kills)
        assertEquals(1,  retrieved.deaths)
        assertEquals(5,  retrieved.assists)
        assertTrue(retrieved.killedBaron)
        assertTrue(retrieved.threeQuestionCheck)
        assertFalse(retrieved.reliedOnTeam)
        assertTrue(retrieved.pushedTower)
        assertTrue(retrieved.engagedStrongest)
        assertTrue(retrieved.mentalStability)
        assertEquals("very clean game", retrieved.notes)
        assertEquals(95, retrieved.score)
    }
}
