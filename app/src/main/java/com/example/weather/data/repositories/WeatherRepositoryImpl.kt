package com.example.weather.data.repositories

import com.example.weather.data.ResultWrapper
import com.example.weather.data.UnitSystem
import com.example.weather.data.WeatherLocation
import com.example.weather.data.db.CurrentWeatherDao
import com.example.weather.data.db.FutureWeatherDao
import com.example.weather.data.db.entity.current.CurrentWeatherEntry
import com.example.weather.data.db.entity.future.Day
import com.example.weather.data.db.entity.future.FutureWeatherEntry
import com.example.weather.data.network.WeatherNetworkDataSource
import com.example.weather.data.network.response.CurrentWeatherResponse
import com.example.weather.data.network.response.FutureWeatherResponse
import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.RequestTimeProvider
import com.example.weather.data.providers.UnitProvider
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class WeatherRepositoryImpl(
    private val currentWeatherDao: CurrentWeatherDao,
    private val futureWeatherDao: FutureWeatherDao,
    private val weatherNetworkDataSource: WeatherNetworkDataSource,
    private val locationProvider: LocationProvider,
    private val unitProvider: UnitProvider,
    private val requestTimeProvider: RequestTimeProvider
) : WeatherRepository {

    override suspend fun getCurrentWeather(
        isRefresh: Boolean,
        weatherLocation: WeatherLocation,
        unitSystem: UnitSystem
    ): ResultWrapper<CurrentWeatherEntry> =
        if (isRefresh || isFetchNewDataNeeded(
                currentLocation = weatherLocation,
                lastLocation = locationProvider.getLocationForLastRequestCurrentWeather(),
                currentUnitSystem = unitSystem,
                lastUnitSystem = unitProvider.getUnitSystemForLastRequestCurrentWeather(),
                currentTimeMillis = System.currentTimeMillis(),
                lastRequestTimeMillis = requestTimeProvider.getTimestampMillisForLastRequestCurrentWeather()
            )
        ) {
            fetchCurrentWeather(weatherLocation, unitSystem)
        } else {
            ResultWrapper.Success(currentWeatherDao.getWeather())
        }

    override suspend fun getFutureWeather(
        isRefresh: Boolean,
        weatherLocation: WeatherLocation,
        unitSystem: UnitSystem
    ): ResultWrapper<List<FutureWeatherEntry>> =
        if (isRefresh || isFetchNewDataNeeded(
                currentLocation = weatherLocation,
                lastLocation = locationProvider.getLocationForLastRequestFutureWeather(),
                currentUnitSystem = unitSystem,
                lastUnitSystem = unitProvider.getUnitSystemForLastRequestFutureWeather(),
                currentTimeMillis = System.currentTimeMillis(),
                lastRequestTimeMillis = requestTimeProvider.getTimestampMillisForLastRequestFutureWeather()
            )
        ) {
            fetchFutureWeather(weatherLocation, unitSystem)
        } else {
            ResultWrapper.Success(futureWeatherDao.getWeather())
        }

    private suspend fun fetchCurrentWeather(
        weatherLocation: WeatherLocation,
        unitSystem: UnitSystem
    ): ResultWrapper<CurrentWeatherEntry> =
        weatherNetworkDataSource.fetchCurrentWeather(
            latitude = weatherLocation.latitude,
            longitude = weatherLocation.longitude,
            unitSystem = unitSystem.name
        ).let { currentWeatherResult ->
            when (currentWeatherResult) {
                is ResultWrapper.Loading -> ResultWrapper.Loading()
                is ResultWrapper.Success -> {
                    locationProvider.persistLocationForLastRequestCurrentWeather(weatherLocation)
                    unitProvider.persistUnitSystemForLastRequestCurrentWeather(unitSystem)
                    requestTimeProvider.persistTimeForLastRequestCurrentWeather(System.currentTimeMillis())
                    val currentWeatherEntry = convertToCurrentWeatherEntry(currentWeatherResult.data)
                    persistFetchedCurrentWeather(currentWeatherEntry)
                    ResultWrapper.Success(currentWeatherEntry)
                }
                is ResultWrapper.Error -> ResultWrapper.Error(currentWeatherResult.error)
            }
        }

    private suspend fun fetchFutureWeather(
        weatherLocation: WeatherLocation,
        unitSystem: UnitSystem
    ): ResultWrapper<List<FutureWeatherEntry>> =
        weatherNetworkDataSource.fetchFutureWeather(
            latitude = weatherLocation.latitude,
            longitude = weatherLocation.longitude,
            unitSystem = unitSystem.name
        ).let { futureWeatherResult ->
            when (futureWeatherResult) {
                is ResultWrapper.Loading -> ResultWrapper.Loading()
                is ResultWrapper.Success -> {
                    locationProvider.persistLocationForLastRequestFutureWeather(weatherLocation)
                    unitProvider.persistUnitSystemForLastRequestFutureWeather(unitSystem)
                    requestTimeProvider.persistTimeForLastRequestFutureWeather(System.currentTimeMillis())
                    val futureWeatherList = convertToFutureWeatherList(futureWeatherResult.data)
                    persistFetchedFutureWeather(futureWeatherList)
                    ResultWrapper.Success(futureWeatherList)
                }
                is ResultWrapper.Error -> ResultWrapper.Error(futureWeatherResult.error)
            }
        }

    private suspend fun persistFetchedCurrentWeather(currentWeatherEntry: CurrentWeatherEntry) =
        currentWeatherDao.insertWeather(currentWeatherEntry)

    private suspend fun persistFetchedFutureWeather(futureWeatherList: List<FutureWeatherEntry>) =
        futureWeatherDao.insertWeather(futureWeatherList)

    private fun isFetchNewDataNeeded(
        currentLocation: WeatherLocation,
        lastLocation: WeatherLocation?,
        currentUnitSystem: UnitSystem,
        lastUnitSystem: UnitSystem?,
        currentTimeMillis: Long,
        lastRequestTimeMillis: Long
    ): Boolean {
        val currentTimeMin = TimeUnit.MILLISECONDS.toMinutes(currentTimeMillis)
        val lastRequestTimeMin = TimeUnit.MILLISECONDS.toMinutes(lastRequestTimeMillis)
        val diff = currentTimeMin - lastRequestTimeMin
        return !(currentLocation == lastLocation && currentUnitSystem == lastUnitSystem && diff < EXPIRE_TIME_MIN)
    }

    private fun convertToCurrentWeatherEntry(weatherResponse: CurrentWeatherResponse) =
        CurrentWeatherEntry(
            temperature = weatherResponse.current.temp.toInt(),
            feelsLikeTemperature = weatherResponse.current.feelsLike.toInt(),
            windDirection = convertToCompassDirection(weatherResponse.current.windDeg),
            windSpeed = weatherResponse.current.windSpeed.toInt(),
            pressure = weatherResponse.current.pressure,
            humidity = weatherResponse.current.humidity,
            uVIndex = weatherResponse.current.uvI.toInt(),
            weatherDescription = weatherResponse.current.weather.first().description,
            weatherIconUrl = "https://openweathermap.org/img/wn/${weatherResponse.current.weather.first().icon}@2x.png"
        )

    private fun convertToFutureWeatherList(weatherResponse: FutureWeatherResponse): List<FutureWeatherEntry> {

        val futureWeatherList = mutableListOf<FutureWeatherEntry>()

        weatherResponse.daily.forEach {

            if (futureWeatherList.size == 7) return@forEach

            val futureWeatherEntry = FutureWeatherEntry(
                dayOfWeekInt = convertToDayOfWeek(it.timestampSec),
                timestampMillis = TimeUnit.SECONDS.toMillis(it.timestampSec),
                day = Day(
                    tempMax = it.temp.max.toInt(),
                    tempMin = it.temp.min.toInt(),
                    tempMorn = it.temp.morn.toInt(),
                    tempDay = it.temp.day.toInt(),
                    tempEve = it.temp.eve.toInt(),
                    tempNight = it.temp.night.toInt(),
                    feelsLikeMorn = it.feelsLike.morn.toInt(),
                    feelsLikeDay = it.feelsLike.day.toInt(),
                    feelsLikeEve = it.feelsLike.eve.toInt(),
                    feelsLikeNight = it.feelsLike.night.toInt(),
                    windDirection = convertToCompassDirection(it.windDeg),
                    windSpeed = it.windSpeed.toInt(),
                    pressure = it.pressure,
                    humidity = it.humidity,
                    uVIndex = it.uvI,
                    weatherDescription = it.weather.first().description,
                    weatherIconUrl = "https://openweathermap.org/img/wn/${it.weather.first().icon}@2x.png"
                )
            )
            futureWeatherList.add(futureWeatherEntry)
        }
        return futureWeatherList
    }

    private fun convertToCompassDirection(degrees: Int): String {
        val directionRange = 360.0 / compassDirections.size
        val index = (degrees / directionRange).roundToInt() % compassDirections.size
        return compassDirections[index]
    }

    private fun convertToDayOfWeek(timestampSec: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = TimeUnit.SECONDS.toMillis(timestampSec)
        }
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    private companion object {
        const val EXPIRE_TIME_MIN = 15
        val compassDirections = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    }
}