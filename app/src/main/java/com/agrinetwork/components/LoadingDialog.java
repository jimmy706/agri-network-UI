package com.agrinetwork.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class LoadingDialog  {
    private String message;
    private Activity context;
    AlertDialog alertDialog;

    public LoadingDialog(Activity context, String message) {
        this.message = message;
        this.context = context;
    }

    public void showLoading() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.context);

        LayoutInflater inflater = context.getLayoutInflater();

    }

    public void closeLoading() {
        if(alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
