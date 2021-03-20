package com.example.weather.data

import kotlinx.serialization.Serializable

@Serializable
data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)