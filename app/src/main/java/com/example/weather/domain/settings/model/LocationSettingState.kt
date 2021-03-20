package com.example.weather.domain.settings.model

import com.example.weather.data.ErrorType
import com.example.weather.data.WeatherLocation

sealed class LocationSettingState {
    object Loading: LocationSettingState()
    data class Data(val location: WeatherLocation, val isClickable: Boolean): LocationSettingState()
    data class Error(val error: ErrorType): LocationSettingState()
}