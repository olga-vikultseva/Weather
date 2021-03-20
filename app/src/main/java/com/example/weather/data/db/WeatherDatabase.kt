package com.example.weather.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weather.data.db.WeatherDatabase.Companion.DATABASE_VERSION
import com.example.weather.data.db.entity.current.CurrentWeatherEntry
import com.example.weather.data.db.entity.future.FutureWeatherEntry

@Database(
    entities = [CurrentWeatherEntry::class, FutureWeatherEntry::class],
    version = DATABASE_VERSION
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun currentWeatherDao(): CurrentWeatherDao
    abstract fun futureWeatherDao(): FutureWeatherDao

    companion object {
        private const val DATABASE_NAME = "weather.db"
        const val DATABASE_VERSION = 1

        fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            WeatherDatabase::class.java, DATABASE_NAME
        ).build()
    }
}