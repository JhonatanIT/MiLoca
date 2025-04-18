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
import org.jibanez.miloca.entity.Location
import org.jibanez.miloca.entity.OrientationType
import org.jibanez.miloca.entity.RealtimeData


class FirebaseRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val lightRef: DatabaseReference = database.child("light")
    private val accelerationRef: DatabaseReference = database.child("acceleration")
    private val flatRef: DatabaseReference = database.child("flat")
    private val orientationRef: DatabaseReference = database.child("orientation")
    private val locationRef: DatabaseReference = database.child("location")
    private val isSafeRef: DatabaseReference = database.child("isSafe")
    private val timestampRef: DatabaseReference = database.child("timestamp")

    fun uploadLightData(light: LightType) {
        lightRef.setValue(light).addOnSuccessListener {
            Log.d("Firebase", "light: $it")
            uploadTimestampData(System.currentTimeMillis())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    fun uploadAccelerationData(acceleration: AccelerationType) {
        accelerationRef.setValue(acceleration).addOnSuccessListener {
            Log.d("Firebase", "acceleration: $it")
            uploadTimestampData(System.currentTimeMillis())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    fun uploadFlatData(flat: FlatType) {
        flatRef.setValue(flat).addOnSuccessListener {
            Log.d("Firebase", "flat: $it")
            uploadTimestampData(System.currentTimeMillis())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    fun uploadOrientationData(orientation: OrientationType) {
        orientationRef.setValue(orientation).addOnSuccessListener {
            Log.d("Firebase", "orientation: $it")
            uploadTimestampData(System.currentTimeMillis())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    fun uploadLocationData(location: Location) {
        locationRef.setValue(location).addOnSuccessListener {
            Log.d("Firebase", "location: $it")
            uploadTimestampData(System.currentTimeMillis())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    fun uploadIsSafeData(isSafe: Boolean) {
        isSafeRef.setValue(isSafe).addOnSuccessListener {
            Log.d("Firebase", "isSafe: $it")
            uploadTimestampData(System.currentTimeMillis())
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    private fun uploadTimestampData(timestamp: Long) {
        timestampRef.setValue(timestamp).addOnSuccessListener {
            Log.d("Firebase", "timestamp: $it")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error writing value", e)
        }
    }

    //TODO use methods to get data
    fun observeRealtimeData() = callbackFlow {
        val currentData = RealtimeData()

        val lightListener = lightRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<LightType>()?.let { trySend(currentData.copy(light = it)) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        val accelerationListener =
            accelerationRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<AccelerationType>()
                        ?.let { trySend(currentData.copy(acceleration = it)) }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        val flatListener = flatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<FlatType>()?.let { trySend(currentData.copy(flat = it)) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        val orientationListener = orientationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<OrientationType>()
                    ?.let { trySend(currentData.copy(orientation = it)) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        val locationListener = locationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<Location>()?.let { trySend(currentData.copy(location = it)) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose {
            lightRef.removeEventListener(lightListener)
            accelerationRef.removeEventListener(accelerationListener)
            flatRef.removeEventListener(flatListener)
            orientationRef.removeEventListener(orientationListener)
            locationRef.removeEventListener(locationListener)
        }
    }
}