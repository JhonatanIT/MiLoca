package org.jibanez.miloca

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
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

    fun startLocationUpdates(interval: Long) {

        //Coroutine scope that is tied to the lifecycle of the ViewModel
        viewModelScope.launch {
            //For each location update, update the location data - Kotlin Flow
            locationClient.getLocationUpdates(interval).collect { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                _locationData.postValue("Location: ($lat, $long)")
            }
        }
    }
}