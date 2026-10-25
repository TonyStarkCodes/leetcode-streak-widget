package com.tonystarkcodes.leetcodestreakwidget.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonystarkcodes.leetcodestreakwidget.data.model.LeetCodeProfile
import com.tonystarkcodes.leetcodestreakwidget.data.repository.LeetCodeRepository
import com.tonystarkcodes.leetcodestreakwidget.service.WidgetUpdateWorker
import com.tonystarkcodes.leetcodestreakwidget.ui.theme.*
import com.tonystarkcodes.leetcodestreakwidget.widget.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeetCodeStreakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StreakAppScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakAppScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { LeetCodeRepository(context) }

    var usernameInput by remember { mutableStateOf(repository.preferences.getUsername()) }
    var cachedProfile by remember { mutableStateOf<LeetCodeProfile?>(repository.getCachedProfile()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var autoSyncEnabled by remember { mutableStateOf(repository.preferences.isAutoSyncEnabled()) }

    fun refreshData(username: String = usernameInput) {
        if (username.isBlank()) {
            errorMessage = "Please enter your LeetCode handle."
            return
        }
        isLoading = true
        errorMessage = null
        focusManager.clearFocus()

        coroutineScope.launch {
            val result = repository.syncProfile(username.trim())
            isLoading = false
            if (result.isSuccess) {
                cachedProfile = result.getOrNull()
                Toast.makeText(context, "Synced successfully! Widgets updated.", Toast.LENGTH_SHORT).show()
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to sync profile."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Fire",
                            tint = LeetCodeYellow,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LeetCode Streak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshData() },
                        enabled = !isLoading && usernameInput.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LeetCodeYellow
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { errorMessage = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Profile Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LeetCode Profile",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your handle to track your streak & sync widgets.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. tourist or your_handle") },
                        label = { Text("LeetCode Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = LeetCodeYellow
                            )
                        },
                        trailingIcon = {
                            if (usernameInput.isNotEmpty()) {
                                IconButton(onClick = { usernameInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { refreshData() }),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { refreshData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && usernameInput.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeetCodeYellow,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.Black,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Syncing with LeetCode...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "Sync")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Profile & Update Widgets", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Streak Hero Card
            cachedProfile?.let { profile ->
                StreakHeroCard(profile = profile)
                ProblemsSolvedCard(profile = profile)
            }

            // Widgets Preview & Pinning Card
            WidgetsSection(cachedProfile = cachedProfile)

            // Battery & Sync Settings Card
            BatteryAndSettingsCard(
                autoSyncEnabled = autoSyncEnabled,
                lastSyncTime = cachedProfile?.lastSyncTimestamp ?: 0L,
                onToggleAutoSync = { enabled ->
                    autoSyncEnabled = enabled
                    repository.preferences.setAutoSync(enabled)
                    if (enabled) {
                        WidgetUpdateWorker.schedulePeriodicSync(context)
                        Toast.makeText(context, "Battery-safe sync enabled (every 12h)", Toast.LENGTH_SHORT).show()
                    } else {
                        WidgetUpdateWorker.cancelPeriodicSync(context)
                        Toast.makeText(context, "Manual-only mode enabled (0% battery drain)", Toast.LENGTH_SHORT).show()
                    }
                },
                onManualRefresh = { refreshData() },
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun StreakHeroCard(profile: LeetCodeProfile) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (profile.realName.isNotBlank()) profile.realName else "@${profile.username}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    if (profile.realName.isNotBlank()) {
                        Text(
                            text = "@${profile.username}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Solved Status Pill
                val statusContainer = when {
                    profile.solvedToday -> SolvedGreenContainer
                    profile.currentStreak > 0 -> PendingAmberContainer
                    else -> BrokenRedContainer
                }
                val statusContent = when {
                    profile.solvedToday -> SolvedGreenText
                    profile.currentStreak > 0 -> PendingAmberText
                    else -> BrokenRedText
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (profile.solvedToday) "Solved Today 🔥" else if (profile.currentStreak > 0) "Pending Today ⚠️" else "Streak Lost",
                        color = statusContent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Flame & Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = LeetCodeYellow,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${profile.currentStreak}",
                    fontWeight = FontWeight.Black,
                    fontSize = 58.sp,
                    lineHeight = 58.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "DAY STREAK",
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sub stats: Best streak & Total active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${profile.maxStreak}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Best Streak",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${profile.totalActiveDays}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Active Days",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val rankText = if (profile.ranking > 0) "#${String.format(Locale.US, "%,d", profile.ranking)}" else "--"
                    Text(
                        text = rankText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Global Rank",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accuracy guarantee explanation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Verified",
                    tint = LeetCodeYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profile.streakExplanation,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ProblemsSolvedCard(profile: LeetCodeProfile) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solved Problems",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${profile.totalSolved} Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LeetCodeYellow
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ProblemStatRow(name = "Easy", count = profile.easySolved, total = profile.totalSolved, color = LeetCodeEasy)
            Spacer(modifier = Modifier.height(8.dp))
            ProblemStatRow(name = "Medium", count = profile.mediumSolved, total = profile.totalSolved, color = LeetCodeMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ProblemStatRow(name = "Hard", count = profile.hardSolved, total = profile.totalSolved, color = LeetCodeHard)
        }
    }
}

@Composable
fun ProblemStatRow(name: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) (count.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = color)
            Text(text = "$count", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun WidgetsSection(cachedProfile: LeetCodeProfile?) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Widgets",
                    tint = LeetCodeYellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Home Screen Widgets",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Adaptive Dark/Light mode • Tap to pin to Home Screen",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2x1 Widget Item
            WidgetItem(
                title = "2x1 Compact Streak",
                description = "Minimalist flame + streak count + status pill",
                onPin = { pinWidget(context, StreakWidget2x1Provider::class.java) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Widget Item
            WidgetItem(
                title = "2x2 Square Widget",
                description = "Large streak counter, solved count & 1-tap refresh button",
                onPin = { pinWidget(context, StreakWidget2x2Provider::class.java) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4x2 Widget Item
            WidgetItem(
                title = "4x2 Rich Dashboard",
                description = "Full overview with streak, Easy/Med/Hard stats & rank",
                onPin = { pinWidget(context, StreakWidget4x2Provider::class.java) }
            )
        }
    }
}

@Composable
fun WidgetItem(title: String, description: String, onPin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalButton(
            onClick = onPin,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun pinWidget(context: Context, providerClass: Class<*>) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, providerClass)
            appWidgetManager.requestPinAppWidget(provider, null, null)
            Toast.makeText(context, "Pinning widget to Home Screen...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Long-press your home screen to add the widget manually.", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "Long-press your home screen to add the widget.", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun BatteryAndSettingsCard(
    autoSyncEnabled: Boolean,
    lastSyncTime: Long,
    onToggleAutoSync: (Boolean) -> Unit,
    onManualRefresh: () -> Unit,
    isLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery",
                    tint = LeetCodeYellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Battery & Sync Policy",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Designed for 0% idle battery drain",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-sync switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Periodic Background Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (autoSyncEnabled) "Syncs every 12h only when battery is not low." else "Manual only (Widgets update when you tap ⟳)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onToggleAutoSync,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = LeetCodeYellow,
                        checkedTrackColor = LeetCodeYellow.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Last synced status & quick refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formattedTime = if (lastSyncTime > 0L) {
                    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    sdf.format(Date(lastSyncTime))
                } else "Never"

                Text(
                    text = "Last synced: $formattedTime",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                OutlinedButton(
                    onClick = onManualRefresh,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh All", fontSize = 12.sp)
                }
            }
        }
    }
}
