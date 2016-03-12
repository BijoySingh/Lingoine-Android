package com.tch.lingoine.server;

/**
 * the links
 * Created by bijoy on 1/2/16.
 */
public class Links {
    public static String getBase() {
        return "http://10.196.31.204/";
    }

    public static String getLogin() {
        return "";
    }

    public static String getLanguages() {
        return getBase() + "api/language/";
    }

    public static String setKnownLanguages() {
        return getBase() + "api/language/";
    }

    public static String setProficientLanguages() {
        return getBase() + "api/language/";
    }

    public static String setLearningLanguages() {
        return getBase() + "api/language/";
    }
}
