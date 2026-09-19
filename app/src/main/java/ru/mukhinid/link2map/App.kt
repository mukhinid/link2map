package ru.mukhinid.link2map

import android.graphics.drawable.Drawable

data class App(val name: String, val packageName: String, val icon: Drawable)

val SUPPORTED_MAP_PACKAGES = listOf(
    "app.organicmaps",
    "com.google.android.apps.maps",
    "ru.yandex.maps",
)

val SUPPORTED_BROWSER_PACKAGES = listOf(
    "org.mozilla.firefox", // Firefox
    "com.android.browser", // Android Browser
    "com.android.chrome", // Chrome
    "com.opera.browser", // Opera
    "com.opera.mini.native", // Opera Mini
    "com.sec.android.app.sbrowser", // Samsung Internet
    "com.microsoft.emmx", // Microsoft Edge
    "com.mi.globalbrowser", // Mi Browser
    "com.yandex.browser", // Yandex
    "com.brave.browser", // Brave Browser
    "com.vivaldi.browser", // Vivaldi

    "com.chrome.beta", // Chrome Beta
    "com.chrome.dev", // Chrome Dev
    "com.chrome.canary", // Chrome Canary
)
