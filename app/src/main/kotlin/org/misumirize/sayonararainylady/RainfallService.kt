package org.misumirize.sayonararainylady

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import pl.charmas.android.reactivelocation.ReactiveLocationProvider
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
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
        super.onDestroy()
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient?.disconnect()
        }
        subscription?.unsubscribe()
    }

    override fun onConnected(bundle: Bundle?) {
        subscription?.unsubscribe()

        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, {})

        subscription = YahooWeatherProvider.create(
                Observable.interval(10, TimeUnit.MINUTES)
                        .flatMap { ReactiveLocationProvider(this).lastKnownLocation }
                        .map { YahooWeatherRequest(it.latitude, it.longitude) }
        )
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

