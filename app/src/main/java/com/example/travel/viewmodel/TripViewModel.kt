package com.example.travel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travel.dao.TripDao
import com.example.travel.data.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripViewModel(private val tripDao: TripDao, private val journeyDao: JourneyDao) : ViewModel() {

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    val tripsPerMonth: StateFlow<Map<String, Int>> = _trips.map {
        val monthlyCounts = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        it.forEach { trip ->
            val calendar = Calendar.getInstance().apply { time = trip.startDate }
            val monthYear = dateFormat.format(calendar.time)
            monthlyCounts[monthYear] = (monthlyCounts[monthYear] ?: 0) + 1
        }
        monthlyCounts
    }.asStateFlow()

    val totalDistance: StateFlow<Double> = _trips.map {
        var total = 0.0
        it.forEach { trip ->
            journeyDao.getJourneysForTrip(trip.id).forEach { journey ->
                total += journey.distance
            }
        }
        total
    }.asStateFlow()

    val tripsByType: StateFlow<Map<String, Int>> = _trips.map {
        val typeCounts = mutableMapOf<String, Int>()
        it.forEach { trip ->
            typeCounts[trip.type] = (typeCounts[trip.type] ?: 0) + 1
        }
        typeCounts
    }.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            _trips.value = tripDao.getAllTrips()
        }
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.insertTrip(trip)
            loadTrips() // Refresh the list after adding
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.updateTrip(trip)
            loadTrips() // Refresh the list after updating
        }
    }

    // Factory for creating TripViewModel with dependencies
    class TripViewModelFactory(private val tripDao: TripDao, private val journeyDao: JourneyDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TripViewModel(tripDao, journeyDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
