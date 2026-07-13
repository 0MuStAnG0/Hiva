package com.example

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.AppDatabase
import com.example.data.MedicationRepository
import com.example.data.SettingsRepository

class MedApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: MedicationRepository
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN skipIntervalDays INTEGER")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN nextDoseAmount TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE medications ADD COLUMN nextDoseDateMillis INTEGER")
                db.execSQL("ALTER TABLE medications ADD COLUMN nextDoseDurationDays INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN type TEXT NOT NULL DEFAULT 'PILL'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN injectionCount INTEGER")
                db.execSQL("ALTER TABLE medications ADD COLUMN injectionIntervalDays INTEGER")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN doseFrequencyHours INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN cycleConsumptionDays INTEGER")
                db.execSQL("ALTER TABLE medications ADD COLUMN cycleRestDays INTEGER")
            }
        }

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "med_database"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
        .fallbackToDestructiveMigration()
        .build()

        repository = MedicationRepository(database.medicationDao())
        settingsRepository = SettingsRepository(this)
    }
}
