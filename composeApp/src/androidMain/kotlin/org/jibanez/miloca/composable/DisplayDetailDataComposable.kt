package org.jibanez.miloca.composable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.jibanez.miloca.entity.SensorType
import org.jibanez.miloca.viewmodel.LocationViewModel

@Composable
fun DisplayDetailDataComposable(
    currentLocation: State<String?>, isRecording: Boolean, distanceToRoute: String
) {
    var light by remember { mutableStateOf<String?>(null) }
    var acceleration by remember { mutableStateOf<String?>(null) }
    var flat by remember { mutableStateOf<String?>(null) }
    var orientation by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    DisposableEffect(Unit) {
        val lightReceiver = createReceiver(SensorType.LIGHT.value) {
            light = it
        }
        val accelerationReceiver = createReceiver(SensorType.ACCELERATION.value) {
            acceleration = it
        }
        val flatReceiver = createReceiver(SensorType.FLAT.value) {
            flat = it
        }
        val orientationReceiver = createReceiver(SensorType.ORIENTATION.value) {
            orientation = it
        }

        //TODO use RECEIVER_NOT_EXPORTED for better security
        ContextCompat.registerReceiver(
            context,
            lightReceiver,
            IntentFilter(SensorType.LIGHT.value),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            context,
            accelerationReceiver,
            IntentFilter(SensorType.ACCELERATION.value),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            context,
            flatReceiver,
            IntentFilter(SensorType.FLAT.value),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            context,
            orientationReceiver,
            IntentFilter(SensorType.ORIENTATION.value),
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(lightReceiver)
            context.unregisterReceiver(accelerationReceiver)
            context.unregisterReceiver(flatReceiver)
            context.unregisterReceiver(orientationReceiver)
        }
    }

    if (isRecording) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Left column - Location info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), horizontalAlignment = Alignment.Start
            ) {
                currentLocation.value?.let { location ->
                    if (location != LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE) {
                        val locationSplit = location.split(",")
                        val latitude = locationSplit[0]
                        val longitude = locationSplit[1]
                        val altitude = locationSplit[2]

                        Row(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = "Lat: ",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = latitude)
                        }
                        Row(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = "Long: ",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = longitude)
                        }
                        Row(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = "Height: ",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "%.2f m.".format(altitude.toFloat()))
                        }
                    }
                }
            }

            // Right column - Sensor info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), horizontalAlignment = Alignment.Start
            ) {
                listOf(
                    light, acceleration, flat, orientation
                ).forEach { sensor ->
                    sensor?.let { text ->
                        val (label, value) = text.split(": ", limit = 2)
                        Row(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = "$label: ",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = value)
                        }
                    }
                }
            }
        }
    } else {
        if (distanceToRoute.isNotEmpty()) {
            Row(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = "Distance: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = distanceToRoute,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

private fun createReceiver(action: String, onReceive: (String?) -> Unit): BroadcastReceiver {
    return object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == action) {
                val value = intent.getStringExtra("value")
                onReceive(value)
            }
        }
    }
}
