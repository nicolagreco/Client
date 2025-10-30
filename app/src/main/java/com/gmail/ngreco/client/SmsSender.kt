package com.gmail.ngreco.client

import android.content.Context
import android.telephony.SmsManager

object SmsSender {

    private const val SMS_PHONE_NUMBER = "3481503476"

    fun sendSms(context: Context): Boolean =
        runCatching {
            val smsManager = context.getSystemService(SmsManager::class.java)
                ?: SmsManager.getDefault()
            val message = context.getString(R.string.sms_body)
            smsManager.sendTextMessage(
                SMS_PHONE_NUMBER,
                null,
                message,
                null,
                null
            )
        }.isSuccess
}
