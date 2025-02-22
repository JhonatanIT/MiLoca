package org.jibanez.miloca.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log

/**
 * LocationApp is the main Application class for the location tracking app.
 *
 * It initializes necessary components, such as the notification channel for location updates.
 * This class is responsible for setting up the environment that the entire app operates in,
 * primarily setting up the notification channel that the app will use.
 */
class LocationApp: Application() {

    companion object {
        const val LOCATION_CHANNEL_ID = "location_channel"
        const val LOCATION_CHANNEL_NAME = "Location"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            LOCATION_CHANNEL_ID,
            LOCATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for location-related notifications" // Add a description
            // You can set other properties here, e.g.,
            // setShowBadge(false)
            // setSound(null, null)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the channel already exists to avoid redundant creation
        if (notificationManager.getNotificationChannel(LOCATION_CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(channel)
            Log.d("LocationApp", "Notification channel created: $LOCATION_CHANNEL_ID")
        } else {
            Log.d("LocationApp", "Notification channel already exists: $LOCATION_CHANNEL_ID")
        }
    }
}