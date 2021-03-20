package com.example.weather.data.network

import com.example.weather.data.ErrorType
import com.example.weather.data.ResultWrapper
import com.example.weather.data.network.response.CityResponse
import com.example.weather.internal.NoConnectivityException

class CityNetworkDataSourceImpl(
    private val geocodingApiService: GeocodingApiService
) : CityNetworkDataSource {

    override suspend fun searchCity(query: String): ResultWrapper<CityResponse> =
        try {
            geocodingApiService.getQueryLocationList(query).let {
                ResultWrapper.Success(it)
            }
        } catch (exception: Exception) {
            ResultWrapper.Error(convertToErrorType(exception))
        }


    override suspend fun searchCity(
        latitude: Double,
        longitude: Double
    ): ResultWrapper<CityResponse> =
        try {
            geocodingApiService.getQueryLocationList("$latitude,$longitude").let {
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
