package org.misumirize.sayonararainylady

import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.TimeInterval

class YahooWeatherProviderTest {

    @Test
    fun testFilterBySpan() {
        val t = TimeInterval(
                21 * 60 * 1000,
                YahooWeatherResponse(emptyList<YahooWeatherResponse.Feature>())
        )
        val o = Observable.from(arrayOf(
                TimeInterval(
                        19 * 60 * 1000,
                        YahooWeatherResponse(emptyList<YahooWeatherResponse.Feature>())
                ),
                t
        ))
        val s = TestSubscriber<TimeInterval<YahooWeatherResponse>>()
        YahooWeatherProvider.filterBySpan(o)
                .subscribe(s)
        s.assertReceivedOnNext(listOf(t))
    }
}

