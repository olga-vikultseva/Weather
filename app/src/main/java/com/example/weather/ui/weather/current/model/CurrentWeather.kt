package com.example.weather.ui.weather.current.model

data class CurrentWeather(
    val cityName: String,
    val weatherDescription: String,
    val temperature: String,
    val feelsLikeTemperature: String,
    val wind: String,
    val pressure: String,
    val humidity: String,
    val uvIndex: String,
    val weatherIconUrl: String
)
