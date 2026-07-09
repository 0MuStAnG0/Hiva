package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.MedicationRepository
import com.example.data.SettingsRepository

class MedApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: MedicationRepository
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "med_database"
        )
        .fallbackToDestructiveMigration()
        .build()
        repository = MedicationRepository(database.medicationDao())
        settingsRepository = SettingsRepository(this)
    }
}
