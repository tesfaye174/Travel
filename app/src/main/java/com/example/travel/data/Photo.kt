package com.example.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val journeyId: Long, // Foreign key to Journey
    val imagePath: String, // Path to the image file
    val timestamp: Long // Timestamp when the photo was taken
)
