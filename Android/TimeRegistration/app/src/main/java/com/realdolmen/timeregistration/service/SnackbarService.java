package com.realdolmen.timeregistration.service;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.realdolmen.timeregistration.R;

/**
 * Created by BCCAZ45 on 29/02/2016.
 */
public class SnackbarService {
    public static Snackbar createErrorSnackbar(Activity a, String message, String buttonText, View.OnClickListener listener) {
        View v = a.findViewById(android.R.id.content);
        Snackbar bar = Snackbar.make(v, message, Snackbar.LENGTH_INDEFINITE);
        bar.setAction(buttonText, listener);
        //bar.setActionTextColor(Color.WHITE);
        View sbView = bar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        return bar;
    }
}
