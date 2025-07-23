package com.example.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "journeys")
data class Journey(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long, // Foreign key to Trip
    val startTime: Date,
    val endTime: Date,
    val duration: Long, // in milliseconds
    val distance: Double, // in meters
    val routeCoordinates: String // Store as a JSON string or similar for simplicity
)
