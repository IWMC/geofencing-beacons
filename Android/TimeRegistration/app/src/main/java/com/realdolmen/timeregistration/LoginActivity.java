package com.realdolmen.timeregistration;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.realdolmen.timeregistration.service.SnackbarService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.login_username)
    EditText username;

    @Bind(R.id.login_password)
    EditText password;

    @Bind(R.id.login_login_button)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_login_button)
    public void doLogin() {
        if(username.getText().toString().isEmpty()) {

        }
    }
}
