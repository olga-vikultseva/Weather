package com.example.weather.domain.weather.future.detail

import com.example.weather.data.db.entity.future.FutureWeatherEntry

interface DetailedWeatherInteractor {
    suspend fun getDetailedWeather(dayOfWeek: Int): FutureWeatherEntry
}
