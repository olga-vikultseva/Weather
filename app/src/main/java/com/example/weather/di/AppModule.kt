package com.example.weather.di

import android.content.Context
import com.example.weather.R
import com.example.weather.data.ErrorType
import com.example.weather.data.datasources.CityDataSource
import com.example.weather.data.datasources.CityDataSourceImpl
import com.example.weather.data.datasources.WeatherDataSource
import com.example.weather.data.datasources.WeatherDataSourceImpl
import com.example.weather.data.db.CurrentWeatherDao
import com.example.weather.data.db.FutureWeatherDao
import com.example.weather.data.db.WeatherDatabase
import com.example.weather.data.network.ConnectivityInterceptor
import com.example.weather.data.network.ConnectivityInterceptorImpl
import com.example.weather.data.network.GeocodingApiService
import com.example.weather.data.network.WeatherApiService
import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.LocationProviderImpl
import com.example.weather.data.providers.UnitSystemProvider
import com.example.weather.data.providers.UnitSystemProviderImpl
import com.example.weather.data.repositories.LastWeatherRequestParamsProvider
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
    fun provideWeatherDataSource(weatherApiService: WeatherApiService): WeatherDataSource =
        WeatherDataSourceImpl(weatherApiService)

    @Provides
    @Singleton
    fun provideGeocodingApiService(connectivityInterceptor: ConnectivityInterceptor): GeocodingApiService =
        GeocodingApiService(connectivityInterceptor)

    @Provides
    @Singleton
    fun provideCityDataSource(geocodingApiService: GeocodingApiService): CityDataSource =
        CityDataSourceImpl(geocodingApiService)

    @Provides
    @Singleton
    fun provideWeatherRepository(
        currentWeatherDao: CurrentWeatherDao,
        futureWeatherDao: FutureWeatherDao,
        weatherDataSource: WeatherDataSource,
        lastWeatherRequestParamsProvider: LastWeatherRequestParamsProvider
    ): WeatherRepository = WeatherRepositoryImpl(
        currentWeatherDao,
        futureWeatherDao,
        weatherDataSource,
        lastWeatherRequestParamsProvider
    )

    @Provides
    @Singleton
    fun provideUnitSystemProvider(@ApplicationContext context: Context): UnitSystemProvider =
        UnitSystemProviderImpl(context)

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context,
        cityDataSource: CityDataSource
    ): LocationProvider = LocationProviderImpl(
        context,
        cityDataSource,
        LocationServices.getFusedLocationProviderClient(context)
    )

    @Provides
    @Singleton
    fun provideLastWeatherRequestParamsProvider(
        @ApplicationContext context: Context
    ): LastWeatherRequestParamsProvider = LastWeatherRequestParamsProvider(context)

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
        unitSystemProvider: UnitSystemProvider
    ): MetricFormatter = object : MetricFormatter {

        override fun getFormattedTemperature(temperature: Int): String {
            val placeholder =
                if (unitSystemProvider.isMetricUnitSystem) R.string.temp_placeholder_metric else R.string.temp_placeholder_imperial
            return context.getString(placeholder, temperature)
        }

        override fun getFormattedWindSpeed(windSpeed: Int): String {
            val placeholder =
                if (unitSystemProvider.isMetricUnitSystem) R.string.wind_speed_placeholder_metric else R.string.wind_speed_placeholder_imperial
            return context.getString(placeholder, windSpeed)
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