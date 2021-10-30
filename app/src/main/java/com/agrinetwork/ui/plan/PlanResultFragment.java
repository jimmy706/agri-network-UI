package com.agrinetwork.ui.plan;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.agrinetwork.R;
import com.agrinetwork.entities.QuantityType;
import com.agrinetwork.entities.plan.HarvestProduct;
import com.agrinetwork.ui.plan.listener.SubmitIntentResultListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlanResultFragment extends Fragment implements Step {
    private TextInputEditText nameInput, quantityInput;
    private MaterialAutoCompleteTextView unitInput;
    private MaterialButton submitProduct;
    private final SubmitIntentResultListener submitIntentResultListener;

    public PlanResultFragment(SubmitIntentResultListener submitIntentResultListener) {
        this.submitIntentResultListener = submitIntentResultListener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_result, container, false);
        nameInput = view.findViewById(R.id.edit_text_product);
        quantityInput = view.findViewById(R.id.edit_text_quantity);
        unitInput = view.findViewById(R.id.edit_text_unit);
        submitProduct = view.findViewById(R.id.add_product);
        List<String> quantityTypes = Arrays.stream(QuantityType.values()).map(QuantityType::getLabel).collect(Collectors.toList());
        ArrayAdapter<String> quantityTypeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,quantityTypes);
        unitInput.setAdapter(quantityTypeAdapter);

        submitProduct.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            float quantity = Float.parseFloat(quantityInput.getText().toString());
            String quantityType = unitInput.getText().toString();
            HarvestProduct intentResult = new HarvestProduct(name, quantity, quantityType);
            submitIntentResultListener.onSubmit(intentResult);
        });
        return view;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}