package com.tch.lingoine.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
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
    TextView country;
    String countryCode;
    LinearLayout userInformation;
    LinearLayout validationContainer;
    LinearLayout phoneContainer;

    TextView username;
    TextView password;
    TextView domain;
    Integer state;

    public static final String STATE = "STATE";
    public static final Integer SIGN_UP = 0;
    public static final Integer VALIDATION = 1;
    public static final Integer USER_INFORMATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        state = getIntent().getIntExtra(STATE, SIGN_UP);

        setToolbar("SIGNUP FOR LINGOINE");
        signUp = (Button) findViewById(R.id.signup_button);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        phoneNumber.setText(preferences.getPhoneNumber());
        validation = (EditText) findViewById(R.id.validation_code);
        country = (TextView) findViewById(R.id.country_code);
        userInformation = (LinearLayout) findViewById(R.id.user_information);
        validationContainer = (LinearLayout) findViewById(R.id.validation_container);
        phoneContainer = (LinearLayout) findViewById(R.id.phone_number_container);
        userInformation.setVisibility(View.INVISIBLE);
        validationContainer.setVisibility(View.GONE);

        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.password);
        domain = (TextView) findViewById(R.id.domain);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        countryCode = tm.getSimCountryIso().toUpperCase();
        if (countryCode.isEmpty()) {
            countryCode = "IN";
        }

        country.setText(countryCode);

        setupView();
    }

    public void setupView() {
        if (state.equals(SIGN_UP)) {
            phoneContainer.setVisibility(View.VISIBLE);
            validationContainer.setVisibility(View.GONE);
            signUp.setText("REQUEST CODE");
            setupSignUp();
        } else if (state.equals(VALIDATION)) {
            phoneNumber.setEnabled(false);
            validationContainer.setVisibility(View.VISIBLE);
            signUp.setText("VALIDATE");
            setupValidation();
        } else {
            phoneContainer.setVisibility(View.GONE);
            validationContainer.setVisibility(View.GONE);
            userInformation.setVisibility(View.VISIBLE);
            username.setText(preferences.getUsername());
            password.setText(preferences.getPassword());
            domain.setText(preferences.getDomain());
            setupLogin();
        }
    }

    public void setupSignUp() {
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setPhoneNumber(phoneNumber.getText().toString());
                Kandy.getProvisioning().requestCode(
                    KandyValidationMethoud.CALL,
                    phoneNumber.getText().toString(),
                    countryCode, new KandyResponseListener() {
                        @Override
                        public void onRequestFailed(int responseCode, String err) {
                            Functions.makeToast(context, "Something went wrong. " + err);
                        }

                        @Override
                        public void onRequestSucceded() {
                            Intent intent = new Intent(context, SignupActivity.class);
                            intent.putExtra(STATE, VALIDATION);
                            startActivity(intent);
                            finish();
                        }
                    });
            }
        });
    }

    public void setupValidation() {
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Kandy.getProvisioning().validateAndProvision(
                    phoneNumber.getText().toString(),
                    validation.getText().toString(),
                    countryCode, new KandyValidationResponseListener() {

                        @Override
                        public void onRequestFailed(int responseCode, String err) {
                            Log.d("VALIDATION", "Something went wrong. " + err);
                        }

                        @Override
                        public void onRequestSuccess(IKandyValidationResponse response) {
                            preferences.save(response.getUserId(), response.getUser(), response.getUserPassword(), response.getDomainName());
                            Intent intent = new Intent(context, SignupActivity.class);
                            intent.putExtra(STATE, USER_INFORMATION);
                            startActivity(intent);
                            finish();
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
                finish();
            }
        });
    }
}