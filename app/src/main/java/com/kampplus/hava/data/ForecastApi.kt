package com.kampplus.hava.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse

    companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,wind_speed_10m,is_day"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability,is_day"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
        const val FORECAST_DAYS = 7
    }
}
