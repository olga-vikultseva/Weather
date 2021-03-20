package com.example.weather.di.weather.current

import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.UnitProvider
import com.example.weather.data.repositories.WeatherRepository
import com.example.weather.domain.weather.current.CurrentWeatherInteractor
import com.example.weather.domain.weather.current.CurrentWeatherInteractorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object CurrentWeatherModule {

    @Provides
    @ViewModelScoped
    fun provideCurrentWeatherInteractor(
        locationProvider: LocationProvider,
        weatherRepository: WeatherRepository,
        unitProvider: UnitProvider
    ): CurrentWeatherInteractor =
        CurrentWeatherInteractorImpl(locationProvider, weatherRepository, unitProvider)
}