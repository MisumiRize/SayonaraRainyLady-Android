package org.misumirize.sayonararainylady

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.PermissionChecker
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationServices
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity(), ConnectionCallbacks {

    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.MODEL.startsWith("Android SDK")) {
            Log.d(this.javaClass.name, android.os.Build.MODEL)
        }

        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener {
                    Log.d(this.javaClass.name, it.errorMessage)
                }
                .addApi(LocationServices.API)
                .build()

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleApiClient?.connect()
            startService(Intent(this, RainfallService::class.java))
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient?.disconnect()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (permissions.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                googleApiClient?.connect()
                startService(Intent(this, RainfallService::class.java))
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onConnected(p0: Bundle?) {
        val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        YahooWeatherClient.getWeather(location.latitude, location.longitude)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                }
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient?.connect()
    }
}

