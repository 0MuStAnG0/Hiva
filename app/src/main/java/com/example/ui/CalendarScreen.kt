package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.Medication
import com.example.data.MedicationLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val medications by viewModel.activeMedications.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("تقویم دارویی") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.ui.graphics.Color.Transparent)
        ) {
            if (medications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("دارویی برای نمایش در تقویم وجود ندارد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(medications) { med ->
                        MedicationCalendarCard(medication = med, logs = allLogs.filter { it.medId == med.id })
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationCalendarCard(medication: Medication, logs: List<MedicationLog>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تقویم داروی ${medication.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            // Persian week starts on Saturday (Calendar.SATURDAY = 7)
            val startOffset = (firstDayOfWeek + 1) % 7 
            
            val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                weekDays.forEach { day ->
                    Text(text = day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val totalCells = startOffset + daysInMonth
            val rows = Math.ceil(totalCells / 7.0).toInt()
            
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - startOffset + 1
                        
                        if (day in 1..daysInMonth) {
                            val isToday = day == currentDay
                            val epochDay = Calendar.getInstance().apply { 
                                set(Calendar.YEAR, currentYear)
                                set(Calendar.MONTH, currentMonth)
                                set(Calendar.DAY_OF_MONTH, day)
                            }.timeInMillis / 86400000L
                            
                            val isConsumptionDay = if (medication.type == "INJECTION") {
                                val interval = medication.injectionIntervalDays ?: 0
                                if (interval > 0) {
                                    epochDay % interval == (medication.id % interval).toLong()
                                } else {
                                    true
                                }
                            } else {
                                val consumption = medication.cycleConsumptionDays ?: 0
                                val rest = medication.cycleRestDays ?: 0
                                val skipInterval = medication.skipIntervalDays ?: 0
                                if (consumption > 0 && rest > 0) {
                                    val totalCycle = consumption + rest
                                    val dayInCycle = (epochDay + medication.id) % totalCycle
                                    dayInCycle < consumption
                                } else if (skipInterval > 0) {
                                    epochDay % skipInterval != (medication.id % skipInterval).toLong()
                                } else {
                                    true
                                }
                            }
                            
                            val isTaken = logs.any { 
                                val logCal = Calendar.getInstance().apply { timeInMillis = it.takenAtMillis }
                                logCal.get(Calendar.DAY_OF_MONTH) == day && logCal.get(Calendar.MONTH) == currentMonth && logCal.get(Calendar.YEAR) == currentYear
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isTaken || isConsumptionDay -> MaterialTheme.colorScheme.primary
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 2.dp else if (!isConsumptionDay && !isTaken) 1.dp else 0.dp,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else if (!isConsumptionDay && !isTaken) MaterialTheme.colorScheme.outline else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = when {
                                        isTaken || isConsumptionDay -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Box(modifier = Modifier.size(36.dp)) // Empty cell
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (medication.type == "INJECTION") "روزهای تزریق" else "روزهای مصرف", style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape).background(androidx.compose.ui.graphics.Color.Transparent))
                Spacer(modifier = Modifier.width(4.dp))
                Text("روزهای عدم مصرف/استراحت", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
