package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    initialMedication: Medication? = null,
    onSave: (Medication) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(initialMedication?.name ?: "") }
    var dose by remember { mutableStateOf(initialMedication?.currentDose ?: "") }
    
    val initialTimes = initialMedication?.timeOfDay?.split(",")?.filter { it.isNotBlank() }?.toMutableList()
    val timesList = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(Unit) {
        if (initialTimes != null && initialTimes.isNotEmpty()) {
            timesList.addAll(initialTimes)
        } else {
            timesList.add("08:00")
        }
    }
    
    var cycleConsumptionDays by remember { mutableStateOf(initialMedication?.cycleConsumptionDays?.toString() ?: "") }
    var cycleRestDays by remember { mutableStateOf(initialMedication?.cycleRestDays?.toString() ?: "") }
    var skipIntervalDays by remember { mutableStateOf(initialMedication?.skipIntervalDays?.toString() ?: "") } // Keep for backward compatibility or remove later
    var injectionCount by remember { mutableStateOf(initialMedication?.injectionCount?.toString() ?: "") }
    var injectionIntervalDays by remember { mutableStateOf(initialMedication?.injectionIntervalDays?.toString() ?: "") }
    var doseFrequencyHours by remember { mutableStateOf<Int?>(initialMedication?.doseFrequencyHours) }
    
    var hasDoseChange by remember { mutableStateOf(initialMedication?.nextDoseDateMillis != null) }
    var nextDose by remember { mutableStateOf(initialMedication?.nextDoseAmount ?: "") }
    var daysUntilChange by remember { mutableStateOf(
        if (initialMedication?.nextDoseDateMillis != null) {
            ((initialMedication.nextDoseDateMillis - System.currentTimeMillis()) / 86400000L).coerceAtLeast(0).toString()
        } else ""
    ) }

    var type by remember { mutableStateOf(initialMedication?.type ?: "PILL") }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (initialMedication == null) "افزودن یادآور جدید" else "ویرایش یادآور") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabRow(
                selectedTabIndex = if (type == "PILL") 0 else 1,
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Tab(
                    selected = type == "PILL",
                    onClick = { type = "PILL" },
                    text = { Text("یادآور دارو") }
                )
                Tab(
                    selected = type == "INJECTION",
                    onClick = { type = "INJECTION" },
                    text = { Text("یادآور تزریق") }
                )
            }
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (type == "PILL") "نام دارو" else "نام آمپول/داروی تزریقی") },
                modifier = Modifier.fillMaxWidth().testTag("med_name_input"),
                singleLine = true
            )
            
            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                label = { Text(if (type == "PILL") "مقدار مصرف (مثلاً ۱ قرص)" else "مقدار مصرف (مثلاً ۱ آمپول یا سی‌سی)") },
                modifier = Modifier.fillMaxWidth().testTag("med_dose_input"),
                singleLine = true
            )

            val context = LocalContext.current
            
            Text("فاصله زمانی مصرف در روز (اختیاری):", style = MaterialTheme.typography.titleMedium)
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = doseFrequencyHours == null,
                        onClick = { doseFrequencyHours = null; timesList.clear(); timesList.add("08:00") },
                        label = { Text("یکبار در روز") }
                    )
                    FilterChip(
                        selected = doseFrequencyHours == 6,
                        onClick = { 
                            doseFrequencyHours = 6
                            timesList.clear()
                            timesList.addAll(listOf("06:00", "12:00", "18:00", "00:00"))
                        },
                        label = { Text("هر ۶ ساعت") }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = doseFrequencyHours == 8,
                        onClick = { 
                            doseFrequencyHours = 8
                            timesList.clear()
                            timesList.addAll(listOf("08:00", "16:00", "00:00"))
                        },
                        label = { Text("هر ۸ ساعت") }
                    )
                    FilterChip(
                        selected = doseFrequencyHours == 12,
                        onClick = { 
                            doseFrequencyHours = 12
                            timesList.clear()
                            timesList.addAll(listOf("08:00", "20:00"))
                        },
                        label = { Text("هر ۱۲ ساعت") }
                    )
                }
            }

            Text("زمان‌های مصرف:", style = MaterialTheme.typography.titleMedium)
            
            timesList.forEachIndexed { index, timeVal ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = timeVal,
                        onValueChange = { },
                        label = { Text("زمان ${index + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val parts = timeVal.split(":")
                                val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                
                                android.app.TimePickerDialog(
                                    context,
                                    { _, h, m -> timesList[index] = String.format("%02d:%02d", h, m) },
                                    hour,
                                    minute,
                                    true
                                ).show()
                            },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    if (timesList.size > 1) {
                        IconButton(onClick = { timesList.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف زمان")
                        }
                    }
                }
            }
            
            TextButton(
                onClick = { timesList.add("08:00") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("+ افزودن زمان جدید")
            }
            
            if (type == "PILL") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cycleConsumptionDays,
                        onValueChange = { cycleConsumptionDays = it },
                        label = { Text("سیکل روزانه مصرف (مثلاً ۲۱ روز)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("med_cycle_consumption_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cycleRestDays,
                        onValueChange = { cycleRestDays = it },
                        label = { Text("فاصله مجدد مصرف (مثلاً ۷ روز)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("med_cycle_rest_input"),
                        singleLine = true
                    )
                }
            } else {
                OutlinedTextField(
                    value = injectionCount,
                    onValueChange = { injectionCount = it },
                    label = { Text("تعداد آمپول") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("med_injection_count_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = injectionIntervalDays,
                    onValueChange = { injectionIntervalDays = it },
                    label = { Text("فاصله تزریق بعدی بر اساس روز (مثلاً هر ۳ روز)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("med_injection_interval_input"),
                    singleLine = true
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "یادآور تغییر دوز مصرفی در آینده (اختیاری)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasDoseChange,
                            onCheckedChange = { hasDoseChange = it },
                            modifier = Modifier.testTag("dose_change_switch")
                        )
                    }

                    if (hasDoseChange) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = nextDose,
                            onValueChange = { nextDose = it },
                            label = { Text("مقدار مصرف جدید (پس از تغییر دوز)") },
                            modifier = Modifier.fillMaxWidth().testTag("next_dose_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = daysUntilChange,
                            onValueChange = { daysUntilChange = it },
                            label = { Text("چند روز دیگر دوز تغییر می‌کند؟ (مثلاً ۳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("days_until_change_input"),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val days = daysUntilChange.toLongOrNull()
                    val targetDate = if (hasDoseChange && days != null) {
                        System.currentTimeMillis() + (days * 86400000L)
                    } else null

                    val med = Medication(
                        id = initialMedication?.id ?: 0,
                        name = name,
                        currentDose = dose,
                        timeOfDay = timesList.joinToString(","),
                        daysOfWeek = "0,1,2,3,4,5,6", // Default all days for simplicity
                        skipIntervalDays = skipIntervalDays.toIntOrNull(),
                        cycleConsumptionDays = cycleConsumptionDays.toIntOrNull(),
                        cycleRestDays = cycleRestDays.toIntOrNull(),
                        nextDoseAmount = nextDose,
                        nextDoseDateMillis = targetDate,
                        nextDoseDurationDays = null,
                        type = type,
                        injectionCount = injectionCount.toIntOrNull(),
                        injectionIntervalDays = injectionIntervalDays.toIntOrNull(),
                        doseFrequencyHours = doseFrequencyHours
                    )
                    onSave(med)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_med_button"),
                enabled = name.isNotBlank() && dose.isNotBlank()
            ) {
                Text(if (initialMedication == null) "ثبت یادآور" else "ذخیره تغییرات", fontSize = 18.sp)
            }
        }
    }
}
