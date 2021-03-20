package com.example.weather.ui.citysearch.model

sealed class CitySearchUIState {
    object Empty : CitySearchUIState()
    object Loading : CitySearchUIState()
    object NoMatches : CitySearchUIState()
    data class Data(val cityList: List<CityItem>) : CitySearchUIState()
    data class Error(val errorMessage: String) : CitySearchUIState()
}