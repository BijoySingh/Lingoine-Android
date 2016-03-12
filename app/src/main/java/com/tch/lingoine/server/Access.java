package com.tch.lingoine.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.github.bijoysingh.starter.Functions;
import com.github.bijoysingh.starter.server.AccessItem;
import com.github.bijoysingh.starter.server.AccessManager;
import com.github.bijoysingh.starter.util.FileManager;
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
        }
    }

    @Override
    public void handleSendResponse(AccessItem access, JSONObject jsonObject) {
        Log.d(Access.class.getSimpleName(), "handleSendResponse called");
        writeInFile(jsonObject.toString(), access);

        if (access.activity != null) {
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
            Log.e(Access.class.getSimpleName(), exception.getMessage(), exception);
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
            Log.e(Access.class.getSimpleName(), exception.getMessage(), exception);
        }
    }

    public void writeInFile(String response, AccessItem accessItem) {
        if (accessItem.filename != null && !accessItem.filename.isEmpty()) {
            FileManager.write(context, accessItem.filename, response);
        }
    }
}
