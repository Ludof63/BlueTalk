package com.fsanitize.bluetalk.UI;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Locale;

public abstract class LanguageBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreLangPreference();
    }

    private static final String LANG_PREFERENCE = "lang-pref";
    public void changeLanguage(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void saveLanguagePreference(String lang){
        SharedPreferences pref = getSharedPreferences(LANG_PREFERENCE,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LANG_PREFERENCE,lang);
        editor.commit();
    }

    private String getLangPreference(){
        SharedPreferences pref = getSharedPreferences(LANG_PREFERENCE,MODE_PRIVATE);
        return pref.getString(LANG_PREFERENCE,"empty");
    }

    private void restoreLangPreference(){
        String lang = getLangPreference();
        if(lang == "empty")
            return;
        changeLanguage(lang);
    }
}
