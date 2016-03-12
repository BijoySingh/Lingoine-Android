package com.tch.lingoine.activities;

import android.os.Bundle;

import com.tch.lingoine.R;

public class LanguageChooser extends ActivityBase {

    public static final String CHOOSE_TYPE = "CHOOSE_TYPE";
    public static final String KNOWN_LANGUAGES = "KNOWN_LANGUAGES";
    public static final String PROFICIENT_LANGUAGES = "PROFICIENT_LANGUAGES";
    public static final String LEARN_LANGUAGES = "LEARN_LANGUAGES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_chooser);


    }
}
