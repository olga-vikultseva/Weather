package com.example.weather.data.providers

import com.example.weather.data.ResultWrapper
import com.example.weather.data.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    val isUseDeviceLocation: Boolean
    val useDeviceLocationFlow: Flow<Boolean>
    val locationFlow: Flow<ResultWrapper<WeatherLocation>>
    fun updateUseDeviceLocation(value: Boolean)
    fun updateLocation(location: WeatherLocation)
    suspend fun requestLocationUpdate()
}