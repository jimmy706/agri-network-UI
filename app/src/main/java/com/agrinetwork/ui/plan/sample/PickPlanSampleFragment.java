package com.agrinetwork.ui.plan.sample;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.agrinetwork.R;
import com.agrinetwork.components.PlanSampleAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.PlanSample;
import com.agrinetwork.service.PlanService;
import com.agrinetwork.ui.plan.listener.OnPickPlanSampleListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Response;

public class PickPlanSampleFragment extends Fragment implements Step {
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final Date now = new Date();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<PlanSample> planSamples = new ArrayList<>();

    private PlanSampleAdapter planSampleAdapter;
    private String token;
    private PlanService planService;
    private final Calendar calendar;
    private final SimpleDateFormat sdf;
    private final OnPickPlanSampleListener onPickPlanSampleListener;

    private RecyclerView planSampleList;
    private TextInputEditText startDatePicker;

    private String pickedPlanSampleId = null;


    public PickPlanSampleFragment(OnPickPlanSampleListener onPickPlanSampleListener) {
        this.calendar = Calendar.getInstance();
        this.sdf = new SimpleDateFormat(DATE_FORMAT, new Locale("vi", "VI"));
        this.onPickPlanSampleListener = onPickPlanSampleListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_plan_sample, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        startDatePicker = view.findViewById(R.id.start_date_picker);
        startDatePicker.setText(sdf.format(now));
        DatePickerDialog.OnDateSetListener onFromDateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateText(startDatePicker);
        };
        startDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onFromDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker()
                    .setMinDate(now.getTime());
            datePickerDialog.show();
        });

        planSampleAdapter = new PlanSampleAdapter(getContext(), planSamples);
        planSampleList = view.findViewById(R.id.plan_sample_list);
        planSampleList.setAdapter(planSampleAdapter);
        planSampleList.setLayoutManager(new LinearLayoutManager(getContext()));

        planService = new PlanService(getContext());

        planSampleAdapter.setClickListener((v, position, planSampleId) -> {
            if (planSampleId != null) {
                pickedPlanSampleId = planSampleId;
            }
            try {
                Date startDate = sdf.parse(startDatePicker.getText().toString());
                onPickPlanSampleListener.onPick(pickedPlanSampleId, startDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        fetchSamples();
        return view;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        String startDate = startDatePicker.getText().toString();
        if (!startDate.isEmpty() && pickedPlanSampleId != null && !pickedPlanSampleId.isEmpty()) {
            return null;
        }

        return new VerificationError("Vui lòng chọn ngày và kế hoạch mẫu");
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {
        Toast.makeText(getContext(), "Vui lòng chọn ngày và kế hoạch mẫu", Toast.LENGTH_SHORT).show();
    }

    private void fetchSamples() {
        Future<List<PlanSample>> future = executorService.submit(() -> {
            Call call = planService.getPlanSamples();
            Response response = call.execute();
            if (response.code() == 200) {
                Type type = new TypeToken<List<PlanSample>>(){}.getType();
                Gson gson = new Gson();
                return gson.fromJson(response.body().string(), type);
            }
            return null;
        });

        try {
            List<PlanSample> planSamplesResponse = future.get();
            if (planSamplesResponse != null && !planSamplesResponse.isEmpty()) {
                planSamples.addAll(planSamplesResponse);
                planSampleAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDateText(TextInputEditText editText) {
        editText.setText(sdf.format(calendar.getTime()));
    }
}