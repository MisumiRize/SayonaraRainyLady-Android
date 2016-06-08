package org.misumirize.sayonararainylady

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable
import java.text.SimpleDateFormat
import java.util.*

class YahooWeatherClient {

    companion object {
        val defaultBaseUrl = "http://weather.olp.yahooapis.jp/v1/"
        var service = buildService(defaultBaseUrl)

        fun setBaseUrl(baseUrl: String) {
            service = buildService(baseUrl)
        }

        private fun buildService(baseUrl: String): YahooWeatherService {
            return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()
                    .create(YahooWeatherService::class.java)
        }

        fun getWeather(latitude: Double, longitude: Double): Observable<WeatherResponse> {
            return service.getWeather("$longitude,$latitude")
        }
    }

    interface YahooWeatherService {
        @GET("place?appid=dj0zaiZpPWdhamdRSDNONjlhbiZzPWNvbnN1bWVyc2VjcmV0Jng9N2U-&output=json")
        fun getWeather(@Query("coordinates") coordinates: String): Observable<WeatherResponse>
    }

    data class WeatherResponse (
            @SerializedName("Feature") val features: List<Feature>
    )

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

