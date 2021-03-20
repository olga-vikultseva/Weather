package com.example.weather.di.weather.future.detail

import com.example.weather.data.db.FutureWeatherDao
import com.example.weather.domain.weather.future.detail.DetailedWeatherInteractor
import com.example.weather.domain.weather.future.detail.DetailedWeatherInteractorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object DetailedWeatherModule {

    @Provides
    @FragmentScoped
    fun provideDetailedWeatherInteractor(
        futureWeatherDao: FutureWeatherDao
    ): DetailedWeatherInteractor = DetailedWeatherInteractorImpl(futureWeatherDao)
}
