package com.gmail.ngreco.client

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SmsSenderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val smsSent = SmsSender.sendSms(applicationContext)
        val recentCount = SmsCounter.countRecentMatches(applicationContext)
        SmsStatsRepository.recordLatestStats(applicationContext, recentCount)

        return if (smsSent) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sms_sender_periodic"
    }
}
