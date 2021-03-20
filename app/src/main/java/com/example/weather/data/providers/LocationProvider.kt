package com.example.weather.data.providers

import com.example.weather.data.ResultWrapper
import com.example.weather.data.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    val useDeviceLocationFlow: Flow<Boolean>
    val locationFlow: Flow<ResultWrapper<WeatherLocation>>
    fun isUsingDeviceLocation(): Boolean
    fun updateUseDeviceLocationPreference(value: Boolean)
    fun updateLocationPreference(location: WeatherLocation)
    fun persistLocationForLastRequestCurrentWeather(location: WeatherLocation)
    fun persistLocationForLastRequestFutureWeather(location: WeatherLocation)
    fun getLocationForLastRequestCurrentWeather(): WeatherLocation?
    fun getLocationForLastRequestFutureWeather(): WeatherLocation?
    suspend fun requestLocationUpdate()
}