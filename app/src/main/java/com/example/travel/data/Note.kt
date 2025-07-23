package com.example.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val journeyId: Long, // Foreign key to Journey
    val content: String,
    val timestamp: Long // Timestamp when the note was created
)
