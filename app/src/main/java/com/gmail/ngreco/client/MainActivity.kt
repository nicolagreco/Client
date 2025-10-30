package com.gmail.ngreco.client

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gmail.ngreco.client.databinding.ActivityMainBinding
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requiredPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = requiredPermissions.all { permission ->
                permissions[permission] == true
            }

            if (allGranted) {
                startSmsWorkflow()
            } else {
                showPermissionDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendSmsButton.setOnClickListener {
            when {
                hasAllRequiredPermissions() -> startSmsWorkflow()

                shouldShowPermissionRationale() -> {
                    showPermissionRationale()
                }

                else -> requestPermissionLauncher.launch(requiredPermissions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSmsStats()
    }

    private fun hasAllRequiredPermissions(): Boolean = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowPermissionRationale(): Boolean =
        requiredPermissions.any { permission -> shouldShowRequestPermissionRationale(permission) }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_rationale_title)
            .setMessage(R.string.permission_rationale_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestPermissionLauncher.launch(requiredPermissions)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_denied_title)
            .setMessage(R.string.permission_denied_message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startSmsWorkflow() {
        val smsSent = SmsSender.sendSms(this)
        if (smsSent) {
            scheduleSmsWorker()
            Toast.makeText(this, R.string.sms_scheduled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.sms_failed, Toast.LENGTH_LONG).show()
        }
        refreshSmsStats()
    }

    private fun scheduleSmsWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SmsSenderWorker>(20, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            SmsSenderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun refreshSmsStats() {
        val stats = SmsStatsRepository.getLatestStats(this)
        if (stats == null) {
            binding.smsCountStatus.text = getString(R.string.sms_stats_count_placeholder)
        } else {
            val formattedTime = DateFormat.getDateTimeInstance().format(Date(stats.recordedAt))
            binding.smsCountStatus.text = getString(
                R.string.sms_stats_count_value,
                stats.count,
                formattedTime
            )
        }
    }
}
