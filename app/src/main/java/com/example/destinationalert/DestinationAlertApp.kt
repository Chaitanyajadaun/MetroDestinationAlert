package com.example.destinationalert

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DestinationAlertApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_tracking_channel",
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW 
            ).apply {
                description = "Used to show persistent notification for active trip tracking"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
             // High importance channel for the final alert
            val alertChannel = NotificationChannel(
                "destination_alert_channel",
                "Destination Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you approach your destination"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }
}
