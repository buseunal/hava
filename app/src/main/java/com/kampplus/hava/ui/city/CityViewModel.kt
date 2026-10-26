package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.data.TurkishProvinces
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Success(TurkishProvinces.all))
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // Kullanıcı her harf yazdığında çağrılır: yerel liste anında güncellenir.
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        val local = TurkishProvinces.filter(newQuery)
        _uiState.value = if (local.isEmpty()) LoadState.Loading else LoadState.Success(local)
        searchJob?.cancel()
        if (newQuery.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                delay(DEBOUNCE_MS) // her harfte istek atmamak için bekle
                remoteSearch(local)
            }
        } else if (local.isEmpty()) {
            _uiState.value = LoadState.Empty
        }
    }

    // Klavyedeki "Ara" ya da hata ekranındaki "Tekrar dene".
    fun search() {
        searchJob?.cancel()
        val local = TurkishProvinces.filter(_query.value)
        if (_query.value.isBlank()) {
            _uiState.value = LoadState.Success(local)
            return
        }
        searchJob = viewModelScope.launch {
            if (local.isEmpty()) _uiState.value = LoadState.Loading
            remoteSearch(local)
        }
    }

    private suspend fun remoteSearch(local: List<City>) {
        val q = _query.value
        try {
            val remote = repository.search(q).filterNot { r ->
                // 81 il zaten listede: aynı ili ikinci kez gösterme
                r.countryCode == "TR" && local.any { it.name.equals(r.name, ignoreCase = true) }
            }
            val merged = local + remote
            _uiState.value = if (merged.isEmpty()) LoadState.Empty else LoadState.Success(merged)
        } catch (e: IOException) {
            Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
            _uiState.value = fallback(local, "İnternet bağlantını kontrol et.")
        } catch (e: HttpException) {
            Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
            _uiState.value = fallback(local, "Sunucu şu an cevap vermiyor.")
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
            _uiState.value = fallback(local, "Bir şeyler ters gitti. Tekrar dene.")
        }
    }

    // İnternet yoksa bile yerel eşleşmeler varsa onları göstermeye devam et.
    private fun fallback(local: List<City>, message: String): LoadState<List<City>> =
        if (local.isNotEmpty()) LoadState.Success(local) else LoadState.Error(message)

    private companion object {
        const val TAG = "CityViewModel"
        const val DEBOUNCE_MS = 500L
    }
}
