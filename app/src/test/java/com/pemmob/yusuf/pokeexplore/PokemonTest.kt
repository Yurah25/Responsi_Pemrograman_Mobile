package com.pemmob.yusuf.pokeexplore

import com.google.gson.Gson
import com.pemmob.yusuf.pokeexplore.data.model.Pokemon
import com.pemmob.yusuf.pokeexplore.util.*
import org.junit.Assert.*
import org.junit.Test

class PokemonTest {
    @Test fun mapsNestedApiFieldsAndConvertsUnits() {
        val json = """{"id":25,"name":"pikachu","height":4,"weight":60,"types":[{"slot":1,"type":{"name":"electric"}}],"stats":[{"base_stat":35,"stat":{"name":"hp"}}],"sprites":{"front_default":"sprite.png","other":{"official-artwork":{"front_default":"art.png"}}}}"""
        val pokemon = Gson().fromJson(json, Pokemon::class.java)
        assertEquals("art.png", pokemon.imageUrl)
        assertEquals(0.4, pokemon.heightMeters, 0.001)
        assertEquals(6.0, pokemon.weightKilograms, 0.001)
        assertEquals(35, pokemon.stats.first().baseStat)
        assertEquals("electric", pokemon.types.first().type.name)
    }
    @Test fun handlesNullArtworkAndSprites() {
        val fallback = Gson().fromJson("""{"sprites":{"front_default":"sprite.png","other":null}}""", Pokemon::class.java)
        assertEquals("sprite.png", fallback.imageUrl)
        val missing = Gson().fromJson("""{"sprites":null}""", Pokemon::class.java)
        assertNull(missing.imageUrl)
    }
    @Test fun formatsNamesAndId() {
        assertEquals("Mr Mime", "mr-mime".displayName())
        assertEquals("#0025", 25.pokemonNumber())
        assertEquals("#10001", 10001.pokemonNumber())
    }
}
