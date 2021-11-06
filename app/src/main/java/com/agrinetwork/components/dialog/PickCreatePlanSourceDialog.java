package com.agrinetwork.components.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.agrinetwork.CreatePlanActivity;
import com.agrinetwork.CreatePlanFromSampleActivity;
import com.agrinetwork.R;
import com.google.android.material.button.MaterialButton;

public class PickCreatePlanSourceDialog extends Dialog {

    public PickCreatePlanSourceDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pick_create_plan_source);

        RadioButton mySourceRadio = findViewById(R.id.radio_my_source);
        RadioButton sampleRadio = findViewById(R.id.radio_from_sample);

        MaterialButton submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(v -> {
            if (mySourceRadio.isChecked()) {
                Intent intent = new Intent(getContext(), CreatePlanActivity.class);
                getContext().startActivity(intent);
            } else if (sampleRadio.isChecked()) {
                Intent intent = new Intent(getContext(), CreatePlanFromSampleActivity.class);
                getContext().startActivity(intent);
            }
        });
    }
}
