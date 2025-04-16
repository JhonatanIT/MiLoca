package org.jibanez.miloca.app.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jibanez.miloca.App
import org.jibanez.miloca.composable.DisplayDetailDataComposable
import org.jibanez.miloca.composable.HelpMessagesComposable
import org.jibanez.miloca.composable.MyMap
import org.jibanez.miloca.composable.RecordingControls
import org.jibanez.miloca.composable.RoutesDropdownMenu
import org.jibanez.miloca.composable.formatNumber
import org.jibanez.miloca.service.location.LocationService
import org.jibanez.miloca.service.mediaProjection.ScreenRecordConfig
import org.jibanez.miloca.service.mediaProjection.ScreenRecordService
import org.jibanez.miloca.service.sensor.SensorService
import org.jibanez.miloca.viewmodel.LocationViewModel
import org.jibanez.miloca.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val mediaProjectionManager by lazy {
        getSystemService<MediaProjectionManager>()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        setContent {

            val locationViewModel: LocationViewModel = koinViewModel()
            val mapViewModel: MapViewModel = koinViewModel()

            //observeAsState() converts the imperative LiveData into a declarative State that Compose can understand.
            val currentLocation = locationViewModel.locationData.observeAsState()
            locationViewModel.startLocationUpdates(2000L)

            val routes by mapViewModel.routes.collectAsState(initial = emptyList())

            var showDialog by remember { mutableStateOf(false) }
            var newRouteName by remember { mutableStateOf("") }
            var isRecording by remember { mutableStateOf(false) }

            var routeSelected by remember { mutableStateOf("") }
            val context = LocalContext.current

            var distanceToRoute by remember { mutableStateOf("") }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Create route") },
                    text = {
                        Column {
                            TextField(
                                value = newRouteName,
                                onValueChange = { newRouteName = it },
                                label = { Text("Route name *") },
                                isError = newRouteName.isEmpty()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                isRecording = true
                                Intent(applicationContext, LocationService::class.java).apply {
                                    action = LocationService.ACTION_START
                                    putExtra("ROUTE_NAME", newRouteName)
                                    applicationContext.startForegroundService(this)
                                }

                                Intent(applicationContext, SensorService::class.java).apply {
                                    action = SensorService.ACTION_START
                                    applicationContext.startForegroundService(this)
                                }
                            },
                            enabled = newRouteName.isNotEmpty()
                        ) {
                            Text("Start")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            var lightSensor by remember { mutableStateOf<String?>(null) }
            var linearAccelerationSensor by remember { mutableStateOf<String?>(null) }
            var gravitySensor by remember { mutableStateOf<String?>(null) }

            //Screen recording
            val isServiceRunning by ScreenRecordService
                .isServiceRunning
                .collectAsStateWithLifecycle()
            var hasNotificationPermission by remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                } else mutableStateOf(true)
            }
            val screenRecordLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val intent = result.data ?: return@rememberLauncherForActivityResult
                val config = ScreenRecordConfig(
                    resultCode = result.resultCode,
                    data = intent
                )

                val serviceIntent = Intent(
                    applicationContext,
                    ScreenRecordService::class.java
                ).apply {
                    action = ScreenRecordService.START_RECORDING
                    putExtra(ScreenRecordService.KEY_RECORDING_CONFIG, config)
                }
                startForegroundService(serviceIntent)
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasNotificationPermission = isGranted
                if (hasNotificationPermission && !isServiceRunning) {
                    screenRecordLauncher.launch(
                        mediaProjectionManager.createScreenCaptureIntent()
                    )
                }
            }

            DisposableEffect(Unit) {
                val lightReceiver = createReceiver("TYPE_LIGHT") { values ->
                    lightSensor = "Light: %.2f lux".format(values?.last())
                }
                val linearAccelerationReceiver =
                    createReceiver("TYPE_LINEAR_ACCELERATION") { values ->
                        val magnitude = values?.let {
                            kotlin.math.sqrt(it[0] * it[0] + it[1] * it[1] + it[2] * it[2])
                        } ?: 0f
                        linearAccelerationSensor =
                            "Acceleration: %.2f m/s²".format(magnitude)
                    }
                val gravityReceiver = createReceiver("TYPE_GRAVITY") { values ->
                    gravitySensor = "Gravity: %.2f m/s²".format(values?.last())
                }

                //TODO use RECEIVER_NOT_EXPORTED for better security
                ContextCompat.registerReceiver(
                    context,
                    lightReceiver,
                    IntentFilter("TYPE_LIGHT"),
                    ContextCompat.RECEIVER_EXPORTED
                )
                ContextCompat.registerReceiver(
                    context,
                    linearAccelerationReceiver,
                    IntentFilter("TYPE_LINEAR_ACCELERATION"),
                    ContextCompat.RECEIVER_EXPORTED
                )
                ContextCompat.registerReceiver(
                    context,
                    gravityReceiver,
                    IntentFilter("TYPE_GRAVITY"),
                    ContextCompat.RECEIVER_EXPORTED
                )

                onDispose {
                    context.unregisterReceiver(lightReceiver)
                    context.unregisterReceiver(linearAccelerationReceiver)
                    context.unregisterReceiver(gravityReceiver)
                }
            }

            MaterialTheme {

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Route selected from the dropdown menu
                            RoutesDropdownMenu(routes, isRecording) { route ->
                                routeSelected = route
                            }

                            RecordingControls(
                                isRecording = isRecording,
                                isLocationEnabled = currentLocation.value != LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE,
                                routeSelected = routeSelected,
                                onStartClick = { showDialog = true },
                                onFollowCLick = {
                                    showDialog = true
                                }, //TODO onFollowClick feature (send alert when distance is too far)
                                onStopClick = {
                                    Intent(
                                        applicationContext,
                                        LocationService::class.java
                                    ).apply {
                                        action = LocationService.ACTION_STOP
                                        startService(this)
                                    }

                                    Intent(
                                        applicationContext,
                                        SensorService::class.java
                                    ).apply {
                                        action = SensorService.ACTION_STOP
                                        startService(this)
                                    }

                                    // Reload locations points
                                    mapViewModel.loadRouteIds()

                                    isRecording = false
                                }
                            )
                        }

                        MyMap(
                            mapViewModel,
                            currentLocation,
                            if (isRecording) newRouteName else routeSelected,
                            isRecording
                        ) { distance ->
                            distanceToRoute = formatNumber(distance)
                        }

                        HelpMessagesComposable(
                            isRecording = isRecording,
                            routes = routes,
                            currentLocation = currentLocation
                        )

                        DisplayDetailDataComposable(
                            currentLocation = currentLocation,
                            lightSensor = lightSensor,
                            linearAccelerationSensor = linearAccelerationSensor,
                            gravitySensor = gravitySensor,
                            isRecording = isRecording,
                            distanceToRoute = distanceToRoute
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isRecording && routes.isNotEmpty()) {
                                    Button(onClick = {
                                        distanceToRoute = ""
                                        mapViewModel.deleteAllLocations()
                                    }) {
                                        Text("Delete routes")
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (!hasNotificationPermission &&
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                        ) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            if (isServiceRunning) {
                                                Intent(
                                                    applicationContext,
                                                    ScreenRecordService::class.java
                                                ).also {
                                                    it.action = ScreenRecordService.STOP_RECORDING
                                                    startForegroundService(it)
                                                }
                                            } else {
                                                screenRecordLauncher.launch(
                                                    mediaProjectionManager.createScreenCaptureIntent()
                                                )
                                            }
                                        }
                                    },
                                ) {
                                    Text(
                                        text = if (isServiceRunning) {
                                            "Stop recording"
                                        } else "Start recording"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            //TODO Use location services and notifications in Web and Desktop apps
            //App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(applicationContext, LocationService::class.java))
        stopService(Intent(applicationContext, SensorService::class.java))
    }
}

private fun createReceiver(action: String, onReceive: (FloatArray?) -> Unit): BroadcastReceiver {
    return object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == action) {
                val values = intent.getFloatArrayExtra("values")
                onReceive(values)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}