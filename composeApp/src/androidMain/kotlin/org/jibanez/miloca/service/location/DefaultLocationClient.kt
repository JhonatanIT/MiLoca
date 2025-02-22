package org.jibanez.miloca.service.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch


class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    override fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        // 1. Check for Location Permissions
        if (!context.hasLocationPermission()) {
            throw LocationClient.LocationException("Missing location permission")
        }

        // 2. Check if Network or GPS is enabled
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled && !isNetworkEnabled) {
            throw LocationClient.LocationException("GPS and Network are disabled")
        }

        // 3. Create Location Request
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval)
            .build()

        // 4. Define Location Callback
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    launch { send(location) }
                }
            }
        }

        // 5. Request Location Updates
        try{
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException){
            client.removeLocationUpdates(locationCallback)
            throw LocationClient.LocationException("Failed to request location updates: ${e.message}")
        }

        // 6. Handle Cleanup on Flow Closure
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}