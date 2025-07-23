package com.example.travel.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionService : Service() {

    private lateinit var activityRecognitionClient: ActivityRecognitionClient

    companion object {
        const val ACTION_ACTIVITY_DETECTED = "com.example.travel.ACTION_ACTIVITY_DETECTED"
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_ACTIVITY_CONFIDENCE = "activity_confidence"
    }

    override fun onCreate() {
        super.onCreate()
        activityRecognitionClient = ActivityRecognitionClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.mostProbableActivity?.let { activity ->
                val broadcastIntent = Intent(ACTION_ACTIVITY_DETECTED).apply {
                    putExtra(EXTRA_ACTIVITY_TYPE, activity.type)
                    putExtra(EXTRA_ACTIVITY_CONFIDENCE, activity.confidence)
                }
                sendBroadcast(broadcastIntent)

                // Handle detected activity
                when (activity.type) {
                    DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_BICYCLE, DetectedActivity.IN_VEHICLE -> {
                        println("Detected active: ${activity.type} with confidence ${activity.confidence}")
                        // TODO: Call journeyViewModel.startNewJourney() here
                    }
                    DetectedActivity.STILL -> {
                        println("Detected still: ${activity.type} with confidence ${activity.confidence}")
                        // TODO: Call journeyViewModel.stopCurrentJourney() here
                    }
                    else -> {
                        println("Detected other activity: ${activity.type} with confidence ${activity.confidence}")
                    }
                }
            }
        } else {
            // Request activity updates
            val task = activityRecognitionClient.requestActivityUpdates(
                10000, // 10 seconds interval
                getActivityDetectionPendingIntent()
            )
            task.addOnSuccessListener { println("Activity updates requested successfully") }
            task.addOnFailureListener { e -> println("Failed to request activity updates: ${e.message}") }
        }
        return START_STICKY
    }

    private fun getActivityDetectionPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityRecognitionService::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        activityRecognitionClient.removeActivityUpdates(getActivityDetectionPendingIntent())
            .addOnSuccessListener { println("Activity updates removed successfully") }
            .addOnFailureListener { e -> println("Failed to remove activity updates: ${e.message}") }
    }
}
