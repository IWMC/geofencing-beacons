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
import com.realdolmen.timeregistration.model.User;
import com.realdolmen.timeregistration.service.BackendService;
import com.realdolmen.timeregistration.service.GenericVolleyError;

import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_login_button)
    public void doLogin() {
        if (validate()) {
            final ProgressDialog loginProgress = new ProgressDialog(this);
            loginProgress.setIndeterminate(true);
            loginProgress.setMessage(getString(R.string.login_logging_in));
            loginProgress.show();

            BackendService.with(this).login(new User(username.getText().toString(), password.getText().toString()), new BackendService.RequestCallback<User>() {
                @Override
                public void onSuccess(User data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginProgress.dismiss();
                        }
                    });
                    Snackbar.make(findViewById(android.R.id.content), "Token received.", Snackbar.LENGTH_LONG).show();
                    System.out.println("Token: " + data.getJwtToken());
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
                            loginProgress.dismiss();
                        }
                    });
                }
            });
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginProgress.dismiss();
                        }
                    });
                }
            }, 3000);

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

        return valid;
    }
}
