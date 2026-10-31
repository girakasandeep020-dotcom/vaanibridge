package com.sandeep.vaanibridge;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * VaaniBridge Phase 10: Session Manager
 * Handles secure storage of JWT tokens and user session state.
 */
public class SessionManager {
    private static final String PREF_NAME = "VaaniBridgePrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String token, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "User");
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
