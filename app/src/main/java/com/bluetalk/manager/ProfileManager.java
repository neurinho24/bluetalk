package com.bluetalk.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class ProfileManager {

    private static final String PREFS = "bluetalk_profile";
    private static final String KEY_NICK = "nickname";
    private static final String KEY_PHOTO = "photo_path";

    private final SharedPreferences preferences;

    public ProfileManager(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getNickname() {
        String nick = preferences.getString(KEY_NICK, "Usuário 🔵");
        return nick.contains("🔵") ? nick : nick + " 🔵";
    }

    public void setNickname(String nickname) {
        preferences.edit().putString(KEY_NICK, nickname).apply();
    }

    public String getPhotoPath() {
        return preferences.getString(KEY_PHOTO, "");
    }

    public void setPhotoPath(String path) {
        preferences.edit().putString(KEY_PHOTO, path).apply();
    }
}
