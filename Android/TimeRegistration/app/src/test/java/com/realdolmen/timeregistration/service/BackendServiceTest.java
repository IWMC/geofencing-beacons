package com.realdolmen.timeregistration.service;

import android.content.Context;
import android.os.Build;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.realdolmen.timeregistration.BuildConfig;
import com.realdolmen.timeregistration.ui.login.LoginActivity;
import com.realdolmen.timeregistration.UITestActivity;
import com.realdolmen.timeregistration.model.Session;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by BCCAZ45 on 1/03/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, constants = BuildConfig.class)
public class BackendServiceTest {

    private Context dummyContext1, dummyContext2;

    @Mock
    private RequestQueue requestQueue;

    @Mock
    private RequestCallback requestCallback;

    @InjectMocks
    private BackendService backend;

    private Session session = new Session("test", "password");

    private final ObjectWrapper<JsonObjectRequest> requestWrapper = new ObjectWrapper<>();

    @Before
    public void setUp() throws Exception {
        dummyContext1 = Robolectric.setupActivity(LoginActivity.class);
        dummyContext2 = Robolectric.setupActivity(UITestActivity.class);
        backend = BackendService.with(dummyContext1);
        MockitoAnnotations.initMocks(this);

        when(requestQueue.add(any(Request.class))).then(new Answer<JsonObjectRequest>() {
            @Override
            public JsonObjectRequest answer(InvocationOnMock invocationOnMock) throws Throwable {
                JsonObjectRequest req = (JsonObjectRequest) invocationOnMock.getArguments()[0];
                requestWrapper.setObject(req);
                return req;
            }
        });
    }

    @Test
    public void testWith() throws Exception {
        BackendService service1 = BackendService.with(dummyContext1);
        BackendService service2 = BackendService.with(dummyContext2);

        assertNotEquals("Instances of different contexts should not be the same", service1, service2);
        assertEquals("Same service instance should be returned", service1, BackendService.with(dummyContext1));
        assertEquals("Same service instance should be returned", service2, BackendService.with(dummyContext2));
        assertNotEquals("Instances of different contexts should not be the same", service2, BackendService.with(dummyContext1));
    }

    //TODO: evaluate cost of testing login network requests


    private class ObjectWrapper<E> {
        private E object;

        public ObjectWrapper() {
        }

        public ObjectWrapper(E object) {
            this.object = object;
        }

        public E getObject() {
            return object;
        }

        public void setObject(E object) {
            this.object = object;
        }
    }
}
