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
    
    var skipIntervalDays by remember { mutableStateOf(initialMedication?.skipIntervalDays?.toString() ?: "") }
    
    var hasDoseChange by remember { mutableStateOf(initialMedication?.nextDoseDateMillis != null) }
    var nextDose by remember { mutableStateOf(initialMedication?.nextDoseAmount ?: "") }
    var daysUntilChange by remember { mutableStateOf(
        if (initialMedication?.nextDoseDateMillis != null) {
            ((initialMedication.nextDoseDateMillis - System.currentTimeMillis()) / 86400000L).coerceAtLeast(0).toString()
        } else ""
    ) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (initialMedication == null) "افزودن داروی جدید" else "ویرایش دارو") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام دارو") },
                modifier = Modifier.fillMaxWidth().testTag("med_name_input"),
                singleLine = true
            )
            
            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                label = { Text("مقدار مصرف فعلی (مثلاً ۱ قرص یا ۵۰۰ میلی‌گرم)") },
                modifier = Modifier.fillMaxWidth().testTag("med_dose_input"),
                singleLine = true
            )

            val context = LocalContext.current
            
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
            
            OutlinedTextField(
                value = skipIntervalDays,
                onValueChange = { skipIntervalDays = it },
                label = { Text("سیکل روزهای عدم مصرف (اختیاری، مثلا هر ۱۰ روز)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("med_skip_days_input"),
                singleLine = true
            )

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
                        nextDoseAmount = nextDose,
                        nextDoseDateMillis = targetDate,
                        nextDoseDurationDays = null
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
