package com.pemmob.yusuf.pokeexplore.data.model

import com.google.gson.annotations.SerializedName

data class PokemonListResponse(val count: Int, val results: List<NamedResource>)
data class NamedResource(val name: String, val url: String = "")
data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonType>,
    val stats: List<PokemonStat>,
    val sprites: PokemonSprites?
) {
    val imageUrl: String?
        get() = sprites?.other?.officialArtwork?.frontDefault ?: sprites?.frontDefault
    val heightMeters: Double get() = height / 10.0
    val weightKilograms: Double get() = weight / 10.0
}
data class PokemonType(val slot: Int, val type: NamedResource)
data class PokemonStat(@SerializedName("base_stat") val baseStat: Int, val stat: NamedResource)
data class PokemonSprites(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtherSprites?
)
data class OtherSprites(@SerializedName("official-artwork") val officialArtwork: Artwork?)
data class Artwork(@SerializedName("front_default") val frontDefault: String?)
