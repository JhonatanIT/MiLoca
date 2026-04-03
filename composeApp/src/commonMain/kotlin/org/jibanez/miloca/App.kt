package org.jibanez.miloca

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock // UPDATED: Clock is now part of the Kotlin Standard Library
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.painterResource

import miloca.composeapp.generated.resources.Res
import miloca.composeapp.generated.resources.compose_multiplatform

@Composable
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val greeting = remember { Greeting().greet() }
        val date = remember { todayDate() }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Today's date is $date",
                modifier = Modifier.padding(20.dp),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // IMPROVEMENT: Added a meaningful contentDescription for accessibility
                    Image(
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "Compose Multiplatform Logo"
                    )
                    Text("Compose: $greeting")
                }
            }
        }
    }
}

fun todayDate(): String {
    return Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
}