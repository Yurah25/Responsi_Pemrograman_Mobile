package com.pemmob.yusuf.pokeexplore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pemmob.yusuf.pokeexplore.ui.components.*
import com.pemmob.yusuf.pokeexplore.ui.state.UiState
import com.pemmob.yusuf.pokeexplore.ui.viewmodel.CatalogPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(query: String, state: UiState<CatalogPage>, onQueryChange: (String) -> Unit,
    onRetry: () -> Unit, onPageChange: (Int) -> Unit, onPokemonClick: (Int) -> Unit) {
    val gridState = rememberLazyGridState()
    val page = (state as? UiState.Success)?.data?.page
    LaunchedEffect(query, page) { if (page != null) gridState.scrollToItem(0) }
    Scaffold(topBar = {
        TopAppBar(title = {
            Column {
                Text("PokéExplore", style = MaterialTheme.typography.titleLarge)
                Text("Katalog & eksplorasi Pokémon", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(value = query, onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari nama Pokémon…") }, singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                    }
                }, shape = MaterialTheme.shapes.extraLarge)
            when (state) {
                UiState.Loading -> LoadingPanel(Modifier.weight(1f))
                is UiState.Error -> MessagePanel("Belum dapat memuat data", state.message, "Coba lagi", onRetry, Modifier.weight(1f))
                is UiState.Success -> {
                    val data = state.data
                    if (data.pokemon.isEmpty()) {
                        MessagePanel("Pokémon tidak ditemukan", "Coba nama lain, misalnya pikachu atau char.",
                            "Tampilkan semua", { onQueryChange("") }, Modifier.weight(1f))
                    } else {
                        Text("${data.total} Pokémon • Halaman ${data.page + 1}/${data.pageCount}",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), state = gridState,
                            modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(data.pokemon, key = { it.id }) { pokemon ->
                                PokemonCard(pokemon, onClick = { onPokemonClick(pokemon.id) })
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { onPageChange(data.page - 1) }, enabled = data.page > 0,
                                modifier = Modifier.weight(1f)) { Text("Sebelumnya") }
                            Button(onClick = { onPageChange(data.page + 1) }, enabled = data.page + 1 < data.pageCount,
                                modifier = Modifier.weight(1f)) { Text("Berikutnya") }
                        }
                    }
                }
            }
        }
    }
}
