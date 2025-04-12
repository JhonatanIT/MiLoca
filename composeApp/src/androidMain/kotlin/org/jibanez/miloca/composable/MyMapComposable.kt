package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import org.jibanez.miloca.viewmodel.LocationViewModel
import org.jibanez.miloca.viewmodel.MapViewModel

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MyMap(
    mapViewModel: MapViewModel,
    currentLocation: State<String?>,
    routeSelected: String,
    isRecording: Boolean
) {
    // Set properties using MapProperties composable
    val mapProperties = MapProperties(
        isMyLocationEnabled = true
    )

    // Collect the locations from the StateFlow
    mapViewModel.loadRoutePoints(routeSelected)
    val locationsByRoute by mapViewModel.selectedRoutePoints.collectAsState(initial = emptyList())

    // Marker of Lima
    val lima = remember { LatLng(-12.046374, -77.042793) }
    val limaTitle = "Lima"
    val limaSnippet = "Capital of Peru"

    // Calculate the center location (only if there are locations)
    val centerLocation = remember(locationsByRoute) {

        // Get the first location from the list
        if (locationsByRoute.isNotEmpty()) {
            LatLng(
                locationsByRoute[0].latitude,
                locationsByRoute[0].longitude
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

    if (isRecording) {
        // Update camera position when is recording
        LaunchedEffect(currentLocation.value) {
            currentLocation.value?.let { location ->

                if (LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE != location) {
                    val currentLocationSplit = location.split(",")
                    val currentLatLng = LatLng(
                        currentLocationSplit[0].toDouble(),
                        currentLocationSplit[1].toDouble()
                    )
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            cameraPositionState.position.zoom
                        )
                    )
                }
            }
        }
    } else {

        //TODO Just a visual problem when create the first route
        // Move camera to the center location when a route is selected
        LaunchedEffect(routeSelected) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    centerLocation,
                    15f
                )
            )
        }
    }
}