package com.example.weather.di.citysearch

import com.example.weather.data.datasources.CityDataSource
import com.example.weather.data.providers.LocationProvider
import com.example.weather.domain.citysearch.CitySearchInteractor
import com.example.weather.domain.citysearch.CitySearchInteractorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object CitySearchModule {

    @Provides
    @ViewModelScoped
    fun provideCitySearchInteractor(
        cityDataSource: CityDataSource,
        locationProvider: LocationProvider
    ): CitySearchInteractor = CitySearchInteractorImpl(cityDataSource, locationProvider)
}