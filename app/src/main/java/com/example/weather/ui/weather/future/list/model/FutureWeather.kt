package com.example.weather.ui.weather.future.list.model

data class FutureWeather(
    val cityName: String,
    val data: List<FutureWeatherItem>
)

data class FutureWeatherItem(
    val date: String,
    val dayOfWeekInt: Int,
    val dayOfWeekName: String,
    val temperature: String,
    val weatherDescription: String,
    val weatherIconUrl: String
)