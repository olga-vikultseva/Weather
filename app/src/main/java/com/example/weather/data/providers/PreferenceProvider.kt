package com.example.weather.data.providers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

abstract class PreferenceProvider(private val context: Context) {

    protected val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
}