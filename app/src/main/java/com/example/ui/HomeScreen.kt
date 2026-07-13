package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Medication
import com.example.data.SettingsRepository
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    settingsRepository: SettingsRepository,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val lalezarFont = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.example.R.font.lalezar))
    val medications by viewModel.activeMedications.collectAsState()
    val takenCount by viewModel.takenTodayCount.collectAsState()

    var currentTimeMinutes by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE)) }
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentTimeMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val currentSecond = cal.get(Calendar.SECOND)
            kotlinx.coroutines.delay((60 - currentSecond) * 1000L)
        }
    }

    val sortedMedications = remember(medications, currentTimeMinutes) {
        medications.sortedBy { med ->
            val times = med.timeOfDay.split(",").map { it.trim() }
            val upcomingTimes = times.mapNotNull { timeStr ->
                val parts = timeStr.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].toIntOrNull() ?: 0
                    val m = parts[1].toIntOrNull() ?: 0
                    val totalMins = h * 60 + m
                    if (totalMins >= currentTimeMinutes) totalMins else totalMins + 24 * 60
                } else null
            }
            upcomingTimes.minOrNull() ?: Int.MAX_VALUE
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "مسیر سلامتی", 
                        fontSize = 22.sp,
                        fontFamily = lalezarFont,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    ) {
                        Text("هیوا", fontSize = 26.sp, fontFamily = lalezarFont, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_medication_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن دارو")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MoraleBooster(takenCount, medications.size, settingsRepository)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (medications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ دارویی ثبت نشده است. مسیر خود را شروع کنید!", 
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedMedications, key = { it.id }) { med ->
                        MedicationCard(
                            medication = med,
                            onTakeClick = { viewModel.markAsTaken(med.id) },
                            onEditClick = { onEditClick(med.id) },
                            onDeleteClick = { viewModel.deleteMedication(med.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoraleBooster(taken: Int, total: Int, settingsRepository: SettingsRepository) {
    val duration by settingsRepository.treatmentDuration.collectAsState()
    val startDate by settingsRepository.treatmentStartDate.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    
    val elapsedMillis = System.currentTimeMillis() - startDate
    val elapsedDays = (elapsedMillis / 86400000L).toInt().coerceAtLeast(0)
    val remainingDays = (duration - elapsedDays).coerceAtLeast(0)
    
    val progress = if (duration > 0) {
        elapsedDays.toFloat() / duration.toFloat()
    } else {
        1f
    }.coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(targetValue = progress)

    val quote = when {
        remainingDays == 0 -> "تبریک! دوره درمان شما به پایان رسید."
        taken == 0 -> "امروز یک روز عالی برای مراقبت از خودت است."
        taken < total -> "مسیر سلامتی با همین انتخاب‌های کوچک ساخته می‌شود."
        else -> "عالی بود! تو قوی‌تر از چیزی هستی که فکر می‌کنی!"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_heart")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val circleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showDialog = true }
                    .padding(8.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(110.dp).graphicsLayer { rotationZ = circleRotation },
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    strokeWidth = 10.dp,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Health Heart",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "$remainingDays روز",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "امروز باید $total دارو مصرف کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("تنظیم دوره درمان") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("مدت زمان دوره را انتخاب کنید:")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(
                            selected = duration == 30,
                            onClick = { settingsRepository.setTreatmentDuration(30) },
                            label = { Text("۳۰ روز") }
                        )
                        FilterChip(
                            selected = duration == 60,
                            onClick = { settingsRepository.setTreatmentDuration(60) },
                            label = { Text("۶۰ روز") }
                        )
                        FilterChip(
                            selected = duration == 90,
                            onClick = { settingsRepository.setTreatmentDuration(90) },
                            label = { Text("۹۰ روز") }
                        )
                    }
                    OutlinedButton(
                        onClick = { settingsRepository.resetTreatmentStartDate() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("شروع مجدد دوره درمان (از امروز)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    onTakeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var securityAnswer by remember { mutableStateOf("") }
    
    // Check if there is an upcoming dose change that has arrived
    val hasDoseChange = medication.nextDoseDateMillis != null && medication.nextDoseDateMillis <= System.currentTimeMillis()
    val activeDose = if (hasDoseChange && medication.nextDoseAmount.isNotEmpty()) medication.nextDoseAmount else medication.currentDose

    val epochDay = Calendar.getInstance().timeInMillis / 86400000L
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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scale = if (isConsumptionDay) iconScale else 1f
    val rotation = if (isConsumptionDay) haloRotation else 0f

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConsumptionDay) MaterialTheme.colorScheme.surface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.08f),
                ambientColor = Color.Black.copy(alpha = 0.04f)
            )
            .testTag("med_card_${medication.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isConsumptionDay) {
                    Canvas(modifier = Modifier.matchParentSize().graphicsLayer { rotationZ = rotation }) {
                        val strokeWidth = 3.dp.toPx()
                        drawArc(
                            color = androidx.compose.ui.graphics.Color(0xFF81C784), // Primary-ish
                            startAngle = 0f,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        drawArc(
                            color = androidx.compose.ui.graphics.Color(0xFF4DB6AC), // Tertiary-ish
                            startAngle = 270f,
                            sweepAngle = 60f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isConsumptionDay) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .graphicsLayer { scaleX = scale; scaleY = scale },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (medication.type == "INJECTION") Icons.Default.Vaccines else Icons.Default.LocalPharmacy, 
                        contentDescription = null, 
                        tint = if (isConsumptionDay) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isConsumptionDay) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (medication.type == "INJECTION") "مقدار تزریق" else "مقدار مصرف"}: $activeDose - ساعت(ها): ${medication.timeOfDay.replace(",", " و ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isConsumptionDay) {
                    Text(
                        text = if (medication.type == "INJECTION") "امروز روز تزریق نیست" else "امروز روز عدم مصرف است",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (medication.type == "INJECTION" && medication.injectionIntervalDays != null) {
                    Text(
                        text = "تزریق هر ${medication.injectionIntervalDays} روز یکبار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                } else if (medication.type == "PILL" && medication.cycleConsumptionDays != null && medication.cycleRestDays != null) {
                    Text(
                        text = "سیکل: ${medication.cycleConsumptionDays} روز مصرف، ${medication.cycleRestDays} روز استراحت",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                } else if (medication.type == "PILL" && medication.skipIntervalDays != null) {
                    Text(
                        text = "عدم مصرف: هر ${medication.skipIntervalDays} روز یکبار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                if (medication.nextDoseDateMillis != null && !hasDoseChange) {
                    val daysLeft = ((medication.nextDoseDateMillis - System.currentTimeMillis()) / 86400000L).coerceAtLeast(0)
                    var text = "یادآور: تغییر دوز به ${medication.nextDoseAmount} در $daysLeft روز آینده"
                    if (medication.nextDoseDurationDays != null) {
                        text += " (طول دوره: ${medication.nextDoseDurationDays} روز)"
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            if (isConsumptionDay) {
                IconButton(onClick = onTakeClick, modifier = Modifier.testTag("take_btn_${medication.id}")) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = if (medication.type == "INJECTION") "تزریق شد" else "مصرف شد", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    
    if (showDeleteDialog || showEditDialog) {
        var num1 by remember { mutableStateOf(5) }
        var num2 by remember { mutableStateOf(7) }
        
        LaunchedEffect(showDeleteDialog, showEditDialog) {
            if (showDeleteDialog || showEditDialog) {
                num1 = (1..10).random()
                num2 = (1..10).random()
            }
        }
        
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                showEditDialog = false
                securityAnswer = ""
            },
            title = { Text("تایید امنیتی") },
            text = {
                Column {
                    Text("برای جلوگیری از تغییرات تصادفی توسط کودکان، لطفا پاسخ سوال زیر را وارد کنید:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = securityAnswer,
                        onValueChange = { securityAnswer = it },
                        label = { Text("حاصل جمع $num1 + $num2 = ؟") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (securityAnswer == (num1 + num2).toString()) {
                            if (showDeleteDialog) onDeleteClick()
                            if (showEditDialog) onEditClick()
                            showDeleteDialog = false
                            showEditDialog = false
                            securityAnswer = ""
                        }
                    }
                ) {
                    Text("تایید")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    showEditDialog = false
                    securityAnswer = ""
                }) {
                    Text("انصراف")
                }
            }
        )
    }
}
