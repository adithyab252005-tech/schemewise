package com.schemewise.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * DeadlineAlertEngine — Localized push-notification system for scheme deadlines.
 *
 * Usage (from a SavedScreen or SchemeDetailScreen ViewModel):
 *
 *   DeadlineAlertEngine.scheduleAlert(
 *       context = context,
 *       schemeId = "pm_kisan_2024",
 *       schemeName = "PM-KISAN",
 *       daysUntilDeadline = 14
 *   )
 *
 * This schedules a WorkManager job that fires a local notification
 * 2 days before the deadline. Zero backend, 100% on-device.
 */
object DeadlineAlertEngine {

    private const val CHANNEL_ID   = "scheme_deadlines"
    private const val CHANNEL_NAME = "Scheme Deadline Alerts"
    private const val WORK_TAG     = "deadline_"

    fun scheduleAlert(
        context: Context,
        schemeId: String,
        schemeName: String,
        daysUntilDeadline: Long
    ) {
        // We fire notification 2 days before deadline — max urgency
        val daysBeforeAlert = 2L
        val delayDays = (daysUntilDeadline - daysBeforeAlert).coerceAtLeast(0L)

        val data = workDataOf(
            "scheme_id"   to schemeId,
            "scheme_name" to schemeName,
            "days_left"   to daysBeforeAlert
        )

        val request = OneTimeWorkRequestBuilder<DeadlineNotificationWorker>()
            .setInitialDelay(delayDays, TimeUnit.DAYS)
            .setInputData(data)
            .addTag("$WORK_TAG$schemeId")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "$WORK_TAG$schemeId",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancelAlert(context: Context, schemeId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("$WORK_TAG$schemeId")
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for upcoming scheme application deadlines"
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}

/** WorkManager Worker that fires the actual notification */
class DeadlineNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val schemeName = inputData.getString("scheme_name") ?: "A saved scheme"
        val daysLeft   = inputData.getLong("days_left", 2L)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "scheme_deadlines")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Deadline Alert — SchemeWise")
            .setContentText("$schemeName closes in $daysLeft day${if (daysLeft == 1L) "" else "s"}. Apply now!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("\"$schemeName\" application window closes in $daysLeft day${if (daysLeft == 1L) "" else "s"}. " +
                    "Open SchemeWise to complete your application before it's too late.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(schemeName.hashCode(), notification)
        return Result.success()
    }
}
