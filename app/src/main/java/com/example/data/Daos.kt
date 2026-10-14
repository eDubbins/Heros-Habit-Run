package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsList(): List<HabitEntity>
}

@Dao
interface SufferingDao {
    @Query("SELECT * FROM suffering_inventory ORDER BY createdDate DESC")
    fun getAllItems(): Flow<List<SufferingEntity>>

    @Query("SELECT * FROM suffering_inventory WHERE isCompleted = 0 ORDER BY createdDate DESC")
    fun getActiveItems(): Flow<List<SufferingEntity>>

    @Query("SELECT * FROM suffering_inventory WHERE isCompleted = 0")
    suspend fun getActiveItemsList(): List<SufferingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SufferingEntity)

    @Query("SELECT * FROM suffering_inventory WHERE id = :id")
    suspend fun getItemById(id: String): SufferingEntity?

    @Query("DELETE FROM suffering_inventory WHERE id = :id")
    suspend fun deleteItemById(id: String)
}

@Dao
interface DailyRunDao {
    @Query("SELECT * FROM daily_runs ORDER BY date DESC")
    fun getAllRuns(): Flow<List<DailyRunEntity>>

    @Query("SELECT * FROM daily_runs ORDER BY date DESC")
    suspend fun getAllRunsList(): List<DailyRunEntity>

    @Query("SELECT * FROM daily_runs WHERE date = :date")
    suspend fun getRunByDate(date: String): DailyRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: DailyRunEntity)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
}
