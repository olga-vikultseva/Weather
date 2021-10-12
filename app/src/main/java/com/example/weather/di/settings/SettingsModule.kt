package com.example.weather.di.settings

import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.UnitSystemProvider
import com.example.weather.domain.settings.SettingsInteractor
import com.example.weather.domain.settings.SettingsInteractorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object SettingsModule {

    @Provides
    @ViewModelScoped
    fun provideSettingsInteractor(
        locationProvider: LocationProvider,
        unitSystemProvider: UnitSystemProvider
    ): SettingsInteractor = SettingsInteractorImpl(locationProvider, unitSystemProvider)
}