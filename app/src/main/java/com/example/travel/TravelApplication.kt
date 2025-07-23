package com.example.travel

import android.app.Application
import com.example.travel.data.AppDatabase

class TravelApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    val tripDao by lazy { database.tripDao() }
    val journeyDao by lazy { database.journeyDao() }
    val photoDao by lazy { database.photoDao() }
    val noteDao by lazy { database.noteDao() }
}
