package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    val habitDao = db.habitDao()
    val sufferingDao = db.sufferingDao()
    val dailyRunDao = db.dailyRunDao()
    val profileDao = db.profileDao()

    // Habits
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    suspend fun getAllHabitsList(): List<HabitEntity> = habitDao.getAllHabitsList()
    suspend fun insertHabit(habit: HabitEntity) = habitDao.insertHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    // Suffering Inventory
    val allSufferingItems: Flow<List<SufferingEntity>> = sufferingDao.getAllItems()
    val activeSufferingItems: Flow<List<SufferingEntity>> = sufferingDao.getActiveItems()
    suspend fun getActiveSufferingItemsList(): List<SufferingEntity> = sufferingDao.getActiveItemsList()
    suspend fun insertSufferingItem(item: SufferingEntity) = sufferingDao.insertItem(item)
    suspend fun getSufferingItemById(id: String): SufferingEntity? = sufferingDao.getItemById(id)
    suspend fun deleteSufferingItemById(id: String) = sufferingDao.deleteItemById(id)

    // Daily Runs
    val allRuns: Flow<List<DailyRunEntity>> = dailyRunDao.getAllRuns()
    suspend fun getAllRunsList(): List<DailyRunEntity> = dailyRunDao.getAllRunsList()
    suspend fun getRunByDate(date: String): DailyRunEntity? = dailyRunDao.getRunByDate(date)
    suspend fun insertRun(run: DailyRunEntity) = dailyRunDao.insertRun(run)

    // Profile
    val profile: Flow<ProfileEntity?> = profileDao.getProfileFlow()
    suspend fun getProfile(): ProfileEntity? = profileDao.getProfile()
    suspend fun insertProfile(profile: ProfileEntity) = profileDao.insertProfile(profile)
}
