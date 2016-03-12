package com.tch.lingoine.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationMethoud;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.github.bijoysingh.starter.Functions;
import com.tch.lingoine.R;

public class SignupActivity extends ActivityBase {

    Button signUp;
    EditText phoneNumber;
    EditText validation;
    String countryCode;
    LinearLayout userInformation;

    TextView username;
    TextView password;
    TextView domain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setToolbar("SIGNUP FOR LINGOINE");
        signUp = (Button) findViewById(R.id.signup_button);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        validation = (EditText) findViewById(R.id.validation_code);
        userInformation = (LinearLayout) findViewById(R.id.user_information);
        userInformation.setVisibility(View.GONE);

        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.password);
        domain = (TextView) findViewById(R.id.domain);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        countryCode = tm.getSimCountryIso();

        setupSignUp();
    }

    public void setupSignUp() {
        validation.setEnabled(false);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Kandy.getProvisioning().requestCode(
                    KandyValidationMethoud.CALL,
                    phoneNumber.getText().toString(),
                    countryCode, new KandyResponseListener() {
                        @Override
                        public void onRequestFailed(int responseCode, String err) {
                            Functions.makeToast(context, "Something went wrong," +
                                " please try again later");
                        }

                        @Override
                        public void onRequestSucceded() {
                            setupValidation();
                        }
                    });
            }
        });
    }

    public void setupValidation() {
        phoneNumber.setEnabled(false);
        validation.setEnabled(true);
        signUp.setText("VALIDATE");
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Kandy.getProvisioning().validateAndProvision(
                    phoneNumber.getText().toString(),
                    validation.getText().toString(),
                    countryCode, new KandyValidationResponseListener() {

                        @Override
                        public void onRequestFailed(int responseCode, String err) {
                            Functions.makeToast(context, "Something went wrong," +
                                " please try again later");
                        }

                        @Override
                        public void onRequestSuccess(IKandyValidationResponse response) {
                            userInformation.setVisibility(View.VISIBLE);
                            preferences.save(response.getUserId(), response.getUser(), response.getUserPassword(), response.getDomainName());

                            username.setText(response.getUser());
                            password.setText(response.getUserPassword());
                            domain.setText(response.getDomainName());
                            setupLogin();
                        }
                    });
            }
        });
    }

    public void setupLogin() {
        signUp.setText("GO TO LOGIN");
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(context, LoginActivity.class);
                startActivity(login);
                finish();
            }
        });
    }
}