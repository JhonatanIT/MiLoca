package org.jibanez.miloca.viewmodel

import android.app.Application
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jibanez.miloca.service.location.LocationClient

/**
 * ViewModel responsible for managing location-related data and interactions.
 *
 * This ViewModel uses a [LocationClient] to obtain location updates and exposes the formatted location
 * as a [LiveData] string. It handles starting and managing location updates within the ViewModel's
 * lifecycle.
 *
 */

class LocationViewModel(application: Application, private val locationClient: LocationClient) : AndroidViewModel(application) {

    private val _locationData = MutableLiveData<String>()
    val locationData: LiveData<String> get() = _locationData
    companion object {
        const val GPS_NETWORK_DISABLED_MESSAGE = "GPS or NETWORK disabled"
    }

    fun startLocationUpdates(interval: Long) {

        viewModelScope.launch {
            while (true) {
                if (!isNetworkAndGPSEnabled()) {
                    _locationData.postValue(GPS_NETWORK_DISABLED_MESSAGE)
                    delay(interval)
                    continue
                }

                try {
                    locationClient.getLocationUpdates(interval).collect { location ->
                        val lat = location.latitude.toString()
                        val long = location.longitude.toString()
                        val height = location.altitude.toString()
                        _locationData.postValue("$lat,$long,$height")
                    }
                } catch (e: LocationClient.LocationException) {
                    _locationData.postValue("Location error: ${e.message}")
                }
            }
        }
    }

    private fun isNetworkAndGPSEnabled(): Boolean {
        val locationManager = getSystemService(getApplication<Application>().applicationContext, LocationManager::class.java) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}