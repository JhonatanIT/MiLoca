package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun RecordingControls(
    isRecording: Boolean,
    isLocationEnabled: Boolean,
    routeSelected: String,
    onStartClick: () -> Unit,
    onFollowCLick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    //TODO when start a route dont draw the previous route selected
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        if (!isRecording && isLocationEnabled) {
            Button(onClick = onStartClick) {
                IconWithText(Icons.Default.Add, "New")
            }

            //TODO add follow feature
            Button(onClick = onFollowCLick, enabled = routeSelected.isNotEmpty()) {
                IconWithText(Icons.Default.PlayArrow, "Follow")
            }
        } else if (isRecording && isLocationEnabled) {
            Button(onClick = onStopClick) {
                IconWithText(Icons.Default.Close, "Stop")
            }
        }
    }
}

@Composable
fun IconWithText(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text)
        Text(text = text)
    }
}