package com.example.inventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Grey50,
    primaryContainer = Blue50,
    secondary = Teal500,
    background = Grey50,
    surface = Grey50,
    surfaceVariant = Grey100,
    onBackground = Grey900,
    onSurface = Grey900,
    error = Red500
)

@Composable
fun InventoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
