package ru.mukhinid.link2map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class LinkHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val data: Uri? = intent?.data
        if (data.toString().contains("yandex.ru/maps")) {
            val client = OkHttpClient.Builder()
                .followRedirects(false)
                .build()
            val request = Request.Builder()
                .url(intent.data.toString())
                .head()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LinkHandlerActivity", "Network error", e)
                    runOnUiThread {
                        Toast.makeText(this@LinkHandlerActivity, e.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (response.isRedirect) {
                            val location = response.header("Location")
                            if (location != null) {
                                val geo = getCoordinatesFromUri(location)
                                if (geo != null) {
                                    val mapsIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        "geo:${geo.long},${geo.lat}?q=${geo.long},${geo.lat}&z=${geo.zoom}".toUri()
                                    )
                                    mapsIntent.setPackage("app.organicmaps")
                                    mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(mapsIntent)
                                }
                            }
                        }
                    }
                }
            })
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, data)
            browserIntent.setPackage("org.mozilla.firefox")
            startActivity(browserIntent)
        }
    }

    fun getCoordinatesFromUri(uriString: String): GeoData? {
        return try {
            val uri = uriString.toUri()

            val pointValue = uri.getQueryParameter("poi[point]") ?: return null
            val parts = pointValue.split(',')

            if (parts.size == 2) {
                val longitude = parts[0].toDoubleOrNull() ?: return null
                val latitude = parts[1].toDoubleOrNull() ?: return null

                val zoom = uri.getQueryParameter("z")?.toIntOrNull() ?: 10

                GeoData(longitude, latitude, zoom)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    data class GeoData(val long: Double, val lat: Double, val zoom: Int)
}
