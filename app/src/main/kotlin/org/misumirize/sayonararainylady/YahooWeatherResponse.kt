package org.misumirize.sayonararainylady

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class YahooWeatherResponse (
        @SerializedName("Feature") val features: List<Feature>
) {
    data class Feature (
            @SerializedName("Property") val property: Property
    )

    data class Property (
            @SerializedName("WeatherList") val weather: Weather
    )

    data class Weather (
            @SerializedName("Weather") val rainfalls: List<Rainfall>
    )

    data class Rainfall (
            @SerializedName("Type") val type: String,
            @SerializedName("Date") val date: String,
            @SerializedName("Rainfall") val value: Float
    ) {
        fun toCalendar(): Calendar {
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("yyyyMMddHHmm", Locale.JAPAN).parse(date)
            return calendar
        }
    }
}

