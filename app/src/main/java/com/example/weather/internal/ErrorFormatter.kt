package com.example.weather.internal

import com.example.weather.data.ErrorType

interface ErrorFormatter {
    fun getErrorMessage(error: ErrorType): String
}