package com.tch.lingoine.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.github.bijoysingh.starter.server.AccessItem;
import com.github.bijoysingh.starter.util.FileManager;
import com.tch.lingoine.R;
import com.tch.lingoine.items.LanguageItem;
import com.tch.lingoine.server.Access;
import com.tch.lingoine.server.AccessIds;
import com.tch.lingoine.server.Filenames;
import com.tch.lingoine.server.Links;
import com.tch.lingoine.views.LanguageItemView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends ActivityBase {

    LinearLayout learnLeft, learnRight;
    LinearLayout helpLeft, helpRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupLayouts();

        preferences.firstTimeLoggedIn();

        requestData();
        refreshView();
    }

    public void setupLayouts() {
        helpLeft = (LinearLayout) findViewById(R.id.help_left);
        helpRight = (LinearLayout) findViewById(R.id.help_right);
        learnLeft = (LinearLayout) findViewById(R.id.learn_left);
        learnRight = (LinearLayout) findViewById(R.id.learn_right);
    }

    public void requestData() {
        Access access = new Access(context);
        access.get(new AccessItem(Links.getKnownLanguages(), Filenames.getKnownLanguages(),
            AccessIds.GET_LANGUAGE_KNOWN, true).setActivity(this));
        access.get(new AccessItem(Links.getLearningLanguages(), Filenames.getLearningLanguages(),
            AccessIds.GET_LANGUAGE_LEARNING, true).setActivity(this));
    }

    public List<LanguageItem> getLanguageList(String filename) {
        List<LanguageItem> languageList = new ArrayList<>();
        try {
            String text = FileManager.read(context, filename);
            JSONObject json = new JSONObject(text);
            JSONArray array = json.getJSONArray("results");
            for (int position = 0; position < array.length(); position++) {
                JSONObject jsonItem = array.getJSONObject(position);
                LanguageItem item = new LanguageItem(jsonItem);
                languageList.add(item);
            }
        } catch (Exception e) {
            Log.e("LANGUAGE_CHOOSER", e.getMessage(), e);
        }
        return languageList;
    }

    public void refreshView() {
        helpLeft.removeAllViews();
        helpRight.removeAllViews();
        learnLeft.removeAllViews();
        learnRight.removeAllViews();

        List<LanguageItem> knownLanguageList = getLanguageList(Filenames.getKnownLanguages());
        List<LanguageItem> learningLanguageList = getLanguageList(Filenames.getLearningLanguages());


        Integer position = 0;
        for (LanguageItem item : knownLanguageList) {
            LanguageItemView view = new LanguageItemView(context);
            view.setLanguage(item);
            if (position % 2 == 0) {
                helpLeft.addView(view);
            } else {
                helpRight.addView(view);
            }
            position++;
        }

        position = 0;
        for (LanguageItem item : learningLanguageList) {
            LanguageItemView view = new LanguageItemView(context);
            view.setLanguage(item);
            if (position % 2 == 0) {
                learnLeft.addView(view);
            } else {
                learnRight.addView(view);
            }
            position++;
        }
    }
}
