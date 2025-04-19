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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jibanez.miloca.R
import org.jibanez.miloca.app.LocationApp
import org.jibanez.miloca.entity.RealtimeData
import org.jibanez.miloca.entity.SensorType
import org.jibanez.miloca.repository.FirebaseRepository
import org.koin.android.ext.android.inject


class SensorService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var sensorManager: SensorManager
    private var currentRealtimeData = RealtimeData()

    private val firebaseRepository: FirebaseRepository by inject()
    private val realtimeData: Flow<RealtimeData> by inject()

    private val notification =
        NotificationCompat.Builder(this, LocationApp.SENSOR_CHANNEL_ID).setContentTitle("Sensors")
            .setContentText("Sensors: ...loading").setSmallIcon(R.drawable.ic_launcher_background)
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
                    this@SensorService,
                    1,
                    notification.build(),
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

                //Register sensors
                registerSensor(Sensor.TYPE_LIGHT)
                registerSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                registerSensor(Sensor.TYPE_GRAVITY)

                // Observe realtime data from Firebase
                realtimeData.collect { data ->
                    currentRealtimeData = data
                    Log.d(TAG, "RealtimeData: $data")
                }

                // Keep the coroutine alive until cancellation
                awaitCancellation()

            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                    Log.e(
                        TAG, "App not in a valid state to start foreground service"
                    ) // (e.g. started from bg)
                }
                Log.e(TAG, "Error starting foreground service: ${e.message}")
            } finally {
                // Cleanup when the scope is cancelled
                sensorManager.unregisterListener(this@SensorService)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun registerSensor(sensorType: Int) {
        sensorManager.getDefaultSensor(sensorType)?.let { sensor ->
            sensorManager.registerListener(
                this@SensorService, sensor, SensorManager.SENSOR_DELAY_NORMAL
            )
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
        private const val TAG = "SensorService"
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType = event.sensor.type

        //TODO evaluate if isSafe is false
        when (sensorType) {
            Sensor.TYPE_LIGHT -> handleLightSensor(event.values[0])
            Sensor.TYPE_LINEAR_ACCELERATION -> handleAccelerationSensor(event.values)
            Sensor.TYPE_GRAVITY -> handleGravitySensor(event.values)
        }
    }

    private fun handleLightSensor(lightValue: Float) {
        val lightType = SensorDataProcessor.processLight(lightValue)
        if (currentRealtimeData.light != lightType) {
            updateSensorData(
                SensorType.LIGHT,
                lightType.value,
                { currentRealtimeData.copy(light = lightType) },
                { firebaseRepository.uploadLightData(lightType) })
        }
    }

    private fun handleAccelerationSensor(values: FloatArray) {
        val magnitude = kotlin.math.sqrt(
            values[0] * values[0] + values[1] * values[1] + values[2] * values[2]
        )
        val accelerationType = SensorDataProcessor.processAcceleration(magnitude)
        if (currentRealtimeData.acceleration != accelerationType) {
            updateSensorData(
                SensorType.ACCELERATION,
                accelerationType.value,
                { currentRealtimeData.copy(acceleration = accelerationType) },
                { firebaseRepository.uploadAccelerationData(accelerationType) })
        }
    }

    private fun handleGravitySensor(values: FloatArray) {
        val flatType = SensorDataProcessor.processFlat(values[2])
        val orientationType = SensorDataProcessor.processOrientation(values[0], values[1])

        if (currentRealtimeData.flat != flatType) {
            updateSensorData(
                SensorType.FLAT,
                flatType.value,
                { currentRealtimeData.copy(flat = flatType) },
                { firebaseRepository.uploadFlatData(flatType) })
        }
        if (currentRealtimeData.orientation != orientationType) {
            updateSensorData(
                SensorType.ORIENTATION,
                orientationType.value,
                { currentRealtimeData.copy(orientation = orientationType) },
                { firebaseRepository.uploadOrientationData(orientationType) })
        }
    }

    private fun updateSensorData(
        sensorType: SensorType,
        value: String,
        updateData: () -> RealtimeData,
        uploadData: () -> Unit
    ) {
        currentRealtimeData = updateData()
        uploadData()

        val sensorIntent = Intent(sensorType.value).apply {
            val text = "${sensorType.value}: $value"
            notification.setContentText(text)
            notificationManager.notify(1, notification.build())
            putExtra("value", text)
        }
        sendBroadcast(sensorIntent)
    }

    override fun onAccuracyChanged(sensor: Sensor, p1: Int) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor.name} - $p1")
    }
}