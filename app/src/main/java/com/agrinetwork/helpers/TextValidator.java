package com.agrinetwork.helpers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public abstract class TextValidator implements TextWatcher {

    @NonNull
    private final TextView textView;

    public abstract void validate(TextView textView, String value);

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        validate(this.textView, this.textView.getText().toString());
    }
}
