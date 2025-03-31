package org.jibanez.miloca.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

@Composable
fun RoutesDropdownMenu(
    routes: List<String>,
    onRouteSelected: (String) -> Unit = {}
) {
    val NO_ROUTES = "No routes"
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf(NO_ROUTES) }

    // Set the selected route to the first one in the list if available
    LaunchedEffect(routes) {
        if (routes.isNotEmpty()) {
            selectedRoute = routes[0]
            onRouteSelected(selectedRoute)
        }
    }

    Box(
        modifier = Modifier.width(150.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Button(
            onClick = { expandedDropdown = true },
            enabled = routes.isNotEmpty()
        ) {
            Text(if (routes.isNotEmpty()) selectedRoute else NO_ROUTES)
        }
        if (routes.isNotEmpty()) {
            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                routes.forEach { route ->
                    DropdownMenuItem(
                        text = { Text(text = route) },
                        onClick = {
                            selectedRoute = route
                            expandedDropdown = false
                            onRouteSelected(selectedRoute)
                        }
                    )
                }
            }
        }
    }
}