package ru.mukhinid.link2map

import android.graphics.drawable.Drawable

data class App(val name: String, val packageName: String, val icon: Drawable)

val SUPPORTED_MAP_PACKAGES = listOf(
    "app.organicmaps",
    "com.google.android.apps.maps",
    "ru.yandex.maps",
)
