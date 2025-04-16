package org.jibanez.miloca.service.sensor

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jibanez.miloca.R
import org.jibanez.miloca.app.LocationApp


class SensorService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var sensorManager: SensorManager

    private val notification = NotificationCompat.Builder(this, LocationApp.SENSOR_CHANNEL_ID)
        .setContentTitle("Sensors")
        .setContentText("Sensors: ...loading")
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setOngoing(true)
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }


    /**
     * Called by the system every time a client explicitly starts the service by calling startService(Intent).
     *
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    /**
     * Starts location tracking and displays a notification with the location updates.
     */
    private fun start() {

        serviceScope.launch {
            try {
                ServiceCompat.startForeground(
                    this@SensorService, 1, notification.build(),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                )

                //Light sensor
                val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                lightSensor?.let { sensor ->
                    sensorManager.registerListener(
                        this@SensorService,
                        sensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }

                //Linear acceleration sensor
                val linearAccelerationSensor: Sensor? =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)   //Will be resources optimized with TYPE_ACCELEROMETER
                linearAccelerationSensor?.let { sensor ->
                    sensorManager.registerListener(
                        this@SensorService,
                        sensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }

                val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
                gravitySensor?.let { sensor ->
                    sensorManager.registerListener(
                        this@SensorService,
                        sensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }

                // Keep the coroutine alive until cancellation
                awaitCancellation()

            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e is ForegroundServiceStartNotAllowedException
                ) {
                    println("App not in a valid state to start foreground service") // (e.g. started from bg)
                }
                println("Error starting foreground service: ${e.message}")
            } finally {
                // Cleanup when the scope is cancelled
                sensorManager.unregisterListener(this@SensorService)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun stop() {
        sensorManager.unregisterListener(this@SensorService)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this@SensorService)
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onSensorChanged(event: SensorEvent) {

        val sensorType = event.sensor.type

        val intentAction = when (sensorType) {
            Sensor.TYPE_LIGHT -> "TYPE_LIGHT"
            Sensor.TYPE_LINEAR_ACCELERATION -> "TYPE_LINEAR_ACCELERATION"
            Sensor.TYPE_GRAVITY -> "TYPE_GRAVITY"
            else -> null
        }

        intentAction?.let {
            val sensorIntent = Intent(it).apply {
                putExtra("values", event.values)
            }
            sendBroadcast(sensorIntent)
        }

        // Foreground service notification update
        if (sensorType == Sensor.TYPE_LIGHT) {

            val sensorValues = event.values.joinToString("-")
            val updatedText = when (sensorType) {
                Sensor.TYPE_LIGHT -> "Light: ${event.values.last()} lux"
//                Sensor.TYPE_LINEAR_ACCELERATION -> "Linear acceleration: $sensorValues m/s^2"
                else -> "Sensor: ${event.sensor.name} - $sensorValues"
            }

            val updatedNotification = notification.setContentText(updatedText)

            notificationManager.notify(1, updatedNotification.build())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, p1: Int) {
        println("Sensor accuracy changed: ${sensor.name} - $p1")
    }
}