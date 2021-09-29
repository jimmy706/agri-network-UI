package com.agrinetwork.service;

import android.content.Context;

public class AbstractApiService {
    private Context context;

    public AbstractApiService(Context context) {
        this.context = context;
    }

    private void checkTokenExpiredBeforeCallApi() {

    }
}
