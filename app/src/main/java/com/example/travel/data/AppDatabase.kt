package com.example.travel.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.travel.dao.JourneyDao
import com.example.travel.dao.NoteDao
import com.example.travel.dao.PhotoDao
import com.example.travel.dao.TripDao

@Database(entities = [Trip::class, Journey::class, Photo::class, Note::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun journeyDao(): JourneyDao
    abstract fun photoDao(): PhotoDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
