package com.pemmob.yusuf.pokeexplore.util

import java.util.Locale

fun String.displayName(): String = split("-").joinToString(" ") {
    it.replaceFirstChar { char -> char.titlecase(Locale.ROOT) }
}
fun Int.pokemonNumber(): String = "#" + toString().padStart(4, '0')
fun Double.measurement(unit: String): String = String.format(Locale("id", "ID"), "%.1f %s", this, unit)
fun String.statLabel(): String = when (this) {
    "hp" -> "HP"
    "attack" -> "Attack"
    "defense" -> "Defense"
    "special-attack" -> "Sp. Attack"
    "special-defense" -> "Sp. Defense"
    "speed" -> "Speed"
    else -> displayName()
}
