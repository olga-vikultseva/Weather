package com.example.weather.domain.weather.future.list

import com.example.weather.data.db.entity.future.FutureWeatherEntry
import com.example.weather.domain.weather.WeatherState
import kotlinx.coroutines.flow.Flow

interface FutureWeatherInteractor {
    val futureWeatherStateFlow: Flow<WeatherState<List<FutureWeatherEntry>>>
    fun refreshFutureWeather()
    fun close()
}