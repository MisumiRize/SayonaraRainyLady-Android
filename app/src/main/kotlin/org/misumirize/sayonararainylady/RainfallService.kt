package org.misumirize.sayonararainylady

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.Subscriptions
import java.util.concurrent.TimeUnit

class RainfallService : Service(), ConnectionCallbacks {

    private var googleApiClient: GoogleApiClient? = null
    private var notificationManager: NotificationManager? = null
    private var subscription: Subscription? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener {
                    Log.d(this.javaClass.name, it.errorMessage)
                }
                .addApi(LocationServices.API)
                .build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        googleApiClient?.connect()
        startForeground(0, Notification())
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(this.javaClass.name, "destroy")
        super.onDestroy()
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient?.disconnect()
        }
        subscription?.unsubscribe()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.d(this.javaClass.name, "connected")
        subscription?.unsubscribe()
        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        val handler = Handler()
        val period = 30L
        val unit = TimeUnit.SECONDS
        val observable = Observable.create { observer: Observer<in Location> ->
            val listener = LocationListener {
                observer.onNext(it)
            }
            handler.post {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener)
            }
            Subscriptions.create {
                handler.post {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener)
                }
            }
        }.startWith(LocationServices.FusedLocationApi.getLastLocation(googleApiClient))
        subscription = Observable.combineLatest(observable, Observable.interval(period, unit)) { loc, i -> loc }
                .sample(period, unit)
                .flatMap { YahooWeatherClient.getWeather(it.latitude, it.longitude) }
                .detectRainfall()
                .timeInterval()
                .filter { it.intervalInMilliseconds >= 20 * 60 * 1000 }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val i = Intent(this, MainActivity::class.java)
                    val p = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
                    val notification = Notification.Builder(this)
                            .setContentTitle("foo")
                            .setContentText("bar")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(p)
                            .setAutoCancel(true)
                            .build()
                    notificationManager?.notify(0, notification)
                }
    }

    override fun onConnectionSuspended(i: Int) {
    }
}

fun Observable<YahooWeatherClient.WeatherResponse>.detectRainfall(): Observable<YahooWeatherClient.WeatherResponse> {
    return this.filter {
        val rainfalls = it.features[0].property.weather.rainfalls.sortedBy { it.toCalendar() }
        val observation = rainfalls[0]
        val forecast = rainfalls[2]
        observation.value == 0f && forecast.value > 0f
    }
}


