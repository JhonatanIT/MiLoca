package org.jibanez.miloca.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * LocationApp is the main Application class for the location tracking app.
 *
 * It initializes necessary components, such as the notification channel for location updates.
 * This class is responsible for setting up the environment that the entire app operates in,
 * primarily setting up the notification channel that the app will use.
 */
class LocationApp : Application() {

    companion object {
        const val LOCATION_CHANNEL_ID = "location_channel"
        const val LOCATION_CHANNEL_NAME = "Location"
        const val SENSOR_CHANNEL_ID = "sensor_channel"
        const val SENSOR_CHANNEL_NAME = "Sensor"
        const val MEDIA_PROJECTION_CHANNEL_ID = "screen_recording_channel"
        const val MEDIA_PROJECTION_CHANNEL_NAME = "Screen Recording"
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LocationApp)
            modules(appModule)
        }
        createNotificationChannel(LOCATION_CHANNEL_ID, LOCATION_CHANNEL_NAME)
        createNotificationChannel(SENSOR_CHANNEL_ID, SENSOR_CHANNEL_NAME)
        createNotificationChannel(MEDIA_PROJECTION_CHANNEL_ID, MEDIA_PROJECTION_CHANNEL_NAME)
    }

    private fun createNotificationChannel(
        channelId: String,
        channelName: String
    ) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for $channelName notifications" // Add a description
            // You can set other properties here, e.g.,
            // setShowBadge(false)
            // setSound(null, null)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the channel already exists to avoid redundant creation
        if (notificationManager.getNotificationChannel(channelId) == null) {
            notificationManager.createNotificationChannel(channel)
            Log.d("LocationApp", "Notification channel created: $channelId")
        } else {
            Log.d("LocationApp", "Notification channel already exists: $channelId")
        }
    }
}