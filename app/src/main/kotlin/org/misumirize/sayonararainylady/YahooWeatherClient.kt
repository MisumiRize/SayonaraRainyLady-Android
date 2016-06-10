package org.misumirize.sayonararainylady

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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
            val client = OkHttpClient()
            client.interceptors().add(Interceptor {
                val req = it.request()
                val url = req.url()
                        .newBuilder()
                        .addQueryParameter("appid", BuildConfig.YAHOO_APP_ID)
                        .addQueryParameter("output", "json")
                        .build()
                it.proceed(req.newBuilder().url(url).build())
            })

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
        @GET("place")
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

