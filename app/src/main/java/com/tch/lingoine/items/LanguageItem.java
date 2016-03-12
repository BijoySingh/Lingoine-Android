package com.tch.lingoine.items;

import com.github.bijoysingh.starter.json.JsonField;
import com.github.bijoysingh.starter.json.JsonModel;

import org.json.JSONObject;

/**
 * Language item
 * Created by bijoy on 3/12/16.
 */
public class LanguageItem extends JsonModel {
    @JsonField
    public Integer id;

    @JsonField
    public String language;

    public LanguageItem(JSONObject json) throws Exception {
        super(json);
    }
}
