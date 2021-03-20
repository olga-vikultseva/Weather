package com.example.weather.domain.weather.future.detail

import com.example.weather.data.db.FutureWeatherDao
import com.example.weather.data.db.entity.future.FutureWeatherEntry

class DetailedWeatherInteractorImpl(
    private val futureWeatherDao: FutureWeatherDao
) : DetailedWeatherInteractor {

    override suspend fun getDetailedWeather(dayOfWeek: Int): FutureWeatherEntry =
        futureWeatherDao.getWeatherForDay(dayOfWeek)
}