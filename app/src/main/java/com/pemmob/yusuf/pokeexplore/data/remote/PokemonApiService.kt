package com.pemmob.yusuf.pokeexplore.data.remote

import com.pemmob.yusuf.pokeexplore.data.model.Pokemon
import com.pemmob.yusuf.pokeexplore.data.model.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApiService {
    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int, @Query("offset") offset: Int = 0): PokemonListResponse

    @GET("pokemon/{nameOrId}")
    suspend fun getPokemon(@Path("nameOrId") nameOrId: String): Pokemon
}
