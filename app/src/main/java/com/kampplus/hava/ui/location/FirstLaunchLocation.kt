package com.kampplus.hava.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.kampplus.hava.data.TurkishProvinces
import com.kampplus.hava.model.City
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Uygulama ilk açıldığında bir kez konum izni ister; konum bulunursa
 * [onLocated] ile o şehrin hava/lofi ekranına geçilir. İzin verilmezse
 * hiçbir şey yapmaz, kullanıcı 81 il listesinde kalır.
 */

@Composable
fun FirstLaunchLocation(onLocated: (City) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var done by rememberSaveable { mutableStateOf(false) }

    fun locate() = scope.launch {
        findCity(context)?.let(onLocated)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locate()
    }

    LaunchedEffect(Unit) {
        if (done) return@LaunchedEffect
        done = true
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) locate() else launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}

@SuppressLint("MissingPermission")
private suspend fun findCity(context: Context): City? = withContext(Dispatchers.IO) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = lm.getProviders(true)
    // Önce hızlı yol: son bilinen konum
    var loc: Location? = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
    // Yoksa taze bir konum iste (en fazla 8 sn)
    if (loc == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val provider = when {
            LocationManager.NETWORK_PROVIDER in providers -> LocationManager.NETWORK_PROVIDER
            LocationManager.GPS_PROVIDER in providers -> LocationManager.GPS_PROVIDER
            else -> null
        }
        if (provider != null) {
            loc = withTimeoutOrNull(8000) {
                suspendCancellableCoroutine { cont ->
                    val signal = CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    lm.getCurrentLocation(provider, signal, context.mainExecutor) { cont.resume(it) }
                }
            }
        }
    }
    loc ?: return@withContext null

    // Koordinattan şehir adı (Geocoder çalışmazsa "Konumum")
    @Suppress("DEPRECATION")
    val address = runCatching {
        Geocoder(context, TurkishProvinces.TR).getFromLocation(loc.latitude, loc.longitude, 1)?.firstOrNull()
    }.getOrNull()
    val name = address?.adminArea ?: address?.locality ?: "Konumum"
    val district = address?.subAdminArea ?: address?.locality
    City(
        id = 0L,
        name = "📍 $name",
        latitude = loc.latitude,
        longitude = loc.longitude,
        countryCode = address?.countryCode,
        country = address?.countryName,
        admin1 = district
    )
}
