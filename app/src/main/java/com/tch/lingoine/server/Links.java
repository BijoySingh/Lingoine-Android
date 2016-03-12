package com.tch.lingoine.server;

/**
 * the links
 * Created by bijoy on 1/2/16.
 */
public class Links {
    public static String getBase() {
        return "http://lingoine.thecodershub.com/";
    }

    public static String getLogin() {
        return getBase() + "api/accounts/login/";
    }

    public static String getLanguages() {
        return getBase() + "api/language/";
    }

    public static String setKnownLanguages() {
        return getBase() + "api/user_language/set_know_languages/";
    }

    public static String setProficientLanguages() {
        return getBase() + "api/user_language/set_proficient_languages/";
    }

    public static String setLearningLanguages() {
        return getBase() + "api/user_language/set_learning_languages/";
    }

    public static String getKnownLanguages() {
        return getBase() + "api/user_language/get_know_languages/";
    }

    public static String getProficientLanguages() {
        return getBase() + "api/user_language/get_proficient_languages/";
    }

    public static String getLearningLanguages() {
        return getBase() + "api/user_language/get_learning_languages/";
    }
}
