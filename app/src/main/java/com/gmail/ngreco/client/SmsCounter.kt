package com.gmail.ngreco.client

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import java.util.concurrent.TimeUnit

object SmsCounter {

    private const val TAG = "SmsCounter"
    private val INBOX_URI: Uri = Telephony.Sms.Inbox.CONTENT_URI

    private const val TARGET_ADDRESS = "45600"
    private const val TARGET_BODY = "."

    fun countRecentMatches(context: Context, minutesBack: Long = 20): Int =
        runCatching {
            val cutoff = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(minutesBack)
            val projection = arrayOf(Telephony.Sms._ID)
            val selection = "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.BODY} = ? AND ${Telephony.Sms.DATE} >= ?"
            val selectionArgs = arrayOf(TARGET_ADDRESS, TARGET_BODY, cutoff.toString())

            context.contentResolver.query(
                INBOX_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                cursor.count
            } ?: 0
        }.getOrElse { throwable ->
            if (throwable is SecurityException) {
                Log.w(TAG, "SMS read permission denied", throwable)
            } else {
                Log.e(TAG, "Failed to count SMS messages", throwable)
            }
            0
        }
}
