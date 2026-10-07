package com.pemmob.yusuf.pokeexplore.data.repository

import com.pemmob.yusuf.pokeexplore.data.model.NamedResource
import com.pemmob.yusuf.pokeexplore.data.model.Pokemon
import com.pemmob.yusuf.pokeexplore.data.remote.PokemonApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap

interface PokemonRepository {
    suspend fun getIndex(): List<NamedResource>
    suspend fun getPokemon(nameOrId: String): Pokemon
    suspend fun getPage(entries: List<NamedResource>): List<Pokemon>
}

class RemotePokemonRepository(private val api: PokemonApiService) : PokemonRepository {
    private var index: List<NamedResource>? = null
    private val indexMutex = Mutex()
    private val cache = ConcurrentHashMap<String, Pokemon>()
    private val requests = Semaphore(4)

    override suspend fun getIndex(): List<NamedResource> = indexMutex.withLock {
        index ?: run {
            val first = api.getPokemonList(limit = 1)
            api.getPokemonList(limit = first.count.coerceAtLeast(1)).results.also { index = it }
        }
    }

    override suspend fun getPokemon(nameOrId: String): Pokemon {
        cache[nameOrId]?.let { return it }
        return requests.withPermit {
            cache[nameOrId] ?: api.getPokemon(nameOrId).also {
                cache[it.name] = it
                cache[it.id.toString()] = it
            }
        }
    }

    override suspend fun getPage(entries: List<NamedResource>): List<Pokemon> = coroutineScope {
        entries.map { entry -> async { getPokemon(entry.name) } }.awaitAll()
    }
}
