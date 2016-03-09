package com.realdolmen.timeregistration.service;

import android.content.Context;
import android.os.Build;

import com.realdolmen.timeregistration.BuildConfig;
import com.realdolmen.timeregistration.ui.login.LoginActivity;
import com.realdolmen.timeregistration.UITestActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by BCCAZ45 on 1/03/2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, manifest = "AndroidManifest.xml", constants = BuildConfig.class)
public class BackendServiceTest {

    private Context dummyContext1, dummyContext2;
    @Before
    public void setUp() throws Exception {
        dummyContext1 = Robolectric.setupActivity(LoginActivity.class);
        dummyContext2 = Robolectric.setupActivity(UITestActivity.class);
    }

    @Test
    public void testWith() throws Exception {
        BackendService service1 = BackendService.with(dummyContext1);
        BackendService service2 = BackendService.with(dummyContext2);

        Assert.assertNotEquals(service1, service2);
        Assert.assertEquals(service1, BackendService.with(dummyContext1));
        Assert.assertEquals(service2, BackendService.with(dummyContext2));
        Assert.assertEquals(service2, BackendService.with(dummyContext1));

    }

    @Test
    public void testLogin() throws Exception {

    }
}
