package com.example.travel.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travel.dao.JourneyDao
import com.example.travel.dao.NoteDao
import com.example.travel.dao.PhotoDao
import com.example.travel.data.Journey
import com.example.travel.data.Note
import com.example.travel.data.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class JourneyViewModel(
    private val journeyDao: JourneyDao,
    private val photoDao: PhotoDao,
    private val noteDao: NoteDao
) : ViewModel() {

    private val _currentJourney = MutableStateFlow<Journey?>(null)
    val currentJourney: StateFlow<Journey?> = _currentJourney

    private val _journeyCoordinates = MutableStateFlow<MutableList<Location>>(mutableListOf())
    val journeyCoordinates: StateFlow<List<Location>> = _journeyCoordinates

    fun startNewJourney(tripId: Long) {
        viewModelScope.launch {
            val newJourney = Journey(
                tripId = tripId,
                startTime = Date(),
                endTime = Date(), // Will be updated on stop
                duration = 0,
                distance = 0.0,
                routeCoordinates = "[]"
            )
            val journeyId = journeyDao.insertJourney(newJourney)
            _currentJourney.value = newJourney.copy(id = journeyId)
            _journeyCoordinates.value = mutableListOf()
        }
    }

    fun addCoordinate(location: Location) {
        _journeyCoordinates.value.add(location)
    }

    fun stopCurrentJourney() {
        viewModelScope.launch {
            _currentJourney.value?.let { journey ->
                var totalDistance = 0.0
                if (_journeyCoordinates.value.size > 1) {
                    for (i in 0 until _journeyCoordinates.value.size - 1) {
                        val loc1 = _journeyCoordinates.value[i]
                        val loc2 = _journeyCoordinates.value[i + 1]
                        totalDistance += loc1.distanceTo(loc2)
                    }
                }

                val updatedJourney = journey.copy(
                    endTime = Date(),
                    duration = Date().time - journey.startTime.time,
                    distance = totalDistance,
                    routeCoordinates = _journeyCoordinates.value.joinToString(",") { "${it.latitude},${it.longitude}" }
                )
                journeyDao.updateJourney(updatedJourney)
                _currentJourney.value = null
                _journeyCoordinates.value = mutableListOf()
            }
        }
    }

    fun addPhotoToJourney(journeyId: Long, imagePath: String) {
        viewModelScope.launch {
            val photo = Photo(journeyId = journeyId, imagePath = imagePath, timestamp = System.currentTimeMillis())
            photoDao.insertPhoto(photo)
        }
    }

    fun addNoteToJourney(journeyId: Long, content: String) {
        viewModelScope.launch {
            val note = Note(journeyId = journeyId, content = content, timestamp = System.currentTimeMillis())
            noteDao.insertNote(note)
        }
    }

    fun getJourneyById(journeyId: Long) = journeyDao.getJourneyById(journeyId)

    fun getJourneysForTrip(tripId: Long) = journeyDao.getJourneysForTrip(tripId)

    class JourneyViewModelFactory(
        private val journeyDao: JourneyDao,
        private val photoDao: PhotoDao,
        private val noteDao: NoteDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JourneyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return JourneyViewModel(journeyDao, photoDao, noteDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
