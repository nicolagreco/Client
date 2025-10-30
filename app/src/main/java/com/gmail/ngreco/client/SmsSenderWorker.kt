package com.gmail.ngreco.client

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SmsSenderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result =
        if (SmsSender.sendSms(applicationContext)) {
            Result.success()
        } else {
            Result.retry()
        }

    companion object {
        const val WORK_NAME = "sms_sender_periodic"
    }
}
