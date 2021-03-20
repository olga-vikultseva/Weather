package com.example.weather.di.weather.future.list

import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.UnitProvider
import com.example.weather.data.repositories.WeatherRepository
import com.example.weather.domain.weather.future.list.FutureWeatherInteractor
import com.example.weather.domain.weather.future.list.FutureWeatherInteractorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object FutureWeatherModule {

    @Provides
    @ViewModelScoped
    fun provideFutureWeatherInteractor(
        locationProvider: LocationProvider,
        weatherRepository: WeatherRepository,
        unitProvider: UnitProvider
    ): FutureWeatherInteractor =
        FutureWeatherInteractorImpl(locationProvider, weatherRepository, unitProvider)
}