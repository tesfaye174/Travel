package com.example.travel.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.travel.data.Photo

@Dao
interface PhotoDao {
    @Insert
    suspend fun insertPhoto(photo: Photo)

    @Query("SELECT * FROM photos WHERE journeyId = :journeyId")
    suspend fun getPhotosForJourney(journeyId: Long): List<Photo>
}
