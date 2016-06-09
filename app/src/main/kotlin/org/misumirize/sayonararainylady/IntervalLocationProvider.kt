package org.misumirize.sayonararainylady

import android.location.Location
import android.os.Looper
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import rx.Observable
import rx.Observer
import rx.subscriptions.Subscriptions
import java.util.concurrent.TimeUnit

class IntervalLocationProvider {

    companion object {

        const val PERIOD = 10L

        fun create(googleApiClient: GoogleApiClient,
                   period: Long = PERIOD,
                   unit: TimeUnit = TimeUnit.MINUTES): Observable<Location> {
            val lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)

            val locationRequest = LocationRequest.create();
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 5000
            locationRequest.fastestInterval = 3000

            val o = Observable.create { observer: Observer<in Location> ->
                val listener = LocationListener {
                    observer.onNext(it)
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        googleApiClient,
                        locationRequest,
                        listener,
                        Looper.getMainLooper()
                )
                Subscriptions.create {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener)
                }
            }

            val observable = if (lastLocation != null) {
                o.startWith(lastLocation)
            } else {
                o
            }

            return Observable.combineLatest(observable, Observable.interval(period, unit)) { loc, i -> loc }
                    .sample(period, unit)
        }
    }
}
