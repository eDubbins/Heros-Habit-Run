package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Current app date (static UTC or device local date)
    val todayDateStr = dateFormat.format(Date())

    // UI state streams
    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSuffering: StateFlow<List<SufferingEntity>> = repository.activeSufferingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<ProfileEntity> = repository.profile
        .map { it ?: ProfileEntity(xp = 1450, streak = 5, lastActiveDate = todayDateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileEntity(xp = 1450, streak = 5, lastActiveDate = todayDateStr))

    val allRuns: StateFlow<List<DailyRunEntity>> = repository.allRuns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for showing the Morning Check-In Questionnaire
    private val _showCheckIn = MutableStateFlow(false)
    val showCheckIn: StateFlow<Boolean> = _showCheckIn.asStateFlow()

    private val _checkInDate = MutableStateFlow("")
    val checkInDate: StateFlow<String> = _checkInDate.asStateFlow()

    // Active state of today's run
    private val _todayRun = MutableStateFlow<DailyRunEntity?>(null)
    val todayRun: StateFlow<DailyRunEntity?> = _todayRun.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            // 1. Ensure profile exists. If not, generate default
            val existingProfile = repository.getProfile()
            if (existingProfile == null) {
                repository.insertProfile(ProfileEntity(xp = 1450, streak = 5, lastActiveDate = todayDateStr))
            }

            // 2. Ensure default habits exist
            val habitsList = repository.getAllHabitsList()
            if (habitsList.isEmpty()) {
                repository.insertHabit(HabitEntity("habit-1", "Drink Water", "Hydrate before coffee", isDefault = true))
                repository.insertHabit(HabitEntity("habit-2", "Stretch", "Do a 5-minute full body stretch", isDefault = true))
                repository.insertHabit(HabitEntity("habit-3", "Journal", "Write down 3 daily thoughts", isDefault = true))
            }

            // 3. Trigger check-in flow if needed, otherwise ensure today's run exists
            checkPreviousRunsAndInitializeToday()
        }
    }

    private suspend fun checkPreviousRunsAndInitializeToday() {
        val pastRuns = repository.getAllRunsList()
        // Find most recent run
        val latestRun = pastRuns.firstOrNull() // runs sorted by date DESC

        if (latestRun != null && latestRun.date != todayDateStr) {
            // There is a past run, but no run for today. Is its mood reported?
            if (latestRun.moodReported == null) {
                // Show check-in for this past date
                _checkInDate.value = latestRun.date
                _showCheckIn.value = true
            } else {
                // Already reported, create today's run and update profile
                ensureTodayRunExists()
            }
        } else {
            // First run ever, or today's run already exists
            ensureTodayRunExists()
        }
    }

    private suspend fun ensureTodayRunExists() {
        var run = repository.getRunByDate(todayDateStr)
        if (run == null) {
            run = DailyRunEntity(date = todayDateStr, completedHabitIdsCsv = "", moodReported = null)
            repository.insertRun(run)
        }
        _todayRun.value = run
    }

    /**
     * Submit yesterday's / last run's mood without closing check-in immediately
     */
    fun registerCheckInMood(mood: Int) {
        viewModelScope.launch {
            val checkInDateStr = _checkInDate.value
            if (checkInDateStr.isNotEmpty()) {
                val pastRun = repository.getRunByDate(checkInDateStr)
                if (pastRun != null) {
                    val updatedPastRun = pastRun.copy(moodReported = mood)
                    repository.insertRun(updatedPastRun)
                } else {
                    val newPastRun = DailyRunEntity(date = checkInDateStr, moodReported = mood)
                    repository.insertRun(newPastRun)
                }

                // Update Profile streak
                val prof = repository.getProfile() ?: ProfileEntity(xp = 1450, streak = 5, lastActiveDate = todayDateStr)
                val dayDiff = getDaysBetween(checkInDateStr, todayDateStr)
                val newStreak = if (dayDiff == 1) prof.streak + 1 else 1
                repository.insertProfile(prof.copy(streak = newStreak, lastActiveDate = todayDateStr, xp = prof.xp + 50)) // 50 XP award for finishing a run!
            }
        }
    }

    /**
     * Close the check-in panel and prepare today's active run
     */
    fun finishCheckIn() {
        viewModelScope.launch {
            _showCheckIn.value = false
            ensureTodayRunExists()
        }
    }

    /**
     * Replay morning check-in questionnaire
     */
    fun forceMorningCheckIn() {
        _checkInDate.value = todayDateStr
        _showCheckIn.value = true
    }

    /**
     * Toggle habit completion state for today
     */
    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            val run = _todayRun.value ?: repository.getRunByDate(todayDateStr) ?: return@launch
            val ids = run.completedHabitIdsCsv.split(",").filter { it.isNotEmpty() }.toMutableList()
            
            val isChecked: Boolean
            if (ids.contains(habitId)) {
                ids.remove(habitId)
                isChecked = false
            } else {
                ids.add(habitId)
                isChecked = true
            }

            val updatedRun = run.copy(completedHabitIdsCsv = ids.joinToString(","))
            repository.insertRun(updatedRun)
            _todayRun.value = updatedRun

            // Let's reward a small amount of XP for habit completion (e.g. 10 XP)
            val prof = repository.getProfile() ?: ProfileEntity()
            val xpReward = if (isChecked) 10 else -10
            repository.insertProfile(prof.copy(xp = (prof.xp + xpReward).coerceAtLeast(0)))
        }
    }

    /**
     * Add customized client-side habit
     */
    fun addHabit(name: String, description: String) {
        viewModelScope.launch {
            val id = "habit-${System.currentTimeMillis()}"
            repository.insertHabit(HabitEntity(id, name, description, isDefault = false))
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    /**
     * Add Worry to Suffering Inventory
     */
    fun addWorry(name: String, size: String) {
        viewModelScope.launch {
            val id = "worry-${System.currentTimeMillis()}"
            val item = SufferingEntity(
                id = id,
                name = name,
                size = size,
                createdDate = todayDateStr,
                isCompleted = false
            )
            repository.insertSufferingItem(item)
        }
    }

    /**
     * Swat Down / Conquer Worry and claim XP
     */
    fun conquerWorry(item: SufferingEntity, finalValue: Int) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                isCompleted = true,
                completedDate = todayDateStr,
                finalXpEarned = finalValue
            )
            repository.insertSufferingItem(updatedItem)

            // Update current today's run suffering raw text
            val run = _todayRun.value ?: repository.getRunByDate(todayDateStr)
            if (run != null) {
                val currentConquered = run.sufferingConqueredRaw.split(",")
                    .filter { it.isNotEmpty() }.toMutableList()
                currentConquered.add(item.name)
                repository.insertRun(run.copy(sufferingConqueredRaw = currentConquered.joinToString(",")))
                _todayRun.value = run.copy(sufferingConqueredRaw = currentConquered.joinToString(","))
            }

            // Update Profile stats
            val prof = repository.getProfile() ?: ProfileEntity()
            repository.insertProfile(prof.copy(xp = prof.xp + finalValue))
        }
    }

    /**
     * Help calculations for Days elapsed
     */
    fun getDaysBetween(start: String, end: String): Int {
        return try {
            val startDate = dateFormat.parse(start) ?: return 0
            val endDate = dateFormat.parse(end) ?: return 0
            val diff = endDate.time - startDate.time
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            if (days < 0) 0 else days.toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculates the interactive accrued Suffering Value (SV_i) based on:
     * Small: Base 10 + 1.5/day
     * Medium: Base 25 + 3.0/day
     * Large: Base 50 (constant)
     */
    fun calculateSufferingValue(item: SufferingEntity): Int {
        val days = getDaysBetween(item.createdDate, todayDateStr)
        return when (item.size.lowercase()) {
            "small" -> 10 + (days * 1.5).toInt()
            "medium" -> 25 + (days * 3.0).toInt()
            else -> 50
        }
    }

    /**
     * Habit weights stream based on correlation with next day's mood.
     * Computes Mt * H_(h, t-1) / N
     */
    fun getWeightedHabits(habitsList: List<HabitEntity>, runsList: List<DailyRunEntity>): List<Pair<HabitEntity, Double>> {
        // Sort runs chronologically (ascending, oldest first)
        val sortedRuns = runsList.sortedBy { it.date }
        if (sortedRuns.size < 2) {
            // Not enough subsequent run data, assign standard weights (0.0)
            return habitsList.map { it to 0.0 }
        }

        return habitsList.map { habit ->
            var correlationSum = 0.0
            var count = 0

            // Loop and compare run state at t-1 with mood at t
            for (i in 0 until sortedRuns.size - 1) {
                val prevRun = sortedRuns[i]
                val nextRun = sortedRuns[i + 1]

                val moodOfNext = nextRun.moodReported
                if (moodOfNext != null) {
                    val prevCompletedIdsSet = prevRun.completedHabitIdsCsv.split(",").toSet()
                    val completedInPrev = if (prevCompletedIdsSet.contains(habit.id)) 1.0 else 0.0
                    correlationSum += (moodOfNext.toDouble() * completedInPrev)
                    count++
                }
            }

            // Compute normalized weight (or return sum scaled for game metrics)
            val finalWeight = if (count > 0) {
                // Return scaled correlation weight to show nice gold values
                correlationSum * 5.0
            } else {
                0.0
            }

            habit to finalWeight
        }
    }

    /**
     * Save customized character options
     */
    fun updateCharacter(
        spriteType: String,
        primaryColorName: String,
        secondaryColorName: String,
        accessoryColorName: String
    ) {
        viewModelScope.launch {
            val prof = repository.getProfile() ?: ProfileEntity(xp = 1450, streak = 5, lastActiveDate = todayDateStr)
            val updated = prof.copy(
                spriteType = spriteType,
                primaryColorName = primaryColorName,
                secondaryColorName = secondaryColorName,
                accessoryColorName = accessoryColorName
            )
            repository.insertProfile(updated)
        }
    }

    /**
     * Update an entire run's parameters (habits, mood, notable events)
     */
    fun updateRun(updatedRun: DailyRunEntity) {
        viewModelScope.launch {
            repository.insertRun(updatedRun)
            if (updatedRun.date == todayDateStr) {
                _todayRun.value = updatedRun
            }
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
