package com.example.weather.domain.citysearch

import com.example.weather.data.WeatherLocation
import com.example.weather.domain.citysearch.model.CitySearchState
import kotlinx.coroutines.flow.Flow

interface CitySearchInteractor {
    val citySearchStateFlow: Flow<CitySearchState>
    fun searchCity(query: String)
    fun updateLocationPreference(location: WeatherLocation)
    fun close()
}