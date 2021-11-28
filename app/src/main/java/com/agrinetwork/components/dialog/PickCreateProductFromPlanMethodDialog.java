package com.agrinetwork.components.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.agrinetwork.R;

import com.google.android.material.button.MaterialButton;


public class PickCreateProductFromPlanMethodDialog extends Dialog {

    private OnSubmitListener onSubmitListener;

    public PickCreateProductFromPlanMethodDialog(@NonNull Context context, OnSubmitListener onSubmitListener) {
        super(context);
        this.onSubmitListener = onSubmitListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_pice_create_product_from_plan_method);

        RadioButton mySourceRadio = findViewById(R.id.radio_my_source);
        RadioButton sampleRadio = findViewById(R.id.radio_from_sample);

        MaterialButton submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(v -> {
           if (mySourceRadio.isChecked()) {
               onSubmitListener.onSubmit(PickCreateProductMethods.FROM_SOURCE);
           } else if (sampleRadio.isChecked()) {
               onSubmitListener.onSubmit(PickCreateProductMethods.FROM_SAMPLE);
           }
        });
    }

    public interface OnSubmitListener {
        void onSubmit(PickCreateProductMethods pickedMethod);
    }

    public enum PickCreateProductMethods {
        FROM_SOURCE,
        FROM_SAMPLE;
    }
}
