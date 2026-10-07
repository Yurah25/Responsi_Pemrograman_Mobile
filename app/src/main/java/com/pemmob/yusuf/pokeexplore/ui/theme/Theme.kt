package com.pemmob.yusuf.pokeexplore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(primary = Forest, onPrimary = Color.White,
    primaryContainer = Mint, onPrimaryContainer = Ink, background = Paper, surface = Color.White)
private val Dark = darkColorScheme(primary = Color(0xFF94D5AF),
    primaryContainer = Color(0xFF194D39), onPrimaryContainer = Mint,
    background = Color(0xFF101B16), surface = Color(0xFF18251F))

@Composable
fun PokeExploreTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = PokeTypography, content = content)
}
