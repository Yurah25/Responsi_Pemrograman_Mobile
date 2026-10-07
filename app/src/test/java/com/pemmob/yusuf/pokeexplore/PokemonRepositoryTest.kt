package com.pemmob.yusuf.pokeexplore

import com.pemmob.yusuf.pokeexplore.data.model.*
import com.pemmob.yusuf.pokeexplore.data.remote.PokemonApiService
import com.pemmob.yusuf.pokeexplore.data.repository.RemotePokemonRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PokemonRepositoryTest {
    @Test fun cachesIndexAndDetailByNameAndId() = runTest {
        val api = FakeApi()
        val repository = RemotePokemonRepository(api)
        repository.getIndex()
        repository.getIndex()
        assertEquals(listOf(1, 8), api.limits)
        repository.getPokemon("pokemon-1")
        repository.getPokemon("1")
        assertEquals(1, api.detailCalls)
    }

    @Test fun limitsConcurrentRequestsAndPreservesOrder() = runTest {
        val api = FakeApi()
        val repository = RemotePokemonRepository(api)
        val result = repository.getPage(repository.getIndex())
        assertTrue(api.maxActive <= 4)
        assertEquals((1..8).toList(), result.map { it.id })
        assertEquals(8, api.detailCalls)
    }
}

private class FakeApi : PokemonApiService {
    val limits = mutableListOf<Int>()
    var detailCalls = 0
    var active = 0
    var maxActive = 0
    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonListResponse {
        limits.add(limit)
        return PokemonListResponse(8, (1..8).drop(offset).take(limit).map { NamedResource("pokemon-$it") })
    }
    override suspend fun getPokemon(nameOrId: String): Pokemon {
        detailCalls++
        active++
        maxActive = maxOf(maxActive, active)
        try {
            val id = nameOrId.substringAfterLast('-').toInt()
            delay((9 - id) * 10L)
            return Pokemon(id, "pokemon-$id", 1, 1, emptyList(), emptyList(), null)
        } finally {
            active--
        }
    }
}
