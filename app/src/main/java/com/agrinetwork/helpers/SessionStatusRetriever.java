package com.agrinetwork.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.agrinetwork.config.Variables;

public class SessionStatusRetriever {
    private final Context context;
    private final SharedPreferences sharedPreferences;

    public SessionStatusRetriever(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
    }

    public String getToken() {
        return sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }
}
