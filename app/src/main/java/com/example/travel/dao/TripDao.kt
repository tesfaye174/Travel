package com.example.travel.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.travel.data.Trip

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("SELECT * FROM trips")
    suspend fun getAllTrips(): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?
}
