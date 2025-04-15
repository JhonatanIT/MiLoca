package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jibanez.miloca.viewmodel.LocationViewModel

@Composable
fun HelpMessagesComposable(
    isRecording: Boolean,
    routes: List<String>,
    currentLocation: State<String?>
) {
    if (!isRecording) {
        BlinkingMessage(
            message = "Press + to create a new route ...",
            isVisible = routes.isEmpty()
        )
    }

    currentLocation.value?.let { location ->
        if (location == LocationViewModel.GPS_NETWORK_DISABLED_MESSAGE) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = location,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}