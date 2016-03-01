package com.realdolmen.timeregistration;

import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.realdolmen.timeregistration.model.Session;
import com.realdolmen.timeregistration.service.BackendService;
import com.realdolmen.timeregistration.service.GenericVolleyError;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    @Bind(R.id.login_username)
    EditText username;

    @Bind(R.id.login_password)
    EditText password;

    @Bind(R.id.login_login_button)
    Button loginButton;

    private boolean loggingIn;
    private boolean ignoreDismiss = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        ignoreDismiss = true;
        super.onDestroy();
    }

    @OnClick(R.id.login_login_button)
    public void doLogin() {
        if (validate()) {
            loggingIn = true;
            final ProgressDialog loginProgress = new ProgressDialog(this);
            loginProgress.setIndeterminate(true);
            loginProgress.setMessage(getString(R.string.login_logging_in));
            loginProgress.show();
            loginProgress.setCanceledOnTouchOutside(false);
            loginProgress.setCancelable(false);
            BackendService.with(this).login(new Session(username.getText().toString(), password.getText().toString()), new BackendService.RequestCallback<Session>() {
                @Override
                public void onSuccess(Session data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ignoreDismiss)
                                loginProgress.dismiss();
                        }
                    });
                    Snackbar.make(findViewById(android.R.id.content), "Token received.", Snackbar.LENGTH_LONG).show();
                    System.out.println("Token: " + data.getJwtToken());
                    loggingIn = false;
                }

                @Override
                public void onError(VolleyError error) {
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 400) {
                            Snackbar.make(findViewById(android.R.id.content), R.string.login_incorrect_credentials, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), R.string.login_generic_error, Snackbar.LENGTH_LONG).show();
                        }


                    } else {
                        if (error instanceof GenericVolleyError) {
                            Log.e(TAG, "onError: " + error.getMessage());
                        }
                        final Snackbar bar = Snackbar.make(findViewById(android.R.id.content), R.string.login_server_unreachable, Snackbar.LENGTH_INDEFINITE);
                        bar.setAction(R.string.login_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bar.dismiss();
                                doLogin();
                            }
                        }).show();

                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bar.dismiss();
                            }
                        }, 10000);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ignoreDismiss)
                                loginProgress.dismiss();
                        }
                    });
                    loggingIn = false;
                }

            });
        }
    }

    private boolean validate() {
        boolean valid = true;
        if (username.getText().toString().trim().isEmpty()) {
            username.setError(getString(R.string.login_empty_field));
            valid = false;
        }

        if (password.getText().toString().trim().isEmpty()) {
            password.setError(getString(R.string.login_empty_field));
            valid = false;
        }

        return valid && !loggingIn;
    }
}
