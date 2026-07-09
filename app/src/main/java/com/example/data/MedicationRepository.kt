package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val dao: MedicationDao) {
    val activeMedications: Flow<List<Medication>> = dao.getActiveMedications()
    val allLogs: Flow<List<MedicationLog>> = dao.getAllLogs()

    suspend fun insertMedication(medication: Medication) {
        dao.insertMedication(medication)
    }

    suspend fun getMedicationById(id: Int): Medication? {
        return dao.getMedicationById(id)
    }

    suspend fun deleteMedication(id: Int) {
        dao.deleteMedication(id)
    }

    suspend fun markAsTaken(medId: Int) {
        dao.insertLog(MedicationLog(medId = medId, takenAtMillis = System.currentTimeMillis()))
    }
}
