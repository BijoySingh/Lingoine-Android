package com.tch.lingoine.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tch.lingoine.R;
import com.tch.lingoine.items.LanguageItem;

/**
 * Language item view
 * Created by bijoy on 3/12/16.
 */
public class LanguageItemView extends LinearLayout {

    View view;
    LinearLayout backgroundView;
    TextView shortName;
    TextView name;
    public View message;
    public View video;

    private static final String[] colorsDark = {"D32F2F", "C2185B",
        "303F9F", "1976D2", "0288D1", "00796B",
        "388E3C", "689F38", "FFA000", "F57C00"};

    public LanguageItemView(Context context) {
        super(context);
        init(context);
    }

    public LanguageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LanguageItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public LanguageItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.language_item, this, true);

        shortName = (TextView) view.findViewById(R.id.short_name);
        name = (TextView) view.findViewById(R.id.name);
        backgroundView = (LinearLayout) findViewById(R.id.backgroundView);
        message = findViewById(R.id.message);
        video = findViewById(R.id.video);
    }

    public LanguageItemView setLanguage(LanguageItem item) {
        name.setText(item.language);
        shortName.setText(item.language);
        backgroundView.setBackgroundColor(Color.parseColor("#" + colorsDark[item.id % colorsDark.length]));
        return this;
    }

}
