package org.jibanez.miloca.app.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
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

            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Use koinViewModel to get the LocationViewModel
                    val locationViewModel: LocationViewModel = koinViewModel()

                    //observeAsState() converts the imperative LiveData into a declarative State that Compose can understand.
                    val currentLocation = locationViewModel.locationData.observeAsState()

                    Button(onClick = {
                        Intent(applicationContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                            applicationContext.startForegroundService(this)
                        }
                    }) {
                        Text(text = "Start")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        Intent(applicationContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_STOP
                            startService(this)
                        }
                    }) {
                        Text(text = "Stop")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        locationViewModel.startLocationUpdates(2000L)
                    }) {
                        Text(text = "Show Location")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    currentLocation.value?.let { location ->
                        Text(text = location)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        Intent(applicationContext, SensorService::class.java).apply {
                            action = SensorService.ACTION_START
                            applicationContext.startForegroundService(this)
                        }
                    }) {
                        Text(text = "Start Sensor")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        Intent(applicationContext, SensorService::class.java).apply {
                            action = SensorService.ACTION_STOP
                            startService(this)
                        }
                    }) {
                        Text(text = "Stop Sensor")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    //TODO stylish the map appearance
                    MyMap()
                }
            }
            //TODO Use location services and notifications in Web and Desktop apps
            //App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(applicationContext, LocationService::class.java))
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MyMap(mapViewModel: MapViewModel = koinViewModel()) {
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
            compassEnabled = true
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
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
