package com.example.weather.domain.weather.current

import com.example.weather.data.db.entity.current.CurrentWeatherEntry
import com.example.weather.domain.weather.WeatherState
import kotlinx.coroutines.flow.Flow

interface CurrentWeatherInteractor {
    val currentWeatherStateFlow: Flow<WeatherState<CurrentWeatherEntry>>
    suspend fun subscribeToCurrentWeatherUpdates()
    suspend fun refreshCurrentWeather()
}