package com.example.weather.domain.weather.future.list

import com.example.weather.data.ResultWrapper
import com.example.weather.data.UnitSystem
import com.example.weather.data.WeatherLocation
import com.example.weather.data.db.entity.future.FutureWeatherEntry
import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.UnitProvider
import com.example.weather.data.repositories.WeatherRepository
import com.example.weather.domain.weather.WeatherState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Closeable

class FutureWeatherInteractorImpl(
    private val locationProvider: LocationProvider,
    private val weatherRepository: WeatherRepository,
    private val unitProvider: UnitProvider
) : FutureWeatherInteractor, Closeable {

    private val _futureWeatherStateFlow = MutableStateFlow<WeatherState<List<FutureWeatherEntry>>>(WeatherState.Loading())
    override val futureWeatherStateFlow = _futureWeatherStateFlow.asStateFlow()

    private lateinit var futureWeatherUpdatesJob: Job
    private lateinit var futureWeatherRefreshJob: Job

    init {
        subscribeToFutureWeatherUpdates()
    }

    override fun refreshFutureWeather() {
        if (!this::futureWeatherRefreshJob.isInitialized || futureWeatherRefreshJob.isCompleted) {
            futureWeatherRefreshJob = GlobalScope.launch {
                _futureWeatherStateFlow.value = WeatherState.Loading()
                locationProvider.locationFlow.first().let { locationResult ->
                    when (locationResult) {
                        is ResultWrapper.Loading -> return@launch
                        is ResultWrapper.Success -> {
                            _futureWeatherStateFlow.value = getFutureWeather(
                                isRefresh = true,
                                location = locationResult.data,
                                unitSystem = unitProvider.unitSystemFlow.first()
                            )
                        }
                        is ResultWrapper.Error -> locationProvider.requestLocationUpdate()
                    }
                }
            }
        }
    }

    private fun subscribeToFutureWeatherUpdates() {
        futureWeatherUpdatesJob = GlobalScope.launch {
            locationProvider.locationFlow.combine(unitProvider.unitSystemFlow) { locationResult, unitSystem ->
                _futureWeatherStateFlow.value = WeatherState.Loading()
                locationResult to unitSystem
            }
                .map { (locationResult, unitSystem) ->
                    when (locationResult) {
                        is ResultWrapper.Loading -> WeatherState.Loading()
                        is ResultWrapper.Success -> getFutureWeather(
                            isRefresh = false,
                            location = locationResult.data,
                            unitSystem = unitSystem
                        )
                        is ResultWrapper.Error -> WeatherState.Error(locationResult.error)
                    }
                }
                .collect {
                    _futureWeatherStateFlow.value = it
                }
        }
    }

    private suspend fun getFutureWeather(
        isRefresh: Boolean,
        location: WeatherLocation,
        unitSystem: UnitSystem
    ): WeatherState<List<FutureWeatherEntry>> =
        weatherRepository.getFutureWeather(
            isRefresh = isRefresh,
            weatherLocation = location,
            unitSystem = unitSystem
        ).let { weatherResult ->
            when (weatherResult) {
                is ResultWrapper.Loading -> WeatherState.Loading()
                is ResultWrapper.Success -> WeatherState.Data(
                    data = weatherResult.data,
                    weatherLocation = location
                )
                is ResultWrapper.Error -> WeatherState.Error(weatherResult.error)
            }
        }

    override fun close() {
        futureWeatherUpdatesJob.cancel()
        if (this::futureWeatherRefreshJob.isInitialized && futureWeatherRefreshJob.isActive) {
            futureWeatherRefreshJob.cancel()
        }
    }
}