package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ActivityLog(
    val id: String,
    val timestamp: String,
    val title: String,
    val command: String,
    val type: String, // "start", "stop", "info", "custom"
    val mode: String // "اتصال" or "SMS"
)

class SolarPumpPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("solar_pump_prefs", Context.MODE_PRIVATE)

    var phoneNumber: String
        get() = prefs.getString("phone_number", "0600000000") ?: "0600000000"
        set(value) = prefs.edit().putString("phone_number", value).apply()

    var passcode: String
        get() = prefs.getString("passcode", "1234") ?: "1234"
        set(value) = prefs.edit().putString("passcode", value).apply()

    var controlMode: String
        get() = prefs.getString("control_mode", "call") ?: "call"
        set(value) = prefs.edit().putString("control_mode", value).apply()

    var pumpStatus: String
        get() = prefs.getString("pump_status", "unknown") ?: "unknown"
        set(value) = prefs.edit().putString("pump_status", value).apply()

    fun getLogs(): List<ActivityLog> {
        val raw = prefs.getString("logs_json", null) ?: return listOf(
            ActivityLog(
                id = "init_1",
                timestamp = "12:00:00",
                title = "جاهز للاتصال بنظام RTU5024",
                command = "جاهز (Ready)",
                type = "system",
                mode = "نظام"
            )
        )
        val list = mutableListOf<ActivityLog>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ActivityLog(
                        id = obj.getString("id"),
                        timestamp = obj.getString("timestamp"),
                        title = obj.getString("title"),
                        command = obj.getString("command"),
                        type = obj.optString("type", "info"),
                        mode = obj.optString("mode", "SMS")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun addLog(log: ActivityLog) {
        val current = getLogs().toMutableList()
        current.add(0, log)
        val trimmed = if (current.size > 30) current.take(30) else current
        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("timestamp", item.timestamp)
                put("title", item.title)
                put("command", item.command)
                put("type", item.type)
                put("mode", item.mode)
            }
            array.put(obj)
        }
        prefs.edit().putString("logs_json", array.toString()).apply()
    }

    fun clearLogs() {
        prefs.edit().remove("logs_json").apply()
    }
}
