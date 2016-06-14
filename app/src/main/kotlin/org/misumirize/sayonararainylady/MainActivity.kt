package org.misumirize.sayonararainylady

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.PermissionChecker
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity(), ConnectionCallbacks, LocationListener {

    private var googleApiClient: GoogleApiClient? = null
    private var adapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1)
        val listView = findViewById(R.id.rainfall_list) as ListView
        listView.adapter = adapter

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

    override fun onStart() {
        super.onStart()
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleApiClient?.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
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
        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient?.connect()
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            YahooWeatherClient.getWeather(location.latitude, location.longitude)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        adapter?.clear()
                        adapter?.addAll(it.features.first().property.weather.rainfalls.map {
                            "${it.date}: ${it.value}"
                        })
                        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
                    }
        }
    }
}

