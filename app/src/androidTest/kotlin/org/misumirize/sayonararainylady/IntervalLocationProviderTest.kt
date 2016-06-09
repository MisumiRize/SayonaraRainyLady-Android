package org.misumirize.sayonararainylady

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.*
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class IntervalLocationProviderTest {

    private var googleApiClient: GoogleApiClient? = null

    companion object {

        @JvmStatic
        @BeforeClass
        fun enableMockLocation() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.pressHome()

            val i = Intent(Intent.ACTION_MAIN)
            i.setClassName("com.android.settings", "com.android.settings.DevelopmentSettings")
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            InstrumentationRegistry.getTargetContext().startActivity(i)
            device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 5000)

            val scrollable = UiScrollable(UiSelector().resourceId("android:id/list"))
            scrollable.setAsVerticalList()
            scrollable.getChildByText(UiSelector().className(TextView::class.java.name), "Select mock location app")
                    .clickAndWaitForNewWindow()

            device.findObject(By.text("Sayonara Rainy Lady")).click()
            device.waitForIdle()
        }
    }

    @Before
    fun initializeGoogleApiClient() {
        val latch = CountDownLatch(1)
        googleApiClient = GoogleApiClient.Builder(InstrumentationRegistry.getTargetContext())
                .addConnectionCallbacks(object : ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {
                        latch.countDown()
                    }

                    override fun onConnectionSuspended(i: Int) {
                        throw UnsupportedOperationException()
                    }
                })
                .addOnConnectionFailedListener {
                    throw RuntimeException("Google API client connection failed.")
                }
                .addApi(LocationServices.API)
                .build()
        googleApiClient!!.connect()
        latch.await()
    }

    @Test
    fun setMockModeWorksWorksProperly() {
        val latch = CountDownLatch(2)
        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, {
            Assert.assertEquals(it.provider, "Test Location")
            latch.countDown()
        }, Looper.getMainLooper())

        LocationServices.FusedLocationApi.setMockMode(googleApiClient, true).setResultCallback {
            val location = Location("Test Location")

            LocationServices.FusedLocationApi.setMockLocation(googleApiClient, location).setResultCallback {
                if (it.isSuccess) {
                    latch.countDown()
                }
            }
        }

        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS))
    }
}

