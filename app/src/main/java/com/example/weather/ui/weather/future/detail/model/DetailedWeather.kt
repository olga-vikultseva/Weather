package com.example.weather.ui.weather.future.detail.model

data class DetailedWeather(
    val cityName: String,
    val date: String,
    val weatherDescription: String,
    val temperatureMorning: String,
    val temperatureDay: String,
    val temperatureEvening: String,
    val temperatureNight: String,
    val feelsLikeTemperature: String,
    val wind: String,
    val pressure: String,
    val humidity: String,
    val uvIndex: String,
    val weatherIconUrl: String
)