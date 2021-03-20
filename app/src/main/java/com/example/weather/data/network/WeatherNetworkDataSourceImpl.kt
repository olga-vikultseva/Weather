package com.example.weather.data.network

import com.example.weather.data.ErrorType
import com.example.weather.data.ResultWrapper
import com.example.weather.data.network.response.CurrentWeatherResponse
import com.example.weather.data.network.response.FutureWeatherResponse
import com.example.weather.internal.NoConnectivityException

class WeatherNetworkDataSourceImpl(
    private val weatherApiService: WeatherApiService
) : WeatherNetworkDataSource {

    override suspend fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double,
        unitSystem: String
    ): ResultWrapper<CurrentWeatherResponse> =
        try {
            weatherApiService.getCurrentWeather(latitude, longitude, unitSystem).let {
                ResultWrapper.Success(it)
            }
        } catch (exception: Exception) {
            ResultWrapper.Error(convertToErrorType(exception))
        }

    override suspend fun fetchFutureWeather(
        latitude: Double,
        longitude: Double,
        unitSystem: String
    ): ResultWrapper<FutureWeatherResponse> =
        try {
            weatherApiService.getFutureWeather(latitude, longitude, unitSystem).let {
                ResultWrapper.Success(it)
            }
        } catch (exception: Exception) {
            ResultWrapper.Error(convertToErrorType(exception))
        }

    private fun convertToErrorType(exception: Exception): ErrorType =
        when (exception) {
            is NoConnectivityException -> ErrorType.NO_INTERNET_ACCESS
            else -> ErrorType.UNKNOWN
        }
}