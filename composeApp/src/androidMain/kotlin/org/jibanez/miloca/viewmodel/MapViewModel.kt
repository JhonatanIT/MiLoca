package org.jibanez.miloca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jibanez.miloca.repository.LocationRepository

class MapViewModel(
    private val repository: LocationRepository
) : ViewModel() {
    private val _routes = MutableStateFlow<List<String>>(emptyList())
    val routes: StateFlow<List<String>> = _routes

    private val _selectedRoutePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val selectedRoutePoints: StateFlow<List<LatLng>> = _selectedRoutePoints

    private val _locationsPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val locationsPoints: StateFlow<List<LatLng>> = _locationsPoints

    init {
        viewModelScope.launch {
            repository.getAllRouteIds().collectLatest { routeIds ->
                _routes.value = routeIds
            }
        }
    }

    fun loadRoutePoints(routeId: String) {
        viewModelScope.launch {
            repository.getLocationsByRouteId(routeId).collectLatest { points ->
                _selectedRoutePoints.value = points.map {
                    LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    fun loadLocationsPoints() {
        viewModelScope.launch {
            repository.getAllLocations().collectLatest { points ->
                _locationsPoints.value = points.map {
                    LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    fun deleteAllLocations() {
        viewModelScope.launch {
            repository.deleteAllLocations()
        }
    }
}