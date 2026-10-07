package com.pemmob.yusuf.pokeexplore.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.yusuf.pokeexplore.data.model.Pokemon
import com.pemmob.yusuf.pokeexplore.data.repository.PokemonRepository
import com.pemmob.yusuf.pokeexplore.ui.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class CatalogPage(val pokemon: List<Pokemon>, val total: Int, val page: Int, val pageSize: Int) {
    val pageCount: Int get() = ((total + pageSize - 1) / pageSize).coerceAtLeast(1)
}

class PokemonViewModel(
    private val repository: PokemonRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    val query = savedState.getStateFlow("query", "")
    private val _catalog = MutableStateFlow<UiState<CatalogPage>>(UiState.Loading)
    val catalog = _catalog.asStateFlow()
    private val _detail = MutableStateFlow<UiState<Pokemon>>(UiState.Loading)
    val detail = _detail.asStateFlow()
    private var catalogJob: Job? = null
    private var detailJob: Job? = null
    private var currentPage: Int = savedState["page"] ?: 0
    private var currentDetailId: Int? = null

    init { loadCatalog() }

    fun search(value: String) {
        savedState["query"] = value
        currentPage = 0
        savedState["page"] = 0
        loadCatalog(debounce = true)
    }

    fun changePage(page: Int) {
        val data = (_catalog.value as? UiState.Success)?.data ?: return
        if (page !in 0 until data.pageCount) return
        currentPage = page
        savedState["page"] = page
        loadCatalog()
    }

    fun retryCatalog() = loadCatalog()

    private fun loadCatalog(debounce: Boolean = false) {
        catalogJob?.cancel()
        _catalog.value = UiState.Loading
        val term = query.value.trim()
        val page = currentPage
        catalogJob = viewModelScope.launch {
            try {
                if (debounce) delay(350)
                val matches = repository.getIndex().filter { it.name.contains(term, ignoreCase = true) }
                val pageSize = 24
                val selected = matches.drop(page * pageSize).take(pageSize)
                val pokemon = repository.getPage(selected)
                _catalog.value = UiState.Success(CatalogPage(pokemon, matches.size, page, pageSize))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _catalog.value = UiState.Error(e.userMessage())
            }
        }
    }

    fun loadDetail(id: Int, force: Boolean = false) {
        if (!force && currentDetailId == id && (_detail.value is UiState.Success || detailJob?.isActive == true)) return
        detailJob?.cancel()
        currentDetailId = id
        _detail.value = UiState.Loading
        detailJob = viewModelScope.launch {
            try {
                _detail.value = UiState.Success(repository.getPokemon(id.toString()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detail.value = UiState.Error(e.userMessage())
            }
        }
    }
}

private fun Exception.userMessage(): String = when (this) {
    is HttpException -> if (code() == 404) "Pokémon tidak ditemukan." else "Server bermasalah (${code()}). Coba lagi."
    is IOException -> "Tidak dapat terhubung. Periksa koneksi internet, lalu coba lagi."
    else -> "Data belum dapat dimuat. Silakan coba lagi."
}
