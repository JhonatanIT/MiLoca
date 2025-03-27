package org.jibanez.miloca.service.location

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jibanez.miloca.R
import org.jibanez.miloca.app.LocationApp
import org.jibanez.miloca.entity.LocationPoint
import org.jibanez.miloca.repository.DatabaseProvider
import org.jibanez.miloca.repository.LocationRepository
import org.koin.android.ext.android.inject

/**
 * Service to track location updates and display them in a notification.
 */
class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val locationClient: LocationClient by inject()
//    private var currentRouteId: String = ""
    private var routeName: String = ""


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling startService(Intent).
     *
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val routeName = intent?.getStringExtra("ROUTE_NAME") ?: "Unnamed Route"
        this.routeName = routeName

        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    /**
     * Starts location tracking and displays a notification with the location updates.
     */
    private fun start() {
        val notification = NotificationCompat.Builder(this, LocationApp.LOCATION_CHANNEL_ID)
            .setContentTitle("MiLoca")
            .setContentText("Location: ...loading")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(2000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val altitude = location.altitude.toString()

                val updatedNotification = notification.setContentText(
                    """
                       Location: ($lat, $long)
                       Altitude: $altitude m,
                    """.trimIndent()
                )
                notificationManager.notify(1, updatedNotification.build())

                // Create unique route ID for this tracking session
//                currentRouteId = UUID.randomUUID().toString()

                val locationPoint = LocationPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    routeId = routeName,
                    altitude = location.altitude,
                )

                val db = DatabaseProvider.getDatabase(applicationContext)
                val repository = LocationRepository(db.locationDao())

                repository.saveLocation(locationPoint)
            }
            .launchIn(serviceScope)

        try {
            ServiceCompat.startForeground(this,1, notification.build(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                } else {
                    0
                }
            )
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                println("App not in a valid state to start foreground service") // (e.g. started from bg)
            }
            println("Error starting foreground service: ${e.message}")
        }
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}