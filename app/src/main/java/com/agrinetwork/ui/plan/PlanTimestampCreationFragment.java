package com.agrinetwork.ui.plan;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.agrinetwork.R;
import com.agrinetwork.ui.plan.data.PlanTimestamp;
import com.agrinetwork.ui.plan.listener.SubmitPlanTimestampListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class PlanTimestampCreationFragment extends Fragment implements Step {
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private TextInputEditText fromDateInput, toDateInput, nameInput;
    private MaterialButton submitBtn;
    private final Calendar calendar;
    private final SubmitPlanTimestampListener submitPlanTimestampListener;
    private final SimpleDateFormat sdf;


    public PlanTimestampCreationFragment(SubmitPlanTimestampListener submitPlanTimestampListener) {
        this.calendar = Calendar.getInstance();
        this.submitPlanTimestampListener = submitPlanTimestampListener;
        this.sdf = new SimpleDateFormat(DATE_FORMAT, new Locale("vi", "VI"));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_timestamp_creation, container, false);
        fromDateInput = view.findViewById(R.id.from_date_input);
        toDateInput = view.findViewById(R.id.to_date_input);
        nameInput = view.findViewById(R.id.name_input);
        submitBtn = view.findViewById(R.id.submit_btn);

        Date now = new Date();
        fromDateInput.setText(sdf.format(now));

        DatePickerDialog.OnDateSetListener onFromDateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateText(fromDateInput);
        };
        fromDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onFromDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker()
                    .setMinDate(now.getTime());
            datePickerDialog.show();
        });

        DatePickerDialog.OnDateSetListener onToDateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateText(toDateInput);
        };
        toDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onToDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            DatePicker datePicker = datePickerDialog.getDatePicker();
            datePicker.setMinDate(now.getTime());
            try {
                Date fromDate = sdf.parse(fromDateInput.getText().toString());
                if (fromDate != null) {
                    datePicker.setMinDate(fromDate.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            datePickerDialog.show();
        });

        submitBtn.setOnClickListener(v -> {
            try {
                Date from = sdf.parse(fromDateInput.getText().toString());
                Date to = sdf.parse(toDateInput.getText().toString());
                String name = nameInput.getText().toString();

                submitPlanTimestampListener.onSubmit(new PlanTimestamp(name, from, to));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        return view;
    }

    private void updateDateText(TextInputEditText editText) {
        editText.setText(sdf.format(calendar.getTime()));
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