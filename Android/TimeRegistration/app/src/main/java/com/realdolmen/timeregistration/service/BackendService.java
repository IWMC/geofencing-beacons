package com.realdolmen.timeregistration.service;

import android.content.Context;

/**
 * Created by BCCAZ45 on 29/02/2016.
 */
public class BackendService {
    private final static BackendService instance = new BackendService();

    private Context context;
    public static BackendService with(Context context) {
        instance.context = context;
        return instance;
    }

    public interface RequestCallback<E> {
        void onSuccess(E data);
        void onFail(E data);
    }

    public void login(String username, String password, RequestCallback<?> callback) {

    }
}
