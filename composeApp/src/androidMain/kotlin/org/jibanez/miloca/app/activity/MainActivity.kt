package org.jibanez.miloca.app.activity

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import org.jibanez.miloca.App
import org.jibanez.miloca.LocationViewModel
import org.jibanez.miloca.service.location.DefaultLocationClient
import org.jibanez.miloca.service.location.LocationClient
import org.jibanez.miloca.service.location.LocationService
import org.jibanez.miloca.service.sensor.SensorService

class MainActivity : ComponentActivity() {

    //TODO Use dependency injection: locationClient
    //by: property delegation
    private val locationViewModel: LocationViewModel by viewModels<LocationViewModel> {
        val locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        LocationViewModelFactory(application, locationClient)
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
        setContent {

            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //observeAsState() converts the imperative LiveData into a declarative State that Compose can understand.
                    val locationData = locationViewModel.locationData.observeAsState()

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
                    locationData.value?.let { location ->
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

class LocationViewModelFactory(
    private val application: Application,
    private val locationClient: LocationClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(application, locationClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
fun MyMap() {
    // Set properties using MapProperties composable
    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )
    // Set the camera's starting position
    val singapore = LatLng(1.35, 103.87)

    val cameraPositionState = rememberCameraPositionState() {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    // set settings using MapUiSettings
    val mapUiSettings = MapUiSettings(compassEnabled = true)

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    )
}