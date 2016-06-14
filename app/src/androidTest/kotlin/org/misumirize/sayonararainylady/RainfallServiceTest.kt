package org.misumirize.sayonararainylady

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ServiceTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RainfallServiceTest {

    @Rule
    @JvmField
    val serviceRule: ServiceTestRule = ServiceTestRule()

    @Test
    fun foo() {
        val i = Intent(InstrumentationRegistry.getTargetContext(), RainfallService::class.java)
        val binder: RainfallService.LocalBinder = serviceRule.bindService(i) as RainfallService.LocalBinder
        binder.service.onRainfall()

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()

        val scroller = device.findObject(
                UiSelector().packageName("com.android.systemui")
                        .className("android.view.ViewGroup")
                        .resourceId("com.android.systemui:id/notification_stack_scroller")
        )
        Assert.assertTrue(scroller.exists())

        val title = scroller.getChild(
                UiSelector().packageName("org.misumirize.sayonararainylady")
                        .className("android.widget.TextView")
                        .resourceId("android:id/title")
        )
        Assert.assertTrue(title.exists())
    }
}
