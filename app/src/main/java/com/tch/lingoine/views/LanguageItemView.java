package com.tch.lingoine.views;

import android.annotation.TargetApi;
import android.content.Context;
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
    TextView shortName;
    TextView name;

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
    }

    public LanguageItemView setLanguage(LanguageItem item) {
        name.setText(item.language);
        shortName.setText(item.language.charAt(0));
        return this;
    }

}
