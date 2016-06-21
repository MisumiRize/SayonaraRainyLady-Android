package org.misumirize.sayonararainylady

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class YahooWeatherClientTest {

    var server: MockWebServer? = null

    @Before
    fun injectMockWebServer() {
        server = MockWebServer()
        server!!.start()
        YahooWeatherClient.setBaseUrl(server!!.url("/").toString())
    }

    @After
    fun releaseMockWebServer() {
        server!!.shutdown()
    }

    @Test
    fun testDetectingRainfallFound() {
        server!!.enqueue(
                MockResponse().setResponseCode(200).setBody("""
{
    "ResultInfo": {
        "Count": 1,
        "Total": 1,
        "Start": 1,
        "Status": 200,
        "Latency": 0.004519,
        "Description": "",
        "Copyright": "(C) Yahoo Japan Corporation."
    },
    "Feature": [
        {
            "Id": "201606082110_139.73229_35.663613",
            "Name": "地点(139.73229,35.663613)の2016年06月08日 21時10分から60分間の天気情報",
            "Geometry": {
                "Type": "point",
                "Coordinates": "139.73229,35.663613"
            },
            "Property": {
                "WeatherAreaCode": 4410,
                "WeatherList": {
                    "Weather": [
                        {
                            "Type": "observation",
                            "Date": "201606082110",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082120",
                            "Rainfall": 0.25
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082130",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082140",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082150",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082200",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082210",
                            "Rainfall": 0
                        }
                    ]
                }
            }
        }
    ]
}
                """)
        )
        val s = TestSubscriber<YahooWeatherResponse>()
        YahooWeatherProvider.fetchFromYahooWeather(
                Observable.from(
                        arrayOf(YahooWeatherRequest(35.663613, 139.732293))
                )
        ).toBlocking().subscribe(s)
        s.assertNoErrors()
        Assert.assertEquals(s.onNextEvents.size, 1)
    }

    @Test
    fun testDetectingRainfallNotFound() {
        server!!.enqueue(
                MockResponse().setResponseCode(200).setBody("""
{
    "ResultInfo": {
        "Count": 1,
        "Total": 1,
        "Start": 1,
        "Status": 200,
        "Latency": 0.004519,
        "Description": "",
        "Copyright": "(C) Yahoo Japan Corporation."
    },
    "Feature": [
        {
            "Id": "201606082110_139.73229_35.663613",
            "Name": "地点(139.73229,35.663613)の2016年06月08日 21時10分から60分間の天気情報",
            "Geometry": {
                "Type": "point",
                "Coordinates": "139.73229,35.663613"
            },
            "Property": {
                "WeatherAreaCode": 4410,
                "WeatherList": {
                    "Weather": [
                        {
                            "Type": "observation",
                            "Date": "201606082110",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082120",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082130",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082140",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082150",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082200",
                            "Rainfall": 0
                        },
                        {
                            "Type": "forecast",
                            "Date": "201606082210",
                            "Rainfall": 0
                        }
                    ]
                }
            }
        }
    ]
}
                """)
        )
        val s = TestSubscriber<YahooWeatherResponse>()
        YahooWeatherProvider.fetchFromYahooWeather(
                Observable.from(
                        arrayOf(YahooWeatherRequest(35.663613, 139.732293))
                )
        ).toBlocking().subscribe(s)
        s.assertNoErrors()
        Assert.assertEquals(s.onNextEvents.size, 0)
    }

    @Test
    fun testFilteringInvalidFeature() {
        server!!.enqueue(
                MockResponse().setResponseCode(200).setBody("""
{
    "ResultInfo": {
        "Count": 1,
        "Total": 1,
        "Start": 1,
        "Status": 200,
        "Latency": 0.004519,
        "Description": "",
        "Copyright": "(C) Yahoo Japan Corporation."
    },
    "Feature": [
    ]
}
                """)
        )
        val s = TestSubscriber<YahooWeatherResponse>()
        YahooWeatherProvider.fetchFromYahooWeather(
                Observable.from(
                        arrayOf(YahooWeatherRequest(35.663613, 139.732293))
                )
        ).toBlocking().subscribe(s)
        s.assertNoErrors()
        Assert.assertEquals(s.onNextEvents.size, 0)
    }
}

