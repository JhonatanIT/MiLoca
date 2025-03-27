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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

            MaterialTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
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

                            RoutesDropdownMenu(routes)

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Button(onClick = {
                                    Intent(applicationContext, LocationService::class.java).apply {
                                        action = LocationService.ACTION_START
                                        applicationContext.startForegroundService(this)
                                    }

                                    Intent(applicationContext, SensorService::class.java).apply {
                                        action = SensorService.ACTION_START
                                        applicationContext.startForegroundService(this)
                                    }
                                }) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                                        Text(text = "Start")
                                    }

                                }
                                Button(onClick = {
                                    Intent(applicationContext, LocationService::class.java).apply {
                                        action = LocationService.ACTION_STOP
                                        startService(this)
                                    }

                                    Intent(applicationContext, SensorService::class.java).apply {
                                        action = SensorService.ACTION_STOP
                                        startService(this)
                                    }
                                }) {
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

                        MyMap(mapViewModel)
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

                                Button(onClick = {

                                }) {
                                    Text("Reset")
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
fun RoutesDropdownMenu(routes: List<String>) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf("No routes") }

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
                        }
                    )
                }
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MyMap(mapViewModel: MapViewModel) {
    // Set properties using MapProperties composable
    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )

    // Collect the locations from the StateFlow
    mapViewModel.loadLocationsPoints()
    val locations by mapViewModel.locationsPoints.collectAsState(initial = emptyList())

    // Marker of Lima
    val lima = remember { LatLng(-12.046374, -77.042793) }
    val limaTitle = "Lima"
    val limaSnippet = "Capital of Peru"

    // Calculate the center location (only if there are locations)
    val centerLocation = remember(locations) {
        if (locations.isNotEmpty()) {
            LatLng(
                locations.map { it.latitude }.average(),
                locations.map { it.longitude }.average()
            )
        } else {
            // Default center if no locations are available
            LatLng(lima.latitude, lima.longitude) // You can set a specific default location here
        }
    }


    // Remember camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 10f)
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
        modifier = Modifier.height(400.dp),
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
            points = locations,
            color = Color(0xFF0000FF), // Blue color
            width = 5f,
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
