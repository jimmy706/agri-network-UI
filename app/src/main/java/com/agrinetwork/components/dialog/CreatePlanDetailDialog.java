package com.agrinetwork.components.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.components.NeededFactorAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Attribute;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.plan.Needed;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.service.CategoryService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Response;

public class CreatePlanDetailDialog extends Dialog {
    private final List<ProductCategory> categories = new ArrayList<>();
    private final List<Needed> neededFactors = new ArrayList<>();
    private CategoryService categoryService;
    private final Calendar calendar;
    private final SimpleDateFormat sdf;
    private final CreatePlanDetailDialog.OnSubmitListener onSubmitListener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private MaterialButton submitBtn;
    private TextInputEditText fromDateInput, toDateInput, nameInput;
    private ImageButton addNeededBtn;
    private MaterialAutoCompleteTextView textInputCategory;
    private TextInputEditText textInputName, textInputPriceFrom, textInputPriceTo;
    private MaterialButton submitNeededBtn;
    private LinearLayout addNeededForm;
    private RecyclerView neededList;

    private final String token;
    private NeededFactorAdapter neededFactorAdapter;
    private boolean isOpenNeededForm;

    private Date startDate;
    private Date endDate;

    public CreatePlanDetailDialog(@NonNull Context context, CreatePlanDetailDialog.OnSubmitListener onSubmitListener) {
        super(context);
        this.calendar = Calendar.getInstance();
        this.sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
        this.onSubmitListener = onSubmitListener;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }

    public CreatePlanDetailDialog(@NonNull Context context, CreatePlanDetailDialog.OnSubmitListener onSubmitListener, Date startDate, Date endDate) {
        super(context);
        this.calendar = Calendar.getInstance();
        this.sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
        this.onSubmitListener = onSubmitListener;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_plan_detail);

        categoryService = new CategoryService(getContext());
        fromDateInput = findViewById(R.id.from_date_input);
        toDateInput = findViewById(R.id.to_date_input);
        nameInput = findViewById(R.id.name_input);
        addNeededBtn = findViewById(R.id.add_needed_btn);
        submitBtn = findViewById(R.id.submit_btn);

        addNeededForm = findViewById(R.id.add_needed_form);
        textInputCategory = findViewById(R.id.edit_text_category);
        textInputName = findViewById(R.id.edit_text_product_name);
        textInputPriceFrom = findViewById(R.id.edit_text_price_from);
        textInputPriceTo = findViewById(R.id.edit_text_price_to);
        submitNeededBtn = findViewById(R.id.add_request_btn);

        neededList = findViewById(R.id.needed_list);
        neededFactorAdapter = new NeededFactorAdapter(getContext(), neededFactors);
        neededList.setAdapter(neededFactorAdapter);
        neededList.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchCategory();

        DatePickerDialog.OnDateSetListener onFromDateSetListener = (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateText(fromDateInput);
        };
        fromDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onFromDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            if (startDate != null) {
                DatePicker datePicker = datePickerDialog.getDatePicker();
                datePicker.setMinDate(startDate.getTime());
                fromDateInput.setText(sdf.format(startDate));
            }
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
            datePicker.setMinDate(new Date().getTime());

            try {
                Date fromDate = sdf.parse(fromDateInput.getText().toString());
                if (fromDate != null) {
                    datePicker.setMinDate(fromDate.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (endDate != null) {
                datePicker.setMaxDate(endDate.getTime());
            }
            datePickerDialog.show();
        });

        addNeededBtn.setOnClickListener(v -> {
           isOpenNeededForm = !isOpenNeededForm;
           displayNeededForm();
        });

        submitNeededBtn.setOnClickListener(v -> {
            onAddNeeded();
        });

        submitBtn.setOnClickListener(v -> {
            try {
                Date startDate = sdf.parse(fromDateInput.getText().toString());
                Date endDate = sdf.parse(toDateInput.getText().toString());
                PlanDetail planDetail = new PlanDetail();
                planDetail.setName(nameInput.getText().toString());
                planDetail.setNeededFactors(neededFactors);
                planDetail.setFrom(startDate);
                planDetail.setTo(endDate);
                onSubmitListener.onSubmit(planDetail);
            } catch (Exception e) {
                e.printStackTrace();
            }

            dismiss();
        });
    }

    private void updateDateText(TextInputEditText editText) {
        editText.setText(sdf.format(calendar.getTime()));
    }

    public interface OnSubmitListener {
        void onSubmit(PlanDetail planDetail);
    }

    private void fetchCategory() {
        Future<List<ProductCategory>> future = executor.submit(() -> {
            Call call = categoryService.getCategory(token);
            Response response = call.execute();
            if(response.code() == 200) {
                Type categoryList = new TypeToken<List<ProductCategory>>(){}.getType();
                List<ProductCategory> productCategories = new Gson().fromJson(response.body().string(), categoryList);
                return productCategories;
            }
            return null;
        });

        try {
            List<ProductCategory> categoriesResponse = future.get();
            if(categoriesResponse != null && !categoriesResponse.isEmpty()) {
                categories.addAll(categoriesResponse);
                List<String> categoryNames = Collections.emptyList();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    categoryNames = categories.stream().map(ProductCategory::getName).collect(Collectors.toList());
                }
                textInputCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categoryNames));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onAddNeeded() {
        String name = textInputName.getText().toString();
        String categoryName = textInputCategory.getText().toString();
        List<String> neededCategories = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            neededCategories.addAll(categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .map(ProductCategory::get_id)
                    .collect(Collectors.toList()));
        }
        double priceFrom = Double.parseDouble(textInputPriceFrom.getText().toString());
        double priceTo = Double.parseDouble(textInputPriceTo.getText().toString());
        Needed needed = new Needed();
        needed.setName(name);
        needed.setCategories(neededCategories);
        needed.setPriceRange(new double[]{priceFrom, priceTo});

        neededFactors.add(needed);
        neededList.setAdapter(new NeededFactorAdapter(getContext(), neededFactors));
        isOpenNeededForm = false;
        displayNeededForm();
    }

    private void displayNeededForm() {
        if (isOpenNeededForm) {
            addNeededForm.setVisibility(View.VISIBLE);
            addNeededBtn.setImageResource(R.drawable.ic_close);
            submitBtn.setVisibility(View.GONE);
            neededList.setVisibility(View.GONE);
        }
        else {
            addNeededForm.setVisibility(View.GONE);
            addNeededBtn.setImageResource(R.drawable.ic_plus);
            submitBtn.setVisibility(View.VISIBLE);
            neededList.setVisibility(View.VISIBLE);
        }
    }
}
