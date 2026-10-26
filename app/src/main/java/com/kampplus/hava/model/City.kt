package com.kampplus.hava.model

import com.google.gson.annotations.SerializedName

data class CitySearchResponse(
    val results: List<City>?
)

data class City(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,

    // JSON'da alt çizgili (country_code), Kotlin'de camelCase (countryCode).
    @SerializedName("country_code")
    val countryCode: String?,
    val country: String?,
    // Bazı kayıtlarda bölge bilgisi gelmez, soru işareti uygulamayı çökmekten korur.
    val admin1: String?
) {
    // Kartta ve tahmin ekranında gösterilen alt yazı: "Ankara, Türkiye".
    val subtitle: String
        get() = listOfNotNull(admin1, country).distinct().joinToString(", ")
}
