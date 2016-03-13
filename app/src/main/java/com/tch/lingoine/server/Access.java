package com.tch.lingoine.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.github.bijoysingh.starter.server.AccessItem;
import com.github.bijoysingh.starter.server.AccessManager;
import com.github.bijoysingh.starter.util.FileManager;
import com.tch.lingoine.activities.HomeActivity;
import com.tch.lingoine.activities.LanguageChooser;
import com.tch.lingoine.activities.LoginActivity;
import com.tch.lingoine.utils.Preferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * the server access
 * Created by bijoy on 1/2/16.
 */
public class Access extends AccessManager {

    public Access(Context context) {
        super(context);
    }

    @Override
    public Map<String, String> getAuthenticationData() {
        Preferences preferences = new Preferences(context);
        Map<String, String> map = new HashMap<>();
        map.put("secret-key", "kg7CK2n8Un6D67Pwlum863w5Z6P6osnE");
        map.put("token-auth", preferences.load(Preferences.Keys.AUTH_TOKEN));
        Log.d("Access", map.toString());
        return map;
    }

    @Override
    public void handleGetResponse(AccessItem access, String response) {
        Log.d(Access.class.getSimpleName(), "handleGetResponse called");
        writeInFile(response, access);

        if (access.activity != null) {
            if (access.type.equals(AccessIds.ALL_LANGUAGES)) {
                ((LanguageChooser) access.activity).refreshView();
            } else if (access.type.equals(AccessIds.GET_LANGUAGE_KNOWN)
                || access.type.equals(AccessIds.GET_LANGUAGE_LEARNING)
                || access.type.equals(AccessIds.GET_LANGUAGE_PROFICIENT)) {
                ((HomeActivity) access.activity).refreshView();
            } else if (access.type.equals(AccessIds.GET_LANGUAGE_USER)) {
                ((HomeActivity) access.activity).handleCallResponse(response, AccessIds.GET_LANGUAGE_USER);
            } else if (access.type.equals(AccessIds.GET_LANGUAGE_USER_CHAT)) {
                ((HomeActivity) access.activity).handleCallResponse(response, AccessIds.GET_LANGUAGE_USER_CHAT);
            }
        }
    }

    @Override
    public void handleSendResponse(AccessItem access, JSONObject jsonObject) {
        Log.d(Access.class.getSimpleName(), "handleSendResponse called");
        writeInFile(jsonObject.toString(), access);

        if (access.activity != null) {
            if (access.type.equals(AccessIds.SERVER_LOGIN)) {
                ((LoginActivity) access.activity).handleResponse(jsonObject);
            } else if (access.type.equals(AccessIds.SET_LANGUAGE_KNOWN)
                || access.type.equals(AccessIds.SET_LANGUAGE_LEARNING)
                || access.type.equals(AccessIds.SET_LANGUAGE_PROFICIENT)) {
                ((LanguageChooser) access.activity).handleResponse(jsonObject);
            }
        }
    }

    @Override
    public void handleGetError(AccessItem access, VolleyError volleyError) {
        try {
            if (access.activity != null) {
            }

            Log.e(Access.class.getSimpleName(), "Error: " + access.url);
            String message = new String(volleyError.networkResponse.data);
            Log.e(Access.class.getSimpleName(), "SERVER_ERROR <" + message + ">");
        } catch (Exception exception) {
            Log.e(Access.class.getSimpleName(), volleyError.getMessage(), volleyError);
        }
    }

    @Override
    public void handleSendError(AccessItem access, VolleyError volleyError) {
        try {
            Log.e(Access.class.getSimpleName(), "Error: " + access.url);

            if (access.activity != null) {
            }

            String message = new String(volleyError.networkResponse.data);
            Log.e(Access.class.getSimpleName(), "SERVER_ERROR <" + message + ">");
        } catch (Exception exception) {
            Log.e(Access.class.getSimpleName(), volleyError.getMessage(), volleyError);
        }
    }

    public void writeInFile(String response, AccessItem accessItem) {
        if (accessItem.filename != null && !accessItem.filename.isEmpty()) {
            FileManager.write(context, accessItem.filename, response);
        }
    }
}
