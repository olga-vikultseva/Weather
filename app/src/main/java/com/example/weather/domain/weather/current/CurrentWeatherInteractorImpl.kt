package com.example.weather.domain.weather.current

import com.example.weather.data.ResultWrapper
import com.example.weather.data.UnitSystem
import com.example.weather.data.WeatherLocation
import com.example.weather.data.db.entity.current.CurrentWeatherEntry
import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.UnitProvider
import com.example.weather.data.repositories.WeatherRepository
import com.example.weather.domain.weather.WeatherState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Closeable

class CurrentWeatherInteractorImpl(
    private val locationProvider: LocationProvider,
    private val weatherRepository: WeatherRepository,
    private val unitProvider: UnitProvider
) : CurrentWeatherInteractor, Closeable {

    private val _currentWeatherStateFlow = MutableStateFlow<WeatherState<CurrentWeatherEntry>>(WeatherState.Loading())
    override val currentWeatherStateFlow = _currentWeatherStateFlow.asStateFlow()

    private lateinit var currentWeatherUpdatesJob: Job
    private lateinit var currentWeatherRefreshJob: Job

    init {
        subscribeToCurrentWeatherUpdates()
    }

    override fun refreshCurrentWeather() {
        if (!this::currentWeatherRefreshJob.isInitialized || currentWeatherRefreshJob.isCompleted) {
            currentWeatherRefreshJob = GlobalScope.launch {
                _currentWeatherStateFlow.value = WeatherState.Loading()
                locationProvider.locationFlow.first().let { locationResult ->
                    when (locationResult) {
                        is ResultWrapper.Loading -> return@launch
                        is ResultWrapper.Success -> {
                            _currentWeatherStateFlow.value = getCurrentWeather(
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

    private fun subscribeToCurrentWeatherUpdates() {
        currentWeatherUpdatesJob = GlobalScope.launch {
            locationProvider.locationFlow.combine(unitProvider.unitSystemFlow) { locationResult, unitSystem ->
                _currentWeatherStateFlow.value = WeatherState.Loading()
                locationResult to unitSystem
            }
                .map { (locationResult, unitSystem) ->
                    when (locationResult) {
                        is ResultWrapper.Loading -> WeatherState.Loading()
                        is ResultWrapper.Success -> getCurrentWeather(
                            isRefresh = false,
                            location = locationResult.data,
                            unitSystem = unitSystem
                        )
                        is ResultWrapper.Error -> WeatherState.Error(locationResult.error)
                    }
                }
                .collect {
                    _currentWeatherStateFlow.value = it
                }
        }
    }

    private suspend fun getCurrentWeather(
        isRefresh: Boolean,
        location: WeatherLocation,
        unitSystem: UnitSystem
    ): WeatherState<CurrentWeatherEntry> =
        weatherRepository.getCurrentWeather(
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
        currentWeatherUpdatesJob.cancel()
        if (this::currentWeatherRefreshJob.isInitialized && currentWeatherRefreshJob.isActive) {
            currentWeatherRefreshJob.cancel()
        }
    }
}