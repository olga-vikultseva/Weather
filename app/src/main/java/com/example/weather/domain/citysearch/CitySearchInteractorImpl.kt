package com.example.weather.domain.citysearch

import com.example.weather.data.ResultWrapper
import com.example.weather.data.WeatherLocation
import com.example.weather.data.network.CityNetworkDataSource
import com.example.weather.data.network.response.CityResponse
import com.example.weather.data.providers.LocationProvider
import com.example.weather.domain.citysearch.model.CitySearchState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.Closeable

class CitySearchInteractorImpl(
    private val cityNetworkDataSource: CityNetworkDataSource,
    private val locationProvider: LocationProvider
) : CitySearchInteractor, Closeable {

    private val _citySearchStateFlow = MutableStateFlow<CitySearchState>(CitySearchState.Empty)
    override val citySearchStateFlow = _citySearchStateFlow.asStateFlow()

    private lateinit var searchCityJob: Job

    override fun searchCity(query: String) = if (query.length >= MINIMUM_QUERY_LENGTH) {
        searchCityJob = GlobalScope.launch {
            _citySearchStateFlow.value = CitySearchState.Loading
            _citySearchStateFlow.value = cityNetworkDataSource.searchCity(query).let { cityQueryResult ->
                when(cityQueryResult) {
                    is ResultWrapper.Loading -> CitySearchState.Loading
                    is ResultWrapper.Success -> CitySearchState.SearchResult(
                        convertToWeatherLocationList(cityQueryResult.data)
                    )
                    is ResultWrapper.Error -> CitySearchState.Error(cityQueryResult.error)
                }
            }
        }
    } else {
        _citySearchStateFlow.value = CitySearchState.Empty
    }

    override fun updateLocationPreference(location: WeatherLocation) =
        locationProvider.updateLocationPreference(location)

    private fun convertToWeatherLocationList(cityResponse: CityResponse): List<WeatherLocation> {

        val weatherLocationList = mutableListOf<WeatherLocation>()

        cityResponse.results.forEach {
            WeatherLocation(
                cityName = it.formatted,
                latitude = it.geometry.lat,
                longitude = it.geometry.lng
            ).let(weatherLocationList::add)
        }
        return weatherLocationList
    }

    override fun close() {
        if (this::searchCityJob.isInitialized && searchCityJob.isActive) {
            searchCityJob.cancel()
        }
    }

    private companion object {
        const val MINIMUM_QUERY_LENGTH = 2
    }
}