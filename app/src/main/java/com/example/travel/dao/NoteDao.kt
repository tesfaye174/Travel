package com.example.travel.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.travel.data.Note

@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(note: Note)

    @Query("SELECT * FROM notes WHERE journeyId = :journeyId")
    suspend fun getNotesForJourney(journeyId: Long): List<Note>
}
