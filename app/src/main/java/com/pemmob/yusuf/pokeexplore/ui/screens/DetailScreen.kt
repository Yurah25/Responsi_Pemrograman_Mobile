package com.pemmob.yusuf.pokeexplore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pemmob.yusuf.pokeexplore.data.model.Pokemon
import com.pemmob.yusuf.pokeexplore.ui.components.*
import com.pemmob.yusuf.pokeexplore.ui.state.UiState
import com.pemmob.yusuf.pokeexplore.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(state: UiState<Pokemon>, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Detail Pokémon") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") }
        })
    }) { padding ->
        when (state) {
            UiState.Loading -> LoadingPanel(Modifier.padding(padding))
            is UiState.Error -> MessagePanel("Detail belum tersedia", state.message, "Coba lagi", onRetry, Modifier.padding(padding))
            is UiState.Success -> {
                val pokemon = state.data
                Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(32.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(pokemon.id.pokemonNumber(), style = MaterialTheme.typography.titleMedium)
                            PokemonImage(pokemon, Modifier.fillMaxWidth().height(220.dp))
                            Text(pokemon.name.displayName(), style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pokemon.types.sortedBy { it.slot }.forEach { entry ->
                                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(50)) {
                                        Text(entry.type.name.displayName(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                    Text("Karakteristik", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MeasurementCard("Tinggi", pokemon.heightMeters.measurement("m"), Modifier.weight(1f))
                        MeasurementCard("Berat", pokemon.weightKilograms.measurement("kg"), Modifier.weight(1f))
                    }
                    Text("Statistik dasar", style = MaterialTheme.typography.titleLarge)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            pokemon.stats.forEach { stat ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(stat.stat.name.statLabel(), style = MaterialTheme.typography.bodyMedium)
                                        Text(stat.baseStat.toString(), style = MaterialTheme.typography.titleSmall)
                                    }
                                    LinearProgressIndicator(progress = { (stat.baseStat / 255f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(7.dp))
                                }
                            }
                            HorizontalDivider()
                            Text("Total: ${pokemon.stats.sumOf { it.baseStat }}", style = MaterialTheme.typography.titleMedium)
                            Text("Angka adalah base stat, bukan persentase. Skala visual 0–255.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("Sumber data: PokéAPI", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
