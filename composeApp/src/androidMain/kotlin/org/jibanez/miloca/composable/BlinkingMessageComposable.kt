package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun BlinkingMessage(
    modifier: Modifier = Modifier,
    message: String,
    isVisible: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            var visible by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(400)
                    visible = !visible
                }
            }

            if (visible) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}