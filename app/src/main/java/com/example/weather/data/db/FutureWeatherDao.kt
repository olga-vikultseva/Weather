package com.example.weather.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.data.db.entity.future.FutureWeatherEntry

@Dao
interface FutureWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(futureWeatherEntries: List<FutureWeatherEntry>)

    @Query("select * from future_weather")
    suspend fun getWeather(): List<FutureWeatherEntry>

    @Query("select * from future_weather where day_of_week_int = :dayOfWeek")
    suspend fun getWeatherForDay(dayOfWeek: Int): FutureWeatherEntry
}