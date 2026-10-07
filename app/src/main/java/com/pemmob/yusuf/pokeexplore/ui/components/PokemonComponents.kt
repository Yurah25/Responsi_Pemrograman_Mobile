package com.pemmob.yusuf.pokeexplore.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pemmob.yusuf.pokeexplore.R
import com.pemmob.yusuf.pokeexplore.data.model.Pokemon
import com.pemmob.yusuf.pokeexplore.util.displayName
import com.pemmob.yusuf.pokeexplore.util.pokemonNumber

@Composable
fun PokemonImage(pokemon: Pokemon, modifier: Modifier = Modifier) {
    var failed by remember(pokemon.imageUrl) { mutableStateOf(false) }
    var attempt by remember(pokemon.imageUrl) { mutableIntStateOf(0) }
    val context = LocalContext.current
    val request = remember(context, pokemon.imageUrl, attempt) {
        ImageRequest.Builder(context).data(pokemon.imageUrl).crossfade(true).build()
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        key(attempt) {
            AsyncImage(model = request, contentDescription = "Gambar ${pokemon.name.displayName()}",
                placeholder = painterResource(R.drawable.ic_pokeball),
                error = painterResource(R.drawable.ic_pokeball),
                fallback = painterResource(R.drawable.ic_pokeball),
                onError = { failed = true }, onSuccess = { failed = false },
                contentScale = ContentScale.Fit, modifier = Modifier.weight(1f).fillMaxWidth())
        }
        if (pokemon.imageUrl == null) {
            Text("Gambar belum tersedia", style = MaterialTheme.typography.labelSmall)
        } else if (failed) {
            TextButton(onClick = { failed = false; attempt++ }, contentPadding = PaddingValues(0.dp)) {
                Text("Ulangi gambar", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(pokemon.id.pokemonNumber(), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge)
            PokemonImage(pokemon, Modifier.fillMaxWidth().height(124.dp))
            Text(pokemon.name.displayName(), style = MaterialTheme.typography.titleMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(pokemon.types.sortedBy { it.slot }.joinToString(" • ") { it.type.name.displayName() },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LoadingPanel(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Memuat Pokémon…")
    }
}

@Composable
fun MessagePanel(title: String, message: String, action: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAction) { Text(action) }
    }
}
