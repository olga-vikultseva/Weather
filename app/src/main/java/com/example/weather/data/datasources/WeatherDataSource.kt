package com.example.weather.data.datasources

import com.example.weather.data.ResultWrapper
import com.example.weather.data.network.response.CurrentWeatherResponse
import com.example.weather.data.network.response.FutureWeatherResponse

interface WeatherDataSource {

    suspend fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double,
        unitSystem: String
    ): ResultWrapper<CurrentWeatherResponse>

    suspend fun fetchFutureWeather(
        latitude: Double,
        longitude: Double,
        unitSystem: String
    ): ResultWrapper<FutureWeatherResponse>
}