package com.tch.lingoine.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.github.bijoysingh.starter.Functions;
import com.github.bijoysingh.starter.server.AccessItem;
import com.tch.lingoine.R;
import com.tch.lingoine.server.Access;
import com.tch.lingoine.server.AccessIds;
import com.tch.lingoine.server.Links;
import com.tch.lingoine.utils.Preferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends ActivityBase {

    Button login;
    Button signUp;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (preferences.isLoggedIn() && preferences.isFirstTimeLogin()) {
            // openHomeActivity();
            Intent languages = new Intent(context, LanguageChooser.class);
            languages.putExtra(LanguageChooser.CHOOSE_TYPE, LanguageChooser.KNOWN_LANGUAGES);
            startActivity(languages);
            finish();
            return;
        } else if (preferences.isLoggedIn()) {
            openHomeActivity();
            return;
        }

        login = (Button) findViewById(R.id.login_button);
        signUp = (Button) findViewById(R.id.new_user);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        username.setText(preferences.getCompleteUsername());
        password.setText(preferences.getPassword());
        setupButtons();
    }


    public void setupButtons() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDialog();
                if (preferences.getUsername().isEmpty()) {
                    String uname = username.getText().toString();
                    if (uname.contains("@")) {
                        Integer position = uname.indexOf("@");
                        preferences.save(uname.substring(0, position),
                            uname.substring(0, position),
                            password.getText().toString(),
                            uname.substring(position + 1));
                    }
                }

                KandyRecord kandyRecord;
                try {
                    kandyRecord = new KandyRecord(username.getText().toString());
                } catch (KandyIllegalArgumentException e) {
                    Functions.makeToast(context, "Something went wrong, check username");
                    return;
                }

                Kandy.getAccess().login(kandyRecord, password.getText().toString(), new KandyLoginResponseListener() {

                    @Override
                    public void onRequestFailed(int responseCode, String err) {
                        Functions.makeToast(context, err);
                    }

                    @Override
                    public void onLoginSucceeded() {
                        startServerLogin();
                    }
                });
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(context, SignupActivity.class);
                startActivity(login);
            }
        });
    }

    public void handleResponse(JSONObject response) {
        Log.d("LOGIN", response.toString());
        try {
            if (response.has("token")) {
                preferences.save(Preferences.Keys.AUTH_TOKEN, response.getString("token"));
                preferences.save(Preferences.Keys.SERVER_USER_ID, response.getString("uid"));
                openHomeActivity();
            }
        } catch (Exception e) {
            Log.d("LOGIN", "Failed");
            Functions.makeToast(context, "Your login failed.");
        }
    }

    public void openHomeActivity() {
        Intent home = new Intent(context, HomeActivity.class);
        startActivity(home);
        finish();
    }

    public void startDialog() {
        ProgressDialog.show(context, "Logging you in",
            "Please wait, while we get things ready", true).setCancelable(true);
    }

    public void startServerLogin() {
        Map<String, Object> authMap = new HashMap<>();

        authMap.put("username", preferences.getUsername());
        Access access = new Access(context);
        access.send(new AccessItem(Links.getLogin(), null,
            AccessIds.SERVER_LOGIN, true).setActivity(this), authMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        username.setText(preferences.getCompleteUsername());
        password.setText(preferences.getPassword());
    }
}
