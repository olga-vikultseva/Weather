package com.example.weather.data.network

import com.example.weather.data.network.response.CurrentWeatherResponse
import com.example.weather.data.network.response.FutureWeatherResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/onecall?exclude=hourly,minutely,daily")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") unitSystem: String
    ): CurrentWeatherResponse

    @GET("data/2.5/onecall?exclude=current,hourly,minutely")
    suspend fun getFutureWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") unitSystem: String
    ): FutureWeatherResponse

    companion object{

        operator fun invoke(
            connectivityInterceptor: ConnectivityInterceptor
        ): WeatherApiService {

            val requestInterceptor = Interceptor { chain->

                val url = chain.request()
                    .url
                    .newBuilder()
                    .addQueryParameter("appid", WEATHER_API_KEY)
                    .build()

                val request = chain.request()
                    .newBuilder()
                    .url(url)
                    .build()

                return@Interceptor chain.proceed(request)
            }

            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(requestInterceptor)
                .addInterceptor(connectivityInterceptor)
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }

        private const val WEATHER_API_KEY = "2cacacb89a2c3b96d7fcd8c8c3eaed80"
        private const val WEATHER_BASE_URL = "http://api.openweathermap.org/"
    }
}