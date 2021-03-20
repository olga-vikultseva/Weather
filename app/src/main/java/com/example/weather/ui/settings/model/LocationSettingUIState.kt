package com.example.weather.ui.settings.model

sealed class LocationSettingUIState {
    object Loading : LocationSettingUIState()
    data class Data(val cityName: String, val isClickable: Boolean) : LocationSettingUIState()
    data class Error(val errorMessage: String) : LocationSettingUIState()
}