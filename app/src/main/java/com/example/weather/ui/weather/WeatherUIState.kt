package com.example.weather.ui.weather

sealed class WeatherUIState<T> {
    class Loading<T> : WeatherUIState<T>()
    data class Data<T>(val data: T) : WeatherUIState<T>()
    data class Error<T>(val errorMessage: String) : WeatherUIState<T>()
}