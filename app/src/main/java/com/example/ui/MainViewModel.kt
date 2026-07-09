package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Medication
import com.example.data.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application, private val repository: MedicationRepository) : AndroidViewModel(application) {

    private val alarmScheduler = com.example.ui.AlarmScheduler(application)

    val activeMedications: StateFlow<List<Medication>> = repository.activeMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.activeMedications.collect { medications ->
                medications.forEach { alarmScheduler.schedule(it) }
            }
        }
    }

    val allLogs: StateFlow<List<com.example.data.MedicationLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val takenTodayCount: StateFlow<Int> = repository.allLogs.map { logs ->
        val todayStart = getTodayStartMillis()
        logs.count { it.takenAtMillis >= todayStart }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addMedication(med: Medication) {
        viewModelScope.launch {
            repository.insertMedication(med)
            alarmScheduler.schedule(med)
        }
    }

    fun markAsTaken(medId: Int) {
        viewModelScope.launch {
            repository.markAsTaken(medId)
        }
    }
    
    fun deleteMedication(medId: Int) {
        viewModelScope.launch {
            repository.deleteMedication(medId)
            alarmScheduler.cancel(medId)
        }
    }

    private fun getTodayStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

class MainViewModelFactory(private val application: Application, private val repository: MedicationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
