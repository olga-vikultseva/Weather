package com.example.weather.data.network

import com.example.weather.data.network.response.CityResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {

    @GET("geocode/v1/json?")
    suspend fun getQueryLocationList(
        @Query("q") query: String
    ): CityResponse

    companion object {

        operator fun invoke(
            connectivityInterceptor: ConnectivityInterceptor
        ): GeocodingApiService {

            val requestInterceptor = Interceptor { chain ->

                val url = chain.request()
                    .url
                    .newBuilder()
                    .addQueryParameter("key", GEOCODING_API_KEY)
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
                .baseUrl(GEOCODING_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeocodingApiService::class.java)
        }

        private const val GEOCODING_API_KEY = "b36c902a97124419960af4fcf457f634"
        private const val GEOCODING_BASE_URL = "https://api.opencagedata.com/"
    }
}