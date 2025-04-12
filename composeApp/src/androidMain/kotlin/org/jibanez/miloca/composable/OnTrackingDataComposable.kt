package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jibanez.miloca.viewmodel.LocationViewModel

@Composable
fun OnTrackingDataComposable(
    currentLocation: State<String?>,
    lightSensor: String?,
    linearAccelerationSensor: String?,
    gravitySensor: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left column - Location info
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
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
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            listOf(
                lightSensor,
                linearAccelerationSensor,
                gravitySensor
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
}
