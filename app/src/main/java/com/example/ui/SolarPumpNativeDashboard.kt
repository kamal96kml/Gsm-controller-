package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ActivityLog
import com.example.data.SolarPumpPreferences
import com.example.ui.theme.AmberTimer
import com.example.ui.theme.BgDarkDeep
import com.example.ui.theme.BgIndigoDeep
import com.example.ui.theme.BgSlateNavy
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldStart
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderAmber
import com.example.ui.theme.GlassCardBorderCyan
import com.example.ui.theme.GlassCardBorderEmerald
import com.example.ui.theme.GlassCardBorderRose
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseLight
import com.example.ui.theme.RoseStop
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SolarPumpNativeDashboard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val prefs = remember { SolarPumpPreferences(context) }

    var phoneNumber by remember { mutableStateOf(prefs.phoneNumber) }
    var passcode by remember { mutableStateOf(prefs.passcode) }
    var pumpStatus by remember { mutableStateOf(prefs.pumpStatus) }
    var isConfigOpen by remember { mutableStateOf(false) }
    var isAdvancedOpen by remember { mutableStateOf(false) }
    var newPasscode by remember { mutableStateOf("") }
    var authNumber by remember { mutableStateOf("") }
    var authNumberPos by remember { mutableStateOf("01") }
    var delNumber by remember { mutableStateOf("") }
    var relaySeconds by remember { mutableStateOf("0019") }
    var showResetDialog by remember { mutableStateOf(false) }
    var customCmd by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf(prefs.getLogs()) }

    fun triggerHapticFeedback() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 80, 50, 100),
                        intArrayOf(0, 220, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {
            // Ignore if vibration service is unavailable
        }
    }

    fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun addActionLog(title: String, cmd: String, type: String, modeUsed: String) {
        val newLog = ActivityLog(
            id = System.currentTimeMillis().toString(),
            timestamp = getCurrentTime(),
            title = title,
            command = cmd,
            type = type,
            mode = modeUsed
        )
        prefs.addLog(newLog)
        logs = prefs.getLogs()
    }

    fun triggerCall(number: String) {
        if (number.isBlank()) {
            Toast.makeText(context, "يرجى إدخال رقم الشريحة أولاً", Toast.LENGTH_SHORT).show()
            isConfigOpen = true
            return
        }
        val clean = number.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    }

    fun triggerSms(number: String, body: String) {
        if (number.isBlank()) {
            Toast.makeText(context, "يرجى إدخال رقم الشريحة أولاً", Toast.LENGTH_SHORT).show()
            isConfigOpen = true
            return
        }
        val clean = number.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$clean")).apply {
            putExtra("sms_body", body)
        }
        context.startActivity(intent)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // Simple, elegant, static dark background
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090D16),
                            Color(0xFF0F172A),
                            Color(0xFF0A0F1D)
                        )
                    )
                )
        ) {

            // Main Content List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar Card: "GSM Controller" title + subtle Settings gear icon only
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("header_card"),
                        borderColor = GlassCardBorderCyan
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, CyanPrimary.copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_gsm_logo),
                                        contentDescription = "GSM Controller Logo",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(11.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Column {
                                    Text(
                                        text = "GSM Controller",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                    Text(
                                        text = phoneNumber.ifBlank { "الرقم غير محدد" },
                                        color = CyanPrimary.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Subtle Settings gear icon at top right
                            IconButton(
                                onClick = { isConfigOpen = !isConfigOpen },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (isConfigOpen) CyanPrimary else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Status Indicator Card
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("status_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (pumpStatus) {
                                                "running" -> EmeraldLight
                                                "stopped" -> RoseLight
                                                else -> AmberTimer
                                            }
                                        )
                                )

                                Column {
                                    Text(
                                        text = "حالة المضخة",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = when (pumpStatus) {
                                            "running" -> "المضخة قيد التشغيل"
                                            "stopped" -> "المضخة متوقفة"
                                            else -> "جاهزة للاستخدام"
                                        },
                                        color = when (pumpStatus) {
                                            "running" -> EmeraldLight
                                            "stopped" -> RoseLight
                                            else -> TextPrimary
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Text(
                                text = "PIN: $passcode",
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Settings Drawer (Collapsible)
                item {
                    AnimatedVisibility(visible = isConfigOpen) {
                        GlassContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("config_panel"),
                            borderColor = CyanPrimary.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "إعدادات الشريحة ورمز PIN",
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("رقم هاتف الشريحة (SIM)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("phone_input"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = GlassCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = passcode,
                                    onValueChange = { if (it.length <= 4) passcode = it },
                                    label = { Text("رمز المرور (PIN الافتراضي: 1234)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("passcode_input"),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldStart,
                                        unfocusedBorderColor = GlassCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Button(
                                    onClick = {
                                        prefs.phoneNumber = phoneNumber
                                        prefs.passcode = passcode
                                        isConfigOpen = false
                                        Toast.makeText(context, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("save_config_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.35f)),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(listOf(CyanPrimary, EmeraldStart))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = "حفظ الإعدادات", color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 1) TOP BUTTON (Green Glass): تشغيل المضخة (SMS) -> [PASSCODE]#ON#
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .clickable {
                                triggerHapticFeedback()
                                pumpStatus = "running"
                                prefs.pumpStatus = "running"
                                val cmd = "$passcode#ON#"
                                addActionLog("تشغيل المضخة (SMS)", cmd, "start", "SMS")
                                triggerSms(phoneNumber, cmd)
                            }
                            .testTag("btn_start_sms"),
                        borderColor = GlassCardBorderEmerald,
                        backgroundColor = Color(0x3B10B981),
                        cornerRadius = 26.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(EmeraldStart.copy(alpha = 0.35f))
                                        .border(1.dp, EmeraldLight.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start SMS",
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "تشغيل المضخة (SMS)",
                                        color = Color(0xFFD1FAE5),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp
                                    )
                                    Text(
                                        text = "$passcode#ON#",
                                        color = EmeraldLight.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "SMS ON",
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 2) MIDDLE BUTTON (Red Glass): إيقاف المضخة (SMS) -> [PASSCODE]#OFF#
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .clickable {
                                triggerHapticFeedback()
                                pumpStatus = "stopped"
                                prefs.pumpStatus = "stopped"
                                val cmd = "$passcode#OFF#"
                                addActionLog("إيقاف المضخة (SMS)", cmd, "stop", "SMS")
                                triggerSms(phoneNumber, cmd)
                            }
                            .testTag("btn_stop_sms"),
                        borderColor = GlassCardBorderRose,
                        backgroundColor = Color(0x3BEF4444),
                        cornerRadius = 26.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(RoseStop.copy(alpha = 0.35f))
                                        .border(1.dp, RoseLight.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop SMS",
                                        tint = RoseLight,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "إيقاف المضخة (SMS)",
                                        color = Color(0xFFFFE4E6),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp
                                    )
                                    Text(
                                        text = "$passcode#OFF#",
                                        color = RoseLight.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, RoseLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "SMS OFF",
                                    color = RoseLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 3) BOTTOM BUTTON (Yellow/Amber Glass): تشغيل عن طريق الاتصال -> tel:
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .clickable {
                                triggerHapticFeedback()
                                pumpStatus = "running"
                                prefs.pumpStatus = "running"
                                addActionLog("تشغيل عن طريق الاتصال", "اتصال بالرقم $phoneNumber", "call", "اتصال")
                                triggerCall(phoneNumber)
                            }
                            .testTag("btn_start_call"),
                        borderColor = GlassCardBorderAmber,
                        backgroundColor = Color(0x3BF59E0B),
                        cornerRadius = 26.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(AmberTimer.copy(alpha = 0.35f))
                                        .border(1.dp, AmberTimer.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = AmberTimer,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "تشغيل عن طريق الاتصال",
                                        color = Color(0xFFFEF3C7),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = phoneNumber.ifBlank { "الرقم غير محدد" },
                                        color = AmberTimer.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, AmberTimer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "CALL",
                                    color = AmberTimer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Quick Diagnostics (Status 4466#EE#, Timer 4466#T#, Signal 4466#CSQ#)
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_diagnostics_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "أوامر سريعة للاستعلام والتشخيص",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionButton(
                                    title = "فحص الحالة",
                                    code = "$passcode#EE#",
                                    icon = Icons.Default.Info,
                                    tint = CyanPrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("check_status_btn")
                                ) {
                                    val cmd = "$passcode#EE#"
                                    addActionLog("استعلام حالة الجهاز", cmd, "info", "SMS")
                                    triggerSms(phoneNumber, cmd)
                                }

                                QuickActionButton(
                                    title = "مؤقت التتابع",
                                    code = "$passcode#T#",
                                    icon = Icons.Default.Timer,
                                    tint = AmberTimer,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("check_timer_btn")
                                ) {
                                    val cmd = "$passcode#T#"
                                    addActionLog("فحص مؤقت التتابع", cmd, "info", "SMS")
                                    triggerSms(phoneNumber, cmd)
                                }

                                QuickActionButton(
                                    title = "قوة الإشارة",
                                    code = "$passcode#CSQ?",
                                    icon = Icons.Default.Wifi,
                                    tint = EmeraldLight,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("check_signal_btn")
                                ) {
                                    val cmd = "$passcode#CSQ?"
                                    addActionLog("فحص قوة الإشارة", cmd, "info", "SMS")
                                    triggerSms(phoneNumber, cmd)
                                }
                            }
                        }
                    }
                }

                // 4) NEW SECTION: Advanced RTU5024 Configuration Commands (إعدادات وبرمجة الجهاز)
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advanced_config_card"),
                        borderColor = PurpleAccent.copy(alpha = 0.45f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header with expand toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAdvancedOpen = !isAdvancedOpen },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(PurpleAccent.copy(alpha = 0.25f))
                                            .border(1.dp, PurpleAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Advanced",
                                            tint = PurpleAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "إعدادات وبرمجة الجهاز",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "أوامر التهيئة المتقدمة (دليل RTU5024)",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { isAdvancedOpen = !isAdvancedOpen },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAdvancedOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Advanced",
                                        tint = TextSecondary
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isAdvancedOpen) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                    // 1) Setup New Password: [PASSCODE]#PWD[NEW_PASS]#PWD[NEW_PASS]#
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "تغيير رمز المرور (PIN)",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = newPasscode,
                                                onValueChange = { if (it.length <= 4) newPasscode = it },
                                                placeholder = { Text("رمز جديد (4 أرقام)", fontSize = 12.sp) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("new_passcode_input"),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = CyanPrimary,
                                                    unfocusedBorderColor = GlassCardBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                )
                                            )
                                            Button(
                                                onClick = {
                                                    if (newPasscode.length == 4) {
                                                        val cmd = "$passcode#PWD$newPasscode#PWD$newPasscode#"
                                                        addActionLog("تغيير رمز المرور", cmd, "config", "SMS")
                                                        triggerSms(phoneNumber, cmd)
                                                        newPasscode = ""
                                                    } else {
                                                        Toast.makeText(context, "يرجى كتابة رمز جديد مكون من 4 أرقام", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.height(50.dp).testTag("btn_change_pwd"),
                                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.35f)),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = Brush.horizontalGradient(listOf(CyanPrimary, EmeraldStart))
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("إرسال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = "الصيغة: $passcode#PWD[الرمز]#PWD[الرمز]#",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                                    // 2) Add Authorized Number: [PASSCODE]#TEL[NUMBER]#[POS]#
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "إضافة رقم مصرح له بالتحكم",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = authNumber,
                                                onValueChange = { authNumber = it },
                                                placeholder = { Text("06XXXXXXXX", fontSize = 12.sp) },
                                                modifier = Modifier
                                                    .weight(1.5f)
                                                    .testTag("auth_number_input"),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldStart,
                                                    unfocusedBorderColor = GlassCardBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                )
                                            )
                                            OutlinedTextField(
                                                value = authNumberPos,
                                                onValueChange = { if (it.length <= 3) authNumberPos = it },
                                                placeholder = { Text("01", fontSize = 12.sp) },
                                                modifier = Modifier
                                                    .weight(0.7f)
                                                    .testTag("auth_pos_input"),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldStart,
                                                    unfocusedBorderColor = GlassCardBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                )
                                            )
                                            Button(
                                                onClick = {
                                                    val clean = authNumber.replace(Regex("[^0-9+]"), "")
                                                    if (clean.isNotBlank()) {
                                                        val pos = authNumberPos.padStart(2, '0')
                                                        val cmd = "$passcode#TEL$clean#$pos#"
                                                        addActionLog("إضافة رقم مصرح له", cmd, "config", "SMS")
                                                        triggerSms(phoneNumber, cmd)
                                                        authNumber = ""
                                                    } else {
                                                        Toast.makeText(context, "يرجى إدخال رقم الهاتف", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.height(50.dp).testTag("btn_add_auth"),
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldStart.copy(alpha = 0.35f)),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = Brush.horizontalGradient(listOf(EmeraldStart, CyanPrimary))
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("إضافة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = "الصيغة: $passcode#TEL[الرقم]#[01]#",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                                    // 3) Delete Authorized Number: [PASSCODE]#DEL[NUMBER]#
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "حذف رقم مصرح له",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = delNumber,
                                                onValueChange = { delNumber = it },
                                                placeholder = { Text("الرقم المراد حذفه", fontSize = 12.sp) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("del_number_input"),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = RoseStop,
                                                    unfocusedBorderColor = GlassCardBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                )
                                            )
                                            Button(
                                                onClick = {
                                                    val clean = delNumber.replace(Regex("[^0-9+]"), "")
                                                    if (clean.isNotBlank()) {
                                                        val cmd = "$passcode#DEL$clean#"
                                                        addActionLog("حذف رقم مصرح له", cmd, "config", "SMS")
                                                        triggerSms(phoneNumber, cmd)
                                                        delNumber = ""
                                                    } else {
                                                        Toast.makeText(context, "يرجى إدخال الرقم للحذف", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.height(50.dp).testTag("btn_del_auth"),
                                                colors = ButtonDefaults.buttonColors(containerColor = RoseStop.copy(alpha = 0.35f)),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = Brush.horizontalGradient(listOf(RoseStop, AmberTimer))
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("حذف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = "الصيغة: $passcode#DEL[الرقم]#",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                                    // 4) Setup Relay Time (GOT): [PASSCODE]#GOT[SECONDS]#
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "ضبط مدة وصل التتابع (بالثواني GOT)",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = relaySeconds,
                                                onValueChange = { if (it.length <= 4) relaySeconds = it },
                                                placeholder = { Text("0019 (ثواني)", fontSize = 12.sp) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("relay_seconds_input"),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AmberTimer,
                                                    unfocusedBorderColor = GlassCardBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                )
                                            )
                                            Button(
                                                onClick = {
                                                    val clean = relaySeconds.padStart(4, '0')
                                                    val cmd = "$passcode#GOT$clean#"
                                                    addActionLog("ضبط مدة التتابع", cmd, "config", "SMS")
                                                    triggerSms(phoneNumber, cmd)
                                                },
                                                modifier = Modifier.height(50.dp).testTag("btn_set_got"),
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberTimer.copy(alpha = 0.35f)),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = Brush.horizontalGradient(listOf(AmberTimer, CyanPrimary))
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("ضبط", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = "الصيغة: $passcode#GOT[0019]# (4 أرقام مثل 0019 ثانية)",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                                    // 5) Access Permissions: Allow All (AA) & Authorized Only (AU)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "إذن الاستجابة للمكالمات",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val cmd = "$passcode#AA#"
                                                    addActionLog("السماح لجميع المتصلين", cmd, "config", "SMS")
                                                    triggerSms(phoneNumber, cmd)
                                                },
                                                modifier = Modifier.weight(1f).height(44.dp).testTag("btn_allow_all"),
                                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.25f)),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = Brush.horizontalGradient(listOf(CyanPrimary, EmeraldStart))
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("السماح للجميع (AA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    val cmd = "$passcode#AU#"
                                                    addActionLog("المصرح لهم فقط", cmd, "config", "SMS")
                                                    triggerSms(phoneNumber, cmd)
                                                },
                                                modifier = Modifier.weight(1f).height(44.dp).testTag("btn_auth_only"),
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberTimer.copy(alpha = 0.25f)),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = Brush.horizontalGradient(listOf(AmberTimer, PurpleAccent))
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("المصرح لهم فقط (AU)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                                    // 6) Reset Factory Default: [PASSCODE]#RESET#
                                    Button(
                                        onClick = { showResetDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_factory_reset"),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseStop.copy(alpha = 0.35f)),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.horizontalGradient(listOf(RoseStop, Color.Red))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Reset", tint = RoseLight, modifier = Modifier.size(18.dp))
                                            Text("إعادة ضبط المصنع (RESET)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Activity Feed
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activity_feed_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldLight)
                                    )
                                    Text(
                                        text = "سجل العمليات المباشر",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                if (logs.isNotEmpty()) {
                                    Text(
                                        text = "مسح السجل",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .clickable {
                                                prefs.clearLogs()
                                                logs = emptyList()
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            if (logs.isEmpty()) {
                                Text(
                                    text = "لا توجد عمليات مسجلة حتى الآن",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    logs.take(8).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White.copy(alpha = 0.04f))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                    .background(
                                                        when (item.type) {
                                                            "start" -> EmeraldLight
                                                            "stop" -> RoseLight
                                                            "call" -> AmberTimer
                                                            "info" -> CyanPrimary
                                                            else -> PurpleAccent
                                                        }
                                                    )
                                                )
                                                Column {
                                                    Text(
                                                        text = item.title,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = item.command,
                                                        color = CyanPrimary.copy(alpha = 0.8f),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = item.timestamp,
                                                    color = TextMuted,
                                                    fontSize = 10.sp
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color.White.copy(alpha = 0.08f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = item.mode,
                                                        color = TextSecondary,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5) Custom SMS Command Input (At the Very Bottom)
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_command_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "إرسال أمر نصي مخصص (Custom SMS)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customCmd,
                                    onValueChange = { customCmd = it },
                                    placeholder = { Text("$passcode#COMMAND#", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("custom_command_input"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (customCmd.isNotBlank()) {
                                            val raw = customCmd.trim()
                                            val full = if (raw.startsWith("$passcode#")) raw else "$passcode#$raw"
                                            addActionLog("أمر مخصص مرسل", full, "custom", "SMS")
                                            triggerSms(phoneNumber, full)
                                            customCmd = ""
                                        }
                                    }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpleAccent,
                                        unfocusedBorderColor = GlassCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (customCmd.isNotBlank()) {
                                            val raw = customCmd.trim()
                                            val full = if (raw.startsWith("$passcode#")) raw else "$passcode#$raw"
                                            addActionLog("أمر مخصص مرسل", full, "custom", "SMS")
                                            triggerSms(phoneNumber, full)
                                            customCmd = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .height(54.dp)
                                        .testTag("send_custom_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent.copy(alpha = 0.35f)),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(listOf(PurpleAccent, CyanPrimary))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = TextPrimary)
                                }
                            }
                        }
                    }
                }

                // 6) Developer Rights & Contact Card
                item {
                    GlassContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .clickable {
                                try {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:abdelhadi.kamal96@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "GSM Controller - RTU5024")
                                    }
                                    context.startActivity(emailIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "abdelhadi.kamal96@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("developer_rights_card"),
                        borderColor = CyanPrimary.copy(alpha = 0.45f),
                        backgroundColor = Color(0x2E111C33),
                        cornerRadius = 26.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.5.dp, CyanPrimary.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_gsm_logo),
                                        contentDescription = "Developer Logo",
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "حقوق التطوير والبرمجة",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "عبدالهادي كمال",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "abdelhadi.kamal96@gmail.com",
                                    color = CyanPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "جميع الحقوق محفوظة للمطور ©",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("تحذير: إعادة ضبط المصنع", fontWeight = FontWeight.Bold, color = RoseLight) },
                    text = { Text("هل أنت متأكد من إعادة ضبط مصنع جهاز RTU5024؟ سيتم مسح جميع الأرقام المسجلة واسترجاع رمز المرور الافتراضي 1234.", color = TextPrimary) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showResetDialog = false
                                val cmd = "$passcode#RESET#"
                                addActionLog("إعادة ضبط المصنع", cmd, "config", "SMS")
                                triggerSms(phoneNumber, cmd)
                            }
                        ) {
                            Text("نعم، إعادة الضبط", color = RoseLight, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("إلغاء", color = TextSecondary)
                        }
                    },
                    containerColor = BgDarkDeep,
                    shape = RoundedCornerShape(18.dp)
                )
            }
        }
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassCardBorder,
    backgroundColor: Color = GlassCardBg,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        borderColor.copy(alpha = 0.55f),
                        borderColor.copy(alpha = 0.18f)
                    )
                ),
                RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent
                    )
                )
            )
        ) {
            content()
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    code: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(text = code, color = tint, fontSize = 9.sp)
        }
    }
}
