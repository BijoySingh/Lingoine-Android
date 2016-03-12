package com.tch.lingoine.utils;

import android.content.Context;

import com.github.bijoysingh.starter.util.PreferenceManager;

/**
 * Shared preferences
 * Created by bijoy on 3/12/16.
 */
public class Preferences extends PreferenceManager {

    public Preferences(Context context) {
        super(context);
    }

    @Override
    public String getPreferencesFolder() {
        return "LINGOINE";
    }


    public class Keys {
        public static final String AUTH_TOKEN = "AUTH_TOKEN";
        public static final String USER_ID = "USER_ID";
        public static final String USERNAME = "USERNAME";
        public static final String PASSWORD = "PASSWORD";
        public static final String DOMAIN = "DOMAIN";
        public static final String PHONE_NUMBER = "PHONE_NUMBER";
    }

    public void save(String userId, String username, String password, String domain) {
        save(Keys.USER_ID, userId);
        save(Keys.USERNAME, username);
        save(Keys.PASSWORD, password);
        save(Keys.DOMAIN, domain);
    }

    public void setPhoneNumber(String number) {
        save(Keys.PHONE_NUMBER, number);
    }

    public String getPhoneNumber() {
        return load(Keys.PHONE_NUMBER, "");
    }

    public String getUserId() {
        return load(Keys.USER_ID, "");
    }

    public String getUsername() {
        return load(Keys.USERNAME, "");
    }

    public String getDomain() {
        return load(Keys.DOMAIN, "");
    }

    public String getCompleteUsername() {
        if (!getUsername().isEmpty()) {
            return getUsername() + "@" + getDomain();
        }
        return "";
    }

    public String getPassword() {
        return load(Keys.PASSWORD, "");
    }

}
