package org.jibanez.miloca

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Extension function to check if the app has been granted both coarse and fine location permissions.
 *
 * @return `true` if both ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION permissions are granted, `false` otherwise.
 */
fun Context.hasLocationPermission(): Boolean {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}