package ru.mukhinid.link2map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ru.mukhinid.link2map.ui.theme.Link2MapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(apps: List<App>, onDismiss: () -> Unit, onAppSelected: (String) -> Unit) {
    ModalBottomSheet(onDismiss) {
        AppPickerList(apps, onAppSelected)
    }
}

@Composable
private fun AppPickerList(apps: List<App>, onAppSelected: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(apps) { item ->
            val painter = DrawablePainter(item.icon)
            ListItem(
                modifier = Modifier.clickable(onClick = { onAppSelected(item.packageName) }),
                leadingContent = { Image(painter, null, Modifier.size(40.dp)) },
                headlineContent = { Text(item.name) },
            )
        }
    }
}

@Preview
@Composable
private fun AppPickerListPreview() {
    val icon = ContextCompat.getDrawable(
        LocalContext.current,
        R.drawable.ic_launcher_foreground,
    )!!
    Link2MapTheme {
        AppPickerList(
            listOf(
                App("foobar", "foo.bar", icon),
                App("foobaz", "foo.baz", icon)
            ),
            {},
        )
    }
}
