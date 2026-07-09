package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.AppFontSize
import com.example.data.AppThemeMode
import com.example.data.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val themeMode by settingsRepository.themeMode.collectAsState()
    val fontSize by settingsRepository.fontSize.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات برنامه") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "هیوا در زبان کردی یعنی امید و آرزو",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "به امید روزهای خوش برای شما",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text("تم برنامه", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = themeMode == AppThemeMode.SYSTEM,
                    onClick = { settingsRepository.setThemeMode(AppThemeMode.SYSTEM) },
                    label = { Text("سیستم") }
                )
                FilterChip(
                    selected = themeMode == AppThemeMode.LIGHT,
                    onClick = { settingsRepository.setThemeMode(AppThemeMode.LIGHT) },
                    label = { Text("روشن") }
                )
                FilterChip(
                    selected = themeMode == AppThemeMode.DARK,
                    onClick = { settingsRepository.setThemeMode(AppThemeMode.DARK) },
                    label = { Text("تاریک") }
                )
            }

            Divider()

            Text("اندازه فونت", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = fontSize == AppFontSize.SMALL,
                        onClick = { settingsRepository.setFontSize(AppFontSize.SMALL) }
                    )
                    Text("کوچک", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = fontSize == AppFontSize.MEDIUM,
                        onClick = { settingsRepository.setFontSize(AppFontSize.MEDIUM) }
                    )
                    Text("متوسط", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = fontSize == AppFontSize.LARGE,
                        onClick = { settingsRepository.setFontSize(AppFontSize.LARGE) }
                    )
                    Text("بزرگ", modifier = Modifier.padding(start = 8.dp))
                }
            }
            
            Divider()
            
            val isAlarmEnabled by settingsRepository.isAlarmEnabled.collectAsState()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "یادآوری هشدار (نوتیفیکیشن)", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary
                )
                Switch(
                    checked = isAlarmEnabled,
                    onCheckedChange = { settingsRepository.setAlarmEnabled(it) }
                )
            }
            
            Divider()
            
            val context = LocalContext.current
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://donito.me/HivaRad"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.LocalCafe, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("نویسنده برنامه رو به یک قهوه دعوت کن")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "نسخه کنونی: 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
