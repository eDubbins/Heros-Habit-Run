package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "suffering_inventory")
data class SufferingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val size: String, // "small", "medium", "large"
    val createdDate: String, // "YYYY-MM-DD"
    val isCompleted: Boolean = false,
    val completedDate: String? = null,
    val finalXpEarned: Int = 0 // XP earned upon swipe-out
)

@Entity(tableName = "daily_runs")
data class DailyRunEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val completedHabitIdsCsv: String = "", // comma-separated habit IDs
    val moodReported: Int? = null, // -1 = Hard, 0 = Meh, 1 = Great
    val sufferingConqueredRaw: String = "", // comma-separated named items defeated today
    val notableEvents: String = "" // general notable events contributing to mood
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val spriteType: String = "hero",
    val primaryColorName: String = "green",
    val secondaryColorName: String = "dark_green",
    val accessoryColorName: String = "gold"
)
