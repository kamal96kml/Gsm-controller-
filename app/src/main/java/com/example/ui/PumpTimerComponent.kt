package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SolarPumpPreferences
import com.example.ui.theme.AmberTimer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.RoseLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controller to handle device alarm ringtone & vibration when pump timer completes.
 */
class PumpAlarmController(private val context: Context) {
    private var ringtone: Ringtone? = null
    var isRinging by mutableStateOf(false)
        private set

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun startAlarm() {
        if (isRinging) return
        isRinging = true
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getValidRingtoneUri(context)

            ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    it.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.isLooping = true
                }
                it.play()
            }

            // Start rhythmic alert vibration
            triggerVibrationPattern()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerVibrationPattern() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 600, 250, 600, 250, 600)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 600, 250, 600), -1)
            }
        } catch (_: Exception) {}
    }

    fun stopAlarm() {
        try {
            ringtone?.stop()
        } catch (_: Exception) {}
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        isRinging = false
    }
}

/**
 * High-end Glassmorphic Pump Countdown Timer & Stop Reminder Card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PumpTimerCard(
    modifier: Modifier = Modifier,
    passcode: String,
    onStopPump: () -> Unit,
    onTimerFinishedLog: (durationMinutes: Long) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SolarPumpPreferences(context) }
    val alarmController = remember { PumpAlarmController(context) }
    val scope = rememberCoroutineScope()

    // Timer states
    var isTimerRunning by remember { mutableStateOf(prefs.isTimerActive) }
    var totalDurationSeconds by remember {
        mutableLongStateOf(
            if (prefs.timerDurationSeconds > 0) prefs.timerDurationSeconds else 1800L // 30 min default
        )
    }
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    var targetEndTimeMs by remember { mutableLongStateOf(prefs.timerEndTimeMs) }

    // Dialogs
    var showCustomDialog by remember { mutableStateOf(false) }
    var customHours by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("30") }
    var showAlarmDialog by remember { mutableStateOf(false) }

    // Calculate initial remaining seconds on load
    LaunchedEffect(Unit) {
        if (isTimerRunning && targetEndTimeMs > 0) {
            val now = System.currentTimeMillis()
            if (now >= targetEndTimeMs) {
                // Timer already expired while app was closed
                remainingSeconds = 0L
                isTimerRunning = false
                prefs.clearTimer()
                alarmController.startAlarm()
                showAlarmDialog = true
            } else {
                remainingSeconds = (targetEndTimeMs - now) / 1000L
            }
        }
    }

    // Ticking loop
    LaunchedEffect(isTimerRunning, targetEndTimeMs) {
        while (isTimerRunning && isActive) {
            val now = System.currentTimeMillis()
            val left = (targetEndTimeMs - now) / 1000L
            if (left <= 0) {
                remainingSeconds = 0L
                isTimerRunning = false
                prefs.clearTimer()
                alarmController.startAlarm()
                showAlarmDialog = true
                val minutesRan = (totalDurationSeconds / 60).coerceAtLeast(1)
                onTimerFinishedLog(minutesRan)
                break
            } else {
                remainingSeconds = left
            }
            delay(1000L)
        }
    }

    // Periodic vibration reminder while alarm is ringing
    LaunchedEffect(alarmController.isRinging) {
        while (alarmController.isRinging && isActive) {
            alarmController.triggerVibrationPattern()
            delay(2000L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            alarmController.stopAlarm()
        }
    }

    fun startTimerWithDuration(seconds: Long) {
        alarmController.stopAlarm()
        totalDurationSeconds = seconds
        remainingSeconds = seconds
        val now = System.currentTimeMillis()
        targetEndTimeMs = now + (seconds * 1000L)
        isTimerRunning = true
        prefs.saveTimer(seconds, targetEndTimeMs)
    }

    fun stopTimer() {
        isTimerRunning = false
        remainingSeconds = 0L
        targetEndTimeMs = 0L
        prefs.clearTimer()
        alarmController.stopAlarm()
        showAlarmDialog = false
    }

    fun extendTimer(addSeconds: Long) {
        if (isTimerRunning) {
            targetEndTimeMs += (addSeconds * 1000L)
            totalDurationSeconds += addSeconds
            remainingSeconds += addSeconds
            prefs.saveTimer(totalDurationSeconds, targetEndTimeMs)
        } else {
            startTimerWithDuration(addSeconds)
        }
    }

    // Infinite pulsing animation for alarm / active indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Formatted time string
    fun formatTime(totalSec: Long): String {
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }

    fun formatTargetTime(millis: Long): String {
        if (millis <= 0) return ""
        return SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(millis))
    }

    val progress = if (totalDurationSeconds > 0 && isTimerRunning) {
        ((totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    GlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pump_timer_card"),
        borderColor = if (alarmController.isRinging) RoseLight else AmberTimer.copy(alpha = 0.5f),
        backgroundColor = if (alarmController.isRinging) Color(0x35450A15) else Color(0x28172138)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (alarmController.isRinging) RoseLight.copy(alpha = 0.25f)
                                else AmberTimer.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (alarmController.isRinging) RoseLight else AmberTimer.copy(alpha = 0.6f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alarmController.isRinging) Icons.Default.NotificationsActive else Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (alarmController.isRinging) RoseLight else AmberTimer,
                            modifier = Modifier
                                .size(20.dp)
                                .then(if (alarmController.isRinging) Modifier.scale(pulseScale) else Modifier)
                        )
                    }

                    Column {
                        Text(
                            text = "مؤقت تشغيل المضخة والتذكير",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isTimerRunning) "التنازلي نشط • سيرن الهاتف للتذكير"
                            else if (alarmController.isRinging) "انتهى الوقت! المضخة قيد التشغيل"
                            else "حدد المدة وسيرن الهاتف لإيقاف المضخة",
                            color = if (alarmController.isRinging) RoseLight else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Status indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                alarmController.isRinging -> RoseLight.copy(alpha = 0.2f)
                                isTimerRunning -> AmberTimer.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.08f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            alarmController.isRinging -> "رنين المنبه 🔔"
                            isTimerRunning -> "يعمل الآن ⏳"
                            else -> "متوقف"
                        },
                        color = when {
                            alarmController.isRinging -> RoseLight
                            isTimerRunning -> AmberTimer
                            else -> TextMuted
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ALARM RINGING ACTIVE BANNER
            AnimatedVisibility(visible = alarmController.isRinging) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF881337), Color(0xFF4C0519))))
                        .border(1.dp, RoseLight, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Ringing",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(pulseScale)
                        )
                        Text(
                            text = "انتهى وقت التشغيل المحدد للمضخة!",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "يرجى إرسال أمر الإيقاف للحفاظ على المياه وحماية المضخة.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Big Stop Button
                        Button(
                            onClick = {
                                alarmController.stopAlarm()
                                showAlarmDialog = false
                                stopTimer()
                                onStopPump()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("timer_stop_pump_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إيقاف المضخة فوراً", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Silence Alarm Button
                        OutlinedButton(
                            onClick = {
                                alarmController.stopAlarm()
                                showAlarmDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("silence_alarm_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.3f)))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إسكات", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ACTIVE COUNTDOWN DISPLAY
            if (isTimerRunning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, AmberTimer.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الوقت المتبقي حتى الرنين",
                        color = AmberTimer.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Big Clock Digits
                    Text(
                        text = formatTime(remainingSeconds),
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = AmberTimer,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    // Target End Time Info
                    Text(
                        text = "سينتهي التوقيت ويرن الهاتف عند: ${formatTargetTime(targetEndTimeMs)}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Running controls: Quick extend + Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { extendTimer(300L) }, // +5 min
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(AmberTimer.copy(alpha = 0.6f), CyanPrimary.copy(alpha = 0.6f)))
                            )
                        ) {
                            Text("+5 دقائق", fontSize = 11.sp, color = AmberTimer)
                        }

                        OutlinedButton(
                            onClick = { extendTimer(900L) }, // +15 min
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(AmberTimer.copy(alpha = 0.6f), CyanPrimary.copy(alpha = 0.6f)))
                            )
                        ) {
                            Text("+15 دقيقة", fontSize = 11.sp, color = AmberTimer)
                        }

                        Button(
                            onClick = { stopTimer() },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("cancel_timer_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseLight.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء", fontSize = 11.sp, color = RoseLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (!alarmController.isRinging) {
                // IDLE PRESETS & QUICK SELECTOR
                Text(
                    text = "اختر مدة تشغيل المضخة:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // Presets Pills FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "15 دقيقة" to 900L,
                        "30 دقيقة" to 1800L,
                        "45 دقيقة" to 2700L,
                        "1 ساعة" to 3600L,
                        "ساعة ونصف" to 5400L,
                        "2 ساعة" to 7200L,
                        "3 ساعات" to 10800L
                    )

                    presets.forEach { (label, secs) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .clickable {
                                    startTimerWithDuration(secs)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = AmberTimer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Custom duration button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberTimer.copy(alpha = 0.15f))
                            .border(1.dp, AmberTimer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showCustomDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AmberTimer, modifier = Modifier.size(12.dp))
                            Text("مدة مخصصة...", color = AmberTimer, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // CUSTOM DURATION DIALOG
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = {
                Text(
                    text = "تحديد مدة تشغيل مخصصة للمؤقت",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل الساعات والدقائق التي ترغب في تشغيل المضخة خلالها، وسيقوم الهاتف بالرنين فور انتهائها:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customHours,
                            onValueChange = { if (it.length <= 2) customHours = it },
                            label = { Text("الساعات") },
                            placeholder = { Text("0") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberTimer,
                                unfocusedBorderColor = GlassCardBorder
                            )
                        )

                        OutlinedTextField(
                            value = customMinutes,
                            onValueChange = { if (it.length <= 2) customMinutes = it },
                            label = { Text("الدقائق") },
                            placeholder = { Text("30") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberTimer,
                                unfocusedBorderColor = GlassCardBorder
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hrs = customHours.toLongOrNull() ?: 0L
                        val mins = customMinutes.toLongOrNull() ?: 0L
                        val totalSecs = (hrs * 3600L) + (mins * 60L)
                        if (totalSecs > 0) {
                            startTimerWithDuration(totalSecs)
                            showCustomDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTimer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بدء المؤقت", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // ALARM RINGING PROMINENT MODAL DIALOG
    if (showAlarmDialog && alarmController.isRinging) {
        AlertDialog(
            onDismissRequest = { /* Force explicit user action */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm",
                    tint = RoseLight,
                    modifier = Modifier
                        .size(44.dp)
                        .scale(pulseScale)
                )
            },
            title = {
                Text(
                    text = "⏰ تذكير: انتهى وقت تشغيل المضخة!",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "انقضت مدة تشغيل المضخة المحددة. يرجى إيقاف المضخة فوراً لتجنب استنزاف البئر أو فيضان الخزان.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "رمز أمر الإيقاف: $passcode#OFF#",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        alarmController.stopAlarm()
                        showAlarmDialog = false
                        stopTimer()
                        onStopPump()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("إيقاف المضخة الآن (SMS)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            alarmController.stopAlarm()
                            showAlarmDialog = false
                            extendTimer(600L) // +10 min snooze
                        }
                    ) {
                        Text("تمديد 10 دقائق", color = AmberTimer)
                    }

                    TextButton(
                        onClick = {
                            alarmController.stopAlarm()
                            showAlarmDialog = false
                        }
                    ) {
                        Text("إسكات التنبيه فقط", color = TextSecondary)
                    }
                }
            }
        )
    }
}
