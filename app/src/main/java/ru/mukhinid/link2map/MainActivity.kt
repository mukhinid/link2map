package ru.mukhinid.link2map

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.mukhinid.link2map.ui.theme.Link2MapTheme

val Context.dataStore by preferencesDataStore(name = "settings")
val SELECTED_MAP_KEY = stringPreferencesKey("selected_map")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Link2MapTheme {
                val vm: MainScreenViewModel = viewModel(
                    factory = MainScreenViewModel.factory(applicationContext)
                )
                MainScreen(vm)
            }
        }
    }
}

@Composable
private fun MainScreen(
    viewModel: MainScreenViewModel
) {
    val context = LocalContext.current

    val installedMapApps = remember {
        SUPPORTED_MAP_PACKAGES.mapNotNull { pkg ->
            runCatching {
                val info = context.packageManager.getApplicationInfo(pkg, 0)
                App(
                    context.packageManager.getApplicationLabel(info).toString(),
                    pkg,
                    context.packageManager.getApplicationIcon(info)
                )
            }.getOrNull()
        }
    }

    var showPicker by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        OutlinedButton(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            onClick = { showPicker = true },
        ) {
            Text(text = "Choose your map app", textAlign = TextAlign.Center)
        }

        if (showPicker) {
            AppPickerSheet(
                installedMapApps,
                { showPicker = false },
                { packageName -> viewModel.updateSelectedMap(packageName) },
            )
        }
    }
}

class MainScreenViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {
    val selectedMap: StateFlow<String> = repository.selectedMapFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "",
        )

    fun updateSelectedMap(newValue: String) {
        viewModelScope.launch {
            repository.updateSelectedMap(newValue)
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = PreferencesRepository(context.dataStore)
                    return MainScreenViewModel(repo) as T
                }
            }
    }
}

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val selectedMapFlow: Flow<String> = dataStore.data.map { pref -> pref[SELECTED_MAP_KEY] ?: "" }

    suspend fun updateSelectedMap(packageName: String) {
        dataStore.updateData {
            it.toMutablePreferences().also { pref -> pref[SELECTED_MAP_KEY] = packageName }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    Link2MapTheme {
        val vm: MainScreenViewModel = viewModel(
            factory = MainScreenViewModel.factory(LocalContext.current)
        )
        MainScreen(vm)
    }
}
