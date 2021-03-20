package com.example.weather.data.network

import com.example.weather.data.ResultWrapper
import com.example.weather.data.network.response.CityResponse

interface CityNetworkDataSource {
    suspend fun searchCity(query: String): ResultWrapper<CityResponse>
    suspend fun searchCity(latitude: Double, longitude: Double): ResultWrapper<CityResponse>
}