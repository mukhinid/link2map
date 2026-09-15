package ru.mukhinid.link2map

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val selectedMapValue = viewModel.selectedMap.collectAsStateWithLifecycle(initialValue = null).value
    val selectedMap = selectedMapValue?.let {
        installedMapApps.firstOrNull { it.packageName == selectedMapValue }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                "Selected map app",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = { showPicker = true })
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedMap != null) {
                    Image(
                        painter = DrawablePainter(selectedMap.icon),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                }
                Text(
                    selectedMap?.name ?: "Not specified",
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
    }

    if (showPicker) {
        AppPickerSheet(
            installedMapApps,
            { showPicker = false },
            { packageName -> viewModel.updateSelectedMap(packageName) },
        )
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
