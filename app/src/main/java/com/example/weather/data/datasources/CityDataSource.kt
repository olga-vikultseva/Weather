package com.example.weather.data.datasources

import com.example.weather.data.ResultWrapper
import com.example.weather.data.network.response.CityResponse

interface CityDataSource {
    suspend fun searchCity(query: String): ResultWrapper<CityResponse>
    suspend fun searchCity(latitude: Double, longitude: Double): ResultWrapper<CityResponse>
}