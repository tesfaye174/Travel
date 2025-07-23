package com.example.travel.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.travel.data.Journey

@Dao
interface JourneyDao {
    @Insert
    suspend fun insertJourney(journey: Journey): Long

    @Update
    suspend fun updateJourney(journey: Journey)

    @Query("SELECT * FROM journeys WHERE tripId = :tripId")
    suspend fun getJourneysForTrip(tripId: Long): List<Journey>

    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    suspend fun getJourneyById(journeyId: Long): Journey?
}
