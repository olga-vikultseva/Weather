package com.example.weather.data.repositories

import com.example.weather.data.ResultWrapper
import com.example.weather.data.UnitSystem
import com.example.weather.data.WeatherLocation
import com.example.weather.data.db.entity.current.CurrentWeatherEntry
import com.example.weather.data.db.entity.future.FutureWeatherEntry

interface WeatherRepository {

    suspend fun getCurrentWeather(
        isRefresh: Boolean,
        weatherLocation: WeatherLocation,
        unitSystem: UnitSystem
    ): ResultWrapper<CurrentWeatherEntry>

    suspend fun getFutureWeather(
        isRefresh: Boolean,
        weatherLocation: WeatherLocation,
        unitSystem: UnitSystem
    ): ResultWrapper<List<FutureWeatherEntry>>
}