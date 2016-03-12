package com.tch.lingoine.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tch.lingoine.R;
import com.tch.lingoine.utils.Preferences;

/**
 * Basic Activity functions and variables
 * Created by bijoy on 1/25/16.
 */
public abstract class ActivityBase extends AppCompatActivity {

    Context context;
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        preferences = new Preferences(context);
    }

    public void setToolbar() {
        setToolbar(getResources().getString(R.string.app_name));
    }

    public void setToolbar(Integer title) {
        setToolbar(R.drawable.back_arrow, getString(title), true);
    }

    public void setToolbar(String title) {
        setToolbar(R.drawable.back_arrow, title, true);
    }

    public void setToolbar(Integer icon, String title, final Boolean setBackPressListener) {
        ImageView toolbarIcon = (ImageView) findViewById(R.id.toolbar_icon);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        toolbarIcon.setImageResource(icon);
        toolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setBackPressListener) {
                    onBackPressed();
                }
            }
        });
        toolbarTitle.setText(title);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
