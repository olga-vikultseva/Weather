package com.example.weather.di

import android.content.Context
import com.example.weather.R
import com.example.weather.data.ErrorType
import com.example.weather.data.db.CurrentWeatherDao
import com.example.weather.data.db.FutureWeatherDao
import com.example.weather.data.db.WeatherDatabase
import com.example.weather.data.network.*
import com.example.weather.data.providers.*
import com.example.weather.data.repositories.WeatherRepository
import com.example.weather.data.repositories.WeatherRepositoryImpl
import com.example.weather.internal.ErrorFormatter
import com.example.weather.internal.MetricFormatter
import com.example.weather.internal.StringProvider
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase =
        WeatherDatabase.buildDatabase(context)

    @Provides
    @Singleton
    fun provideCurrentWeatherDao(weatherDatabase: WeatherDatabase): CurrentWeatherDao =
        weatherDatabase.currentWeatherDao()

    @Provides
    @Singleton
    fun provideFutureWeatherDao(weatherDatabase: WeatherDatabase): FutureWeatherDao =
        weatherDatabase.futureWeatherDao()

    @Provides
    @Singleton
    fun provideWeatherApiService(connectivityInterceptor: ConnectivityInterceptor): WeatherApiService =
        WeatherApiService(connectivityInterceptor)

    @Provides
    @Singleton
    fun provideWeatherNetworkDataSource(weatherApiService: WeatherApiService): WeatherNetworkDataSource =
        WeatherNetworkDataSourceImpl(weatherApiService)

    @Provides
    @Singleton
    fun provideGeocodingApiService(connectivityInterceptor: ConnectivityInterceptor): GeocodingApiService =
        GeocodingApiService(connectivityInterceptor)

    @Provides
    @Singleton
    fun provideCityNetworkDataSource(geocodingApiService: GeocodingApiService): CityNetworkDataSource =
        CityNetworkDataSourceImpl(geocodingApiService)

    @Provides
    @Singleton
    fun provideWeatherRepository(
        currentWeatherDao: CurrentWeatherDao,
        futureWeatherDao: FutureWeatherDao,
        weatherNetworkDataSource: WeatherNetworkDataSource,
        locationProvider: LocationProvider,
        unitProvider: UnitProvider,
        requestTimeProvider: RequestTimeProvider
    ): WeatherRepository = WeatherRepositoryImpl(
        currentWeatherDao,
        futureWeatherDao,
        weatherNetworkDataSource,
        locationProvider,
        unitProvider,
        requestTimeProvider
    )

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context,
        cityNetworkDataSource: CityNetworkDataSource
    ): LocationProvider = LocationProviderImpl(
        context,
        cityNetworkDataSource,
        LocationServices.getFusedLocationProviderClient(context)
    )

    @Provides
    @Singleton
    fun provideRequestTimeProvider(@ApplicationContext context: Context): RequestTimeProvider =
        RequestTimeProviderImpl(context)

    @Provides
    @Singleton
    fun provideUnitProvider(@ApplicationContext context: Context): UnitProvider =
        UnitProviderImpl(context)

    @Provides
    @Singleton
    fun provideStringProvider(@ApplicationContext context: Context): StringProvider =
        object : StringProvider {

            override fun getString(resId: Int): String = context.getString(resId)

            override fun getString(resId: Int, vararg formatArgs: Any): String =
                context.getString(resId, *formatArgs)
        }

    @Provides
    @Singleton
    fun provideErrorFormatter(@ApplicationContext context: Context): ErrorFormatter =
        object : ErrorFormatter {

            override fun getErrorMessage(error: ErrorType): String = context.getString(
                when (error) {
                    ErrorType.NO_INTERNET_ACCESS -> R.string.no_internet_access_error_message
                    ErrorType.UNKNOWN -> R.string.default_error_message
                }
            )
        }

    @Provides
    @Singleton
    fun provideMetricFormatter(
        @ApplicationContext context: Context,
        unitProvider: UnitProvider
    ): MetricFormatter = object : MetricFormatter {

        override fun getFormattedTemperature(temperature: Int): String {
            val tempPlaceholder =
                if (unitProvider.isMetricUnit) R.string.temp_placeholder_metric else R.string.temp_placeholder_imperial
            return context.getString(tempPlaceholder, temperature)
        }

        override fun getFormattedWindSpeed(windSpeed: Int): String {
            val windSpeedPlaceholder =
                if (unitProvider.isMetricUnit) R.string.wind_speed_placeholder_metric else R.string.wind_speed_placeholder_imperial
            return context.getString(windSpeedPlaceholder, windSpeed)
        }
    }

    @Provides
    @Singleton
    fun provideSimpleDateFormat(): SimpleDateFormat =
        SimpleDateFormat("dd MMMM", Locale.getDefault())

    @Provides
    @Singleton
    fun provideConnectivityInterceptor(@ApplicationContext context: Context): ConnectivityInterceptor =
        ConnectivityInterceptorImpl(context)
}