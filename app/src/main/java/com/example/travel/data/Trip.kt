package com.example.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destination: String,
    val startDate: Date,
    val endDate: Date,
    val type: String // e.g., "Local", "Day", "Multi-day"
)
