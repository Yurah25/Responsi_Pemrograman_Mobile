package com.pemmob.yusuf.pokeexplore

import androidx.lifecycle.SavedStateHandle
import com.pemmob.yusuf.pokeexplore.data.model.*
import com.pemmob.yusuf.pokeexplore.data.repository.PokemonRepository
import com.pemmob.yusuf.pokeexplore.ui.state.UiState
import com.pemmob.yusuf.pokeexplore.ui.viewmodel.PokemonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun searchUsesWholeIndexAndHandlesEmptyResults() = runTest(dispatcher) {
        val vm = PokemonViewModel(FakeRepository(), SavedStateHandle())
        advanceUntilIdle()
        assertEquals(24, (vm.catalog.value as UiState.Success).data.pokemon.size)
        vm.search("  PIKA  ")
        advanceUntilIdle()
        assertEquals("pikachu", (vm.catalog.value as UiState.Success).data.pokemon.single().name)
        vm.search("no-such-pokemon")
        advanceUntilIdle()
        assertTrue((vm.catalog.value as UiState.Success).data.pokemon.isEmpty())
    }
    @Test fun errorCanBeRetried() = runTest(dispatcher) {
        val repo = FakeRepository().apply { fail = true }
        val vm = PokemonViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        assertTrue(vm.catalog.value is UiState.Error)
        repo.fail = false
        vm.retryCatalog()
        advanceUntilIdle()
        assertTrue(vm.catalog.value is UiState.Success)
    }
    @Test fun latestSearchWinsAndPageResets() = runTest(dispatcher) {
        val vm = PokemonViewModel(FakeRepository(), SavedStateHandle())
        advanceUntilIdle()
        vm.changePage(1)
        advanceUntilIdle()
        assertEquals(1, (vm.catalog.value as UiState.Success).data.page)
        vm.search("not-found")
        vm.search("pikachu")
        advanceUntilIdle()
        val result = (vm.catalog.value as UiState.Success).data
        assertEquals(0, result.page)
        assertEquals("pikachu", result.pokemon.single().name)
    }
    @Test fun detailErrorAndRetry() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = PokemonViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        repo.fail = true
        vm.loadDetail(25)
        advanceUntilIdle()
        assertTrue(vm.detail.value is UiState.Error)
        repo.fail = false
        vm.loadDetail(25, force = true)
        advanceUntilIdle()
        assertEquals(25, (vm.detail.value as UiState.Success).data.id)
    }
    @Test fun restoresSearchFromSavedState() = runTest(dispatcher) {
        val vm = PokemonViewModel(FakeRepository(), SavedStateHandle(mapOf("query" to "pikachu")))
        advanceUntilIdle()
        assertEquals("pikachu", vm.query.value)
        assertEquals(1, (vm.catalog.value as UiState.Success).data.total)
    }
}

private class FakeRepository : PokemonRepository {
    var fail = false
    private val entries = (1..30).map { NamedResource(if (it == 25) "pikachu" else "pokemon-$it", it.toString()) }
    override suspend fun getIndex(): List<NamedResource> {
        if (fail) throw IOException("offline")
        return entries
    }
    override suspend fun getPokemon(nameOrId: String): Pokemon {
        if (fail) throw IOException("offline")
        val entry = entries.first { it.name == nameOrId || it.url == nameOrId }
        return Pokemon(entry.url.toInt(), entry.name, 4, 60, emptyList(), emptyList(), null)
    }
    override suspend fun getPage(entries: List<NamedResource>) = entries.map { getPokemon(it.name) }
}
