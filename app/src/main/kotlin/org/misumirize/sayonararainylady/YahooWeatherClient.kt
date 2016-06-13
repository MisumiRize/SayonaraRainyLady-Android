package org.misumirize.sayonararainylady

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Observable

class YahooWeatherClient {

    companion object {
        val defaultBaseUrl = "http://weather.olp.yahooapis.jp/v1/"
        var service = buildService(defaultBaseUrl)

        fun setBaseUrl(baseUrl: String) {
            service = buildService(baseUrl)
        }

        private fun buildService(baseUrl: String): YahooWeatherService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor {
                        val req = it.request()
                        val url = req.url()
                                .newBuilder()
                                .addQueryParameter("appid", BuildConfig.YAHOO_APP_ID)
                                .addQueryParameter("output", "json")
                                .build()
                        it.proceed(req.newBuilder().url(url).build())
                    }
                    .addInterceptor(interceptor)
                    .build()

            return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()
                    .create(YahooWeatherService::class.java)
        }

        fun getWeather(latitude: Double, longitude: Double): Observable<YahooWeatherResponse> {
            return service.getWeather("$longitude,$latitude")
        }
    }

    interface YahooWeatherService {
        @GET("place")
        fun getWeather(@Query("coordinates") coordinates: String): Observable<YahooWeatherResponse>
    }
}

