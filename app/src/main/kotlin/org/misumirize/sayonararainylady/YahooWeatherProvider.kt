package org.misumirize.sayonararainylady

import rx.Observable
import rx.schedulers.Schedulers
import rx.schedulers.TimeInterval

class YahooWeatherProvider {

    companion object {

        fun create(observable: Observable<YahooWeatherRequest>): Observable<TimeInterval<YahooWeatherResponse>> {
            return filterBySpan(
                    fetchFromYahooWeather(observable).timeInterval()
            )
        }

        fun fetchFromYahooWeather(observable: Observable<YahooWeatherRequest>):
                Observable<YahooWeatherResponse> {
            return observable.flatMap {
                YahooWeatherClient.getWeather(it.latitude, it.longitude)
                        .subscribeOn(Schedulers.newThread())
                        .onErrorReturn {
                            YahooWeatherResponse(emptyList())
                        }
            }.filter {
                val feature = it.features.firstOrNull()
                if (feature == null) {
                    false
                } else {
                    val rainfalls = feature.property.weather.rainfalls.sortedBy { it.toCalendar() }
                    val observation = rainfalls[0]
                    val forecast = rainfalls[1]
                    observation.value == 0f && forecast.value > 0f
                }
            }
        }

        fun filterBySpan(observable: Observable<TimeInterval<YahooWeatherResponse>>):
                Observable<TimeInterval<YahooWeatherResponse>> {
            return observable.filter { it.intervalInMilliseconds >= 20 * 60 * 1000 }
        }
    }
}
