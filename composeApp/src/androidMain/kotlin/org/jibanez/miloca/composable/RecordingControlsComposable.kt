package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordingControls(
    isRecording: Boolean,
    isLocationEnabled: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    //TODO add follow button
    //TODO when start a route dont draw the previous route selected
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Button(
            onClick = onStartClick,
            enabled = !isRecording && isLocationEnabled
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                Text(text = "Start")
            }
        }
        Button(
            onClick = onStopClick,
            enabled = isRecording && isLocationEnabled
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Close, contentDescription = "Stop")
                Text(text = "Stop")
            }
        }
    }
}