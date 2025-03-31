package org.jibanez.miloca.app.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import org.jibanez.miloca.App
import org.jibanez.miloca.composable.BlinkingMessage
import org.jibanez.miloca.composable.MyMap
import org.jibanez.miloca.composable.RecordingControls
import org.jibanez.miloca.composable.RoutesDropdownMenu
import org.jibanez.miloca.service.location.LocationService
import org.jibanez.miloca.service.sensor.SensorService
import org.jibanez.miloca.viewmodel.LocationViewModel
import org.jibanez.miloca.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

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
        setContent {

            val locationViewModel: LocationViewModel = koinViewModel()
            val mapViewModel: MapViewModel = koinViewModel()

            //observeAsState() converts the imperative LiveData into a declarative State that Compose can understand.
            val currentLocation = locationViewModel.locationData.observeAsState()
            locationViewModel.startLocationUpdates(2000L)

            val routes by mapViewModel.routes.collectAsState(initial = emptyList())

            var showDialog by remember { mutableStateOf(false) }
            var routeName by remember { mutableStateOf("") }
            var isRecording by remember { mutableStateOf(false) }

            var routeSelected by remember { mutableStateOf("") }
            val context = LocalContext.current


            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Create route") },
                    text = {
                        Column {
                            TextField(
                                value = routeName,
                                onValueChange = { routeName = it },
                                label = { Text("Route name *") },
                                isError = routeName.isEmpty()
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
                                    putExtra("ROUTE_NAME", routeName)
                                    applicationContext.startForegroundService(this)
                                }

                                Intent(applicationContext, SensorService::class.java).apply {
                                    action = SensorService.ACTION_START
                                    applicationContext.startForegroundService(this)
                                }
                            },
                            enabled = routeName.isNotEmpty()
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

            //TODO dont work in android 14
            var lightSensor by remember { mutableStateOf<String?>(null) }
            var linearAccelerationSensor by remember { mutableStateOf<String?>(null) }

            DisposableEffect(Unit) {
                val lightReceiver = createReceiver("TYPE_LIGHT") { values ->
                    lightSensor = "Light: ${values?.last()} lux"
                }
                val linearAccelerationReceiver =
                    createReceiver("TYPE_LINEAR_ACCELERATION") { values ->
                        val magnitude = values?.let {
                            kotlin.math.sqrt(it[0] * it[0] + it[1] * it[1] + it[2] * it[2])
                        } ?: 0f
                        linearAccelerationSensor =
                            "Linear acceleration: %.2f m/s²".format(magnitude)
                    }

                ContextCompat.registerReceiver(
                    context,
                    lightReceiver,
                    IntentFilter("TYPE_LIGHT"),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                ContextCompat.registerReceiver(
                    context,
                    linearAccelerationReceiver,
                    IntentFilter("TYPE_LINEAR_ACCELERATION"),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )

                onDispose {
                    context.unregisterReceiver(lightReceiver)
                    context.unregisterReceiver(linearAccelerationReceiver)
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
                            RoutesDropdownMenu(routes) { route ->
                                routeSelected = route
                            }

                            RecordingControls(
                                isRecording = isRecording,
                                isLocationEnabled = currentLocation.value != LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE,
                                routeSelected = routeSelected,
                                onStartClick = { showDialog = true },
                                onFollowCLick = { showDialog = true },
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

                        MyMap(mapViewModel, currentLocation, routeSelected)

                        BlinkingMessage(
                            message = "Press + to create a new route ...",
                            isVisible = routes.isEmpty()
                        )

                        currentLocation.value?.let { location ->
                            Text(text = location)
                        }

                        if (isRecording) {
                            listOf(lightSensor, linearAccelerationSensor).forEach { sensor ->
                                sensor?.let { text ->
                                    Text(text = text, modifier = Modifier.padding(16.dp))
                                }
                            }
                        }

                        // Bottom Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = {
                                    mapViewModel.deleteAllLocations()
                                }) {
                                    Text("Delete routes")
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