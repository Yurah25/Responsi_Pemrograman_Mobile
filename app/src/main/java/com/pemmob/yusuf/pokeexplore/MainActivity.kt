package com.pemmob.yusuf.pokeexplore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.pemmob.yusuf.pokeexplore.data.remote.RetrofitClient
import com.pemmob.yusuf.pokeexplore.data.repository.RemotePokemonRepository
import com.pemmob.yusuf.pokeexplore.ui.screens.DetailScreen
import com.pemmob.yusuf.pokeexplore.ui.screens.HomeScreen
import com.pemmob.yusuf.pokeexplore.ui.theme.PokeExploreTheme
import com.pemmob.yusuf.pokeexplore.ui.viewmodel.PokemonViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContext = applicationContext
        val factory = viewModelFactory {
            initializer {
                PokemonViewModel(RemotePokemonRepository(RetrofitClient.create(appContext)), createSavedStateHandle())
            }
        }
        setContent {
            PokeExploreTheme {
                val pokemonViewModel: PokemonViewModel = viewModel(factory = factory)
                PokeExploreApp(pokemonViewModel)
            }
        }
    }
}

@Composable
private fun PokeExploreApp(viewModel: PokemonViewModel) {
    val navController = rememberNavController()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(query, catalog, viewModel::search, viewModel::retryCatalog, viewModel::changePage,
                onPokemonClick = { id ->
                    viewModel.loadDetail(id)
                    navController.navigate("detail/$id") { launchSingleTop = true }
                })
        }
        composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            LaunchedEffect(id) { viewModel.loadDetail(id) }
            DetailScreen(detail, onBack = { navController.popBackStack() },
                onRetry = { viewModel.loadDetail(id, force = true) })
        }
    }
}
