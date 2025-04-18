package org.jibanez.miloca.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.jibanez.miloca.entity.AccelerationType
import org.jibanez.miloca.entity.FlatType
import org.jibanez.miloca.entity.LightType
import org.jibanez.miloca.entity.LocationData
import org.jibanez.miloca.entity.OrientationType
import org.jibanez.miloca.entity.RealtimeData


class FirebaseRepository {

    companion object {
        private const val TAG = "FirebaseRepository"
    }

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    fun uploadLightData(light: LightType) {
        val updates = hashMapOf<String, Any>(
            "light" to light, "timestamp" to System.currentTimeMillis()
        )
        database.updateChildren(updates).addOnSuccessListener {
            Log.d(TAG, "Light uploaded")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error writing values", e)
        }
    }

    fun uploadAccelerationData(acceleration: AccelerationType) {
        val updates = hashMapOf<String, Any>(
            "acceleration" to acceleration, "timestamp" to System.currentTimeMillis()
        )
        database.updateChildren(updates).addOnSuccessListener {
            Log.d(TAG, "Acceleration uploaded")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error writing values", e)
        }
    }

    fun uploadFlatData(flat: FlatType) {
        val updates = hashMapOf<String, Any>(
            "flat" to flat, "timestamp" to System.currentTimeMillis()
        )
        database.updateChildren(updates).addOnSuccessListener {
            Log.d(TAG, "Flat uploaded")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error writing values", e)
        }
    }

    fun uploadOrientationData(orientation: OrientationType) {
        val updates = hashMapOf<String, Any>(
            "orientation" to orientation, "timestamp" to System.currentTimeMillis()
        )
        database.updateChildren(updates).addOnSuccessListener {
            Log.d(TAG, "Orientation uploaded")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error writing values", e)
        }
    }

    fun uploadLocationData(location: LocationData) {
        val updates = hashMapOf<String, Any>(
            "location" to location, "timestamp" to System.currentTimeMillis()
        )
        database.updateChildren(updates).addOnSuccessListener {
            Log.d(TAG, "Location uploaded")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error writing values", e)
        }
    }

    fun uploadIsSafeData(isSafe: Boolean) {
        val updates = hashMapOf<String, Any>(
            "isSafe" to isSafe, "timestamp" to System.currentTimeMillis()
        )
        database.updateChildren(updates).addOnSuccessListener {
            Log.d(TAG, "IsSafe uploaded")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error writing values", e)
        }
    }

    fun getRealtimeData() = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val light = dataSnapshot.child("light").getValue<LightType>() ?: LightType.NORMAL
                val acceleration = dataSnapshot.child("acceleration").getValue<AccelerationType>()
                    ?: AccelerationType.STOP
                val flat = dataSnapshot.child("flat").getValue<FlatType>() ?: FlatType.TILTED
                val orientation = dataSnapshot.child("orientation").getValue<OrientationType>()
                    ?: OrientationType.PORTRAIT
                val isSafe = dataSnapshot.child("isSafe").getValue<Boolean>() ?: true
                val location =
                    dataSnapshot.child("location").getValue<LocationData>() ?: LocationData()
                val timestamp =
                    dataSnapshot.child("timestamp").getValue<Long>() ?: System.currentTimeMillis()

                val realtimeData = RealtimeData(
                    light = light,
                    acceleration = acceleration,
                    flat = flat,
                    orientation = orientation,
                    isSafe = isSafe,
                    location = location,
                    timestamp = timestamp
                )

                trySend(realtimeData)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error reading value", databaseError.toException())
            }
        }

        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }
}