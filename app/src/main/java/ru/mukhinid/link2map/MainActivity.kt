package ru.mukhinid.link2map

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
import ru.mukhinid.link2map.ui.theme.Link2MapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Link2MapTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
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

    var selectedMapApp by remember { mutableStateOf("") }
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
                { packageName -> selectedMapApp = packageName },
            )
        }
    }
}
