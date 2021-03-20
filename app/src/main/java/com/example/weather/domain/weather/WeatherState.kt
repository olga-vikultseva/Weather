package com.example.weather.domain.weather

import com.example.weather.data.ErrorType
import com.example.weather.data.WeatherLocation

sealed class WeatherState<T> {
    class Loading<T> : WeatherState<T>()
    data class Data<T>(val data: T, val weatherLocation: WeatherLocation) : WeatherState<T>()
    data class Error<T>(val error: ErrorType) : WeatherState<T>()
}