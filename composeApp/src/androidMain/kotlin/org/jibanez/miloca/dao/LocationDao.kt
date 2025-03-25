package org.jibanez.miloca.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.jibanez.miloca.entity.LocationPoint

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocation(location: LocationPoint)

    @Query("DELETE FROM location_points")
    suspend fun deleteAllLocations()

    @Query("SELECT * FROM location_points WHERE routeId = :routeId ORDER BY timestamp ASC")
    fun getLocationsByRouteId(routeId: String): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points ORDER BY timestamp ASC")
    fun getAllLocations(): Flow<List<LocationPoint>>

    @Query("SELECT DISTINCT routeId FROM location_points")
    fun getAllRouteIds(): Flow<List<String>>

    @Query("SELECT * FROM location_points ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRecordedLocation(): LocationPoint?
}