package org.jibanez.miloca.app.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import org.jibanez.miloca.App
import org.jibanez.miloca.service.location.LocationService
import org.jibanez.miloca.service.sensor.SensorService
import org.jibanez.miloca.viewmodel.LocationViewModel
import org.jibanez.miloca.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity() : ComponentActivity() {

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

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Enter Route Name") },
                    text = {
                        TextField(
                            value = routeName,
                            onValueChange = { routeName = it },
                            label = { Text("Route Name") }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
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
                        }) {
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                onStartClick = { showDialog = true },
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
                            message = "Press start to create a new route ...",
                            isVisible = routes.isEmpty()
                        )

                        currentLocation.value?.let { location ->
                            Text(text = location)
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

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
fun RoutesDropdownMenu(
    routes: List<String>,
    onRouteSelected: (String) -> Unit = {}
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf("No routes") }

    // Set the selected route to the first one in the list if available
    LaunchedEffect(routes) {
        if (routes.isNotEmpty()) {
            selectedRoute = routes[0]
            onRouteSelected(selectedRoute)
        }
    }

    Box(
        modifier = Modifier.width(150.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Button(
            onClick = { expandedDropdown = true },
            enabled = routes.isNotEmpty()
        ) {
            Text(if (routes.isNotEmpty()) selectedRoute else "No routes")
        }
        if (routes.isNotEmpty()) {
            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                routes.forEach { route ->
                    DropdownMenuItem(
                        text = { Text(text = route) },
                        onClick = {
                            selectedRoute = route
                            expandedDropdown = false
                            onRouteSelected(selectedRoute)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingControls(
    isRecording: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Button(
            onClick = onStartClick,
            enabled = !isRecording
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                Text(text = "Start")
            }
        }
        Button(
            onClick = onStopClick,
            enabled = isRecording
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Close, contentDescription = "Stop")
                Text(text = "Stop")
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MyMap(mapViewModel: MapViewModel, currentLocation: State<String?>, routeSelected: String) {
    // Set properties using MapProperties composable
    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )

//    mapViewModel.loadLocationsPoints()
//    val locations by mapViewModel.locationsPoints.collectAsState(initial = emptyList())

    // Collect the locations from the StateFlow
    mapViewModel.loadRoutePoints(routeSelected)
    val locationsByRoute by mapViewModel.selectedRoutePoints.collectAsState(initial = emptyList())


    // Marker of Lima
    val lima = remember { LatLng(-12.046374, -77.042793) }
    val limaTitle = "Lima"
    val limaSnippet = "Capital of Peru"

    // Calculate the center location (only if there are locations)
    val centerLocation = remember(locationsByRoute) {

        // Use the average of all locations if available
        if (locationsByRoute.isNotEmpty()) {
            LatLng(
                locationsByRoute.map { it.latitude }.average(),
                locationsByRoute.map { it.longitude }.average()
            )
        } else {
            // Default center if no locations are available
            LatLng(lima.latitude, lima.longitude) // You can set a specific default location here
        }
    }

    // Remember camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 15f)
    }

    // Update camera position when current location changes
    LaunchedEffect(currentLocation.value) {
        currentLocation.value?.let { location ->
            val currentLocationSplit = location.split(",")
            val currentLatLng = LatLng(
                currentLocationSplit[0].toDouble(),
                currentLocationSplit[1].toDouble()
            )
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(currentLatLng, cameraPositionState.position.zoom)
            )
        }
    }


    // Map UI settings
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    }

    GoogleMap(
        modifier = Modifier.height(500.dp),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        Marker(
            remember { MarkerState(position = lima) },
            title = limaTitle,
            snippet = limaSnippet,
            alpha = 0.8f,
            draggable = true,
        )
        // Primary polyline connecting all locations
        Polyline(
            points = locationsByRoute,
            color = Color(0xFF0000FF), // Blue color
            width = 10f,
            clickable = true,
            jointType = com.google.android.gms.maps.model.JointType.ROUND
        )

        MapEffect(Unit) { map ->
            map.setOnPoiClickListener { poi ->
                println("POI clicked: ${poi.name}")
            }
        }
    }
}

@Composable
private fun BlinkingMessage(
    modifier: Modifier = Modifier,
    message: String,
    isVisible: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            var visible by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(400)
                    visible = !visible
                }
            }

            if (visible) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
