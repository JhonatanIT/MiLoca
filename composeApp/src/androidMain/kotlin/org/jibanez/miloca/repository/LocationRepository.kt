package org.jibanez.miloca.repository

import kotlinx.coroutines.flow.Flow
import org.jibanez.miloca.dao.LocationDao
import org.jibanez.miloca.entity.LocationPoint

class LocationRepository(
    private val locationDao: LocationDao
) {
    suspend fun saveLocation(location: LocationPoint) {
        locationDao.insertLocation(location)
    }

    fun getLocationsByRouteId(routeId: String): Flow<List<LocationPoint>> {
        return locationDao.getLocationsByRouteId(routeId)
    }

    fun getAllLocations(): Flow<List<LocationPoint>> {
        return locationDao.getAllLocations()
    }

    fun getAllRouteIds(): Flow<List<String>> {
        return locationDao.getAllRouteIds()
    }

    suspend fun getLastRecordedLocation(): LocationPoint? {
        return locationDao.getLastRecordedLocation()
    }

    suspend fun deleteAllLocations() {
        locationDao.deleteAllLocations()
    }
}