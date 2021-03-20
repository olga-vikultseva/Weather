package com.example.weather.internal

import java.text.SimpleDateFormat
import java.util.*

fun SimpleDateFormat.formatMillis(timestampMillis: Long): String = format(Date(timestampMillis))