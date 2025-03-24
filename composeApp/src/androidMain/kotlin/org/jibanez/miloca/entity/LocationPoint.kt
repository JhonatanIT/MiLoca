package org.jibanez.miloca.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),   //TODO use LocalDateTime
    val routeId: String, // To group locations belonging to the same route/trip
    val altitude: Double? = null,
)