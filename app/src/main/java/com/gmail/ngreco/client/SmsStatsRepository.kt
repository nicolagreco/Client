package com.gmail.ngreco.client

import android.content.Context
import android.content.SharedPreferences

object SmsStatsRepository {

    private const val PREFS_NAME = "sms_stats"
    private const val KEY_LAST_COUNT = "last_count"
    private const val KEY_LAST_RECORDED_AT = "last_recorded_at"

    fun recordLatestStats(context: Context, count: Int) {
        prefs(context).edit()
            .putInt(KEY_LAST_COUNT, count)
            .putLong(KEY_LAST_RECORDED_AT, System.currentTimeMillis())
            .apply()
    }

    fun getLatestStats(context: Context): SmsStats? {
        val prefs = prefs(context)
        if (!prefs.contains(KEY_LAST_COUNT) || !prefs.contains(KEY_LAST_RECORDED_AT)) {
            return null
        }
        val count = prefs.getInt(KEY_LAST_COUNT, 0)
        val recordedAt = prefs.getLong(KEY_LAST_RECORDED_AT, 0L)
        if (recordedAt == 0L) {
            return null
        }
        return SmsStats(count = count, recordedAt = recordedAt)
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

data class SmsStats(
    val count: Int,
    val recordedAt: Long
)
