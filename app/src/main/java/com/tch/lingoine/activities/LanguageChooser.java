package com.tch.lingoine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.bijoysingh.starter.Functions;
import com.github.bijoysingh.starter.server.AccessItem;
import com.github.bijoysingh.starter.util.FileManager;
import com.tch.lingoine.R;
import com.tch.lingoine.items.LanguageItem;
import com.tch.lingoine.server.Access;
import com.tch.lingoine.server.AccessIds;
import com.tch.lingoine.server.Filenames;
import com.tch.lingoine.server.Links;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LanguageChooser extends ActivityBase {

    public static final String CHOOSE_TYPE = "CHOOSE_TYPE";

    public static final Integer KNOWN_LANGUAGES = 0;
    public static final Integer PROFICIENT_LANGUAGES = 1;
    public static final Integer LEARN_LANGUAGES = 2;

    LinearLayout languages;
    Set<Integer> languagesSelected;
    Integer type;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_chooser);

        type = getIntent().getIntExtra(CHOOSE_TYPE, KNOWN_LANGUAGES);

        languages = (LinearLayout) findViewById(R.id.languages);
        nextButton = (Button) findViewById(R.id.next_button);

        setTitle();
        requestData();
        refreshView();
        setNextButton();
    }

    public void setNextButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInformation();
            }
        });
    }

    public void setTitle() {
        TextView title = (TextView) findViewById(R.id.title);
        if (type.equals(KNOWN_LANGUAGES)) {
            title.setText("Languages I know...");
        } else if (type.equals(PROFICIENT_LANGUAGES)) {
            title.setText("Languages I am proficient in...");
        } else if (type.equals(LEARN_LANGUAGES)) {
            title.setText("Languages I want to learn...");
        }
    }

    public String getLink() {
        if (type.equals(KNOWN_LANGUAGES)) {
            return Links.setKnownLanguages();
        } else if (type.equals(PROFICIENT_LANGUAGES)) {
            return Links.setProficientLanguages();
        } else if (type.equals(LEARN_LANGUAGES)) {
            return Links.setLearningLanguages();
        }
        return Links.setKnownLanguages();
    }

    public void requestData() {
        Access access = new Access(context);
        access.get(new AccessItem(Links.getLanguages(), Filenames.getLanguages(),
            AccessIds.ALL_LANGUAGES, true).setActivity(this));
    }

    public void refreshView() {
        languages.removeAllViews();
        List<LanguageItem> languageList = new ArrayList<>();
        languagesSelected = new HashSet<>();
        try {
            String text = FileManager.read(context, Filenames.getLanguages());
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

        for (LanguageItem item : languageList) {
            final Integer id = item.id;
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(item.language);
            checkBox.setTextSize(18);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        languagesSelected.add(id);
                    } else if (languagesSelected.contains(id)) {
                        languagesSelected.remove(id);
                    }
                }
            });
            languages.addView(checkBox);
        }
    }

    public void sendInformation() {
        Integer[] languagesArray = new Integer[languagesSelected.size()];
        languagesSelected.toArray(languagesArray);
        Map<String, Object> map = new HashMap<>();
        map.put("languages", languagesArray);

        Access access = new Access(context);
        access.send(new AccessItem(getLink(), null, AccessIds.SET_LANGUAGE_KNOWN,
            true).setActivity(this), map);
    }

    public void goToNextPage() {
        Intent nextIntent = new Intent(context, HomeActivity.class);
        if (type.equals(KNOWN_LANGUAGES)) {
            nextIntent = new Intent(context, LanguageChooser.class);
            nextIntent.putExtra(LanguageChooser.CHOOSE_TYPE, LanguageChooser.PROFICIENT_LANGUAGES);
        } else if (type.equals(PROFICIENT_LANGUAGES)) {
            nextIntent = new Intent(context, LanguageChooser.class);
            nextIntent.putExtra(LanguageChooser.CHOOSE_TYPE, LanguageChooser.LEARN_LANGUAGES);
        } else if (type.equals(LEARN_LANGUAGES)) {
            nextIntent = new Intent(context, HomeActivity.class);
        }

        startActivity(nextIntent);
        finish();
    }

    public void handleResponse(JSONObject response) {
        Boolean success = true;
        if (success) {
            goToNextPage();
        }
    }
}
