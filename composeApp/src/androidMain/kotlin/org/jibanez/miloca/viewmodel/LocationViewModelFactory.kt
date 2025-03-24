package org.jibanez.miloca.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.jibanez.miloca.service.location.LocationClient

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