package com.example.weather.domain.citysearch.model

import com.example.weather.data.ErrorType
import com.example.weather.data.WeatherLocation

sealed class CitySearchState {
    object Empty : CitySearchState()
    object Loading : CitySearchState()
    data class SearchResult(val cityList: List<WeatherLocation>) : CitySearchState()
    data class Error(val error: ErrorType) : CitySearchState()
}