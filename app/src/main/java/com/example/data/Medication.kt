package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currentDose: String, // e.g. "1 Pill" or "500 mg"
    val timeOfDay: String, // e.g. "08:00"
    val daysOfWeek: String, // comma separated days: "0,1,2,3,4,5,6"
    val skipIntervalDays: Int? = null, // e.g. every 10 days
    val nextDoseAmount: String = "", // for dose change
    val nextDoseDateMillis: Long? = null, // when the dose change happens
    val nextDoseDurationDays: Int? = null, // how many days the new dose will last
    val type: String = "PILL", // "PILL" or "INJECTION"
    val injectionCount: Int? = null,
    val injectionIntervalDays: Int? = null,
    val doseFrequencyHours: Int? = null, // e.g. 6, 8, 12. If null, then once a day
    val cycleConsumptionDays: Int? = null,
    val cycleRestDays: Int? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medId: Int,
    val takenAtMillis: Long
)
