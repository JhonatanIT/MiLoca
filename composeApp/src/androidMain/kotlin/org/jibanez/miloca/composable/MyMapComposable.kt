package org.jibanez.miloca.composable

import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
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
    isRecording: Boolean,
    onDistanceChange: (Double) -> Unit
) {
    // Constants for default locations and UI
    val defaultLocation = LatLng(-12.046374, -77.042793) // Lima
    val defaultZoneLevel = 15f
    val limaTitle = "Lima"
    val limaSnippet = "Capital of Peru"
    val polylineWith = 5f
    val polylineShadowWith = 100f
    val polylineBlue = 0xFF0000FF
    val polylineShadowTransparency = 0x1A
    val markerAlpha = 0.8f
    val mapHeight = 500
    val tag = "MyMap"

    // Map properties
    val mapProperties = MapProperties(isMyLocationEnabled = true)

    // Load route points from ViewModel
    mapViewModel.loadRoutePoints(routeSelected)
    val routePoints by mapViewModel.selectedRoutePoints.collectAsState(initial = emptyList())

    // Calculate the center location based on route points
    val centerLocation = remember(routePoints) {
        if (routePoints.isNotEmpty()) {
            // Use the first route point as the center
            LatLng(routePoints.first().latitude, routePoints.first().longitude)
        } else {
            // Use default location if no route points are available
            defaultLocation
        }
    }

    // Remember the camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, defaultZoneLevel)
    }

    // Map UI settings
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    }

    // Parse the current location into a LatLng object
    val currentLatLng = remember(currentLocation.value) {
        currentLocation.value?.let { location ->
            if (location != LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE) {
                try {
                    val parts = location.split(",")
                    LatLng(parts[0].toDouble(), parts[1].toDouble())
                } catch (e: NumberFormatException) {
                    Log.e(tag, "Invalid location format: $location")
                    defaultLocation
                } catch (e: IndexOutOfBoundsException) {
                    Log.e(tag, "Invalid location format: $location")
                    defaultLocation
                }
            } else {
                defaultLocation
            }
        } ?: defaultLocation
    }

    // Store the clicked location
    var clickedLocation by remember { mutableStateOf<LatLng?>(null) }

    GoogleMap(
        modifier = Modifier.height(mapHeight.dp),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapLongClick = { latLng ->
            clickedLocation = latLng
        }
    ) {
        // Marker for long-pressed location
        clickedLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                snippet = "Distance: ${
                    formatNumber(
                        SphericalUtil.computeDistanceBetween(
                            currentLatLng,
                            location
                        )
                    )
                }",
                alpha = markerAlpha
            )
        }

        // Marker for Lima
        Marker(
            state = MarkerState(position = defaultLocation),
            title = limaTitle,
            snippet = limaSnippet,
            alpha = markerAlpha
        )

        // Primary polyline for the route
        Polyline(
            points = routePoints,
            color = Color(polylineBlue),
            width = polylineWith,
            jointType = JointType.ROUND
        )

        // Shadow polyline for enhanced visibility
        Polyline(
            points = routePoints,
            color = Color(polylineShadowTransparency.shl(24) + polylineBlue),
            width = polylineShadowWith,
            geodesic = true,
            jointType = JointType.ROUND
        )

        // Handle POI click events
        MapEffect(Unit) { map ->
            map.setOnPoiClickListener { poi ->
                Log.d(tag, "POI clicked: ${poi.name}")
            }
        }
    }

    // Animate camera position when recording
    if (isRecording) {
        LaunchedEffect(currentLocation.value) {
            if (currentLocation.value != LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLng,
                        cameraPositionState.position.zoom
                    )
                )
            }
        }
    } else {
        // Animate camera to route center when a route is selected
        LaunchedEffect(routeSelected) {
            if (routePoints.isNotEmpty()) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        centerLocation,
                        defaultZoneLevel
                    )
                )
            }
        }
    }

    // Calculate and emit the minimum distance to the route
    LaunchedEffect(currentLatLng, routePoints) {
        if (routePoints.isNotEmpty() && !isRecording) {
            val minDistance = routePoints.minOf { routePoint ->
                SphericalUtil.computeDistanceBetween(currentLatLng, routePoint)
            }
            onDistanceChange(minDistance)
        }
    }
}

fun formatNumber(distance: Double): String {
    var formattedDistance = distance
    var unit = " m"
    if (formattedDistance < 1) {
        formattedDistance *= 1000.0
        unit = " mm"
    } else if (formattedDistance > 1000) {
        formattedDistance /= 1000.0
        unit = " km"
    }
    return "%.2f%s".format(formattedDistance, unit)
}