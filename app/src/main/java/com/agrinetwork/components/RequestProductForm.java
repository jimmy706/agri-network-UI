package com.agrinetwork.components;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.agrinetwork.R;
import com.agrinetwork.UserFeedActivity;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Attribute;
import com.agrinetwork.entities.Interest;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.Topic;
import com.agrinetwork.service.CategoryService;
import com.agrinetwork.service.InterestService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Response;

public class RequestProductForm extends LinearLayout {

    private final Context context;

    private final List<ProductCategory> categories = new ArrayList<>();

    private final CategoryService categoryService;

    private final InterestService interestService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final String token;

    private final SharedPreferences sharedPreferences;

    private MaterialAutoCompleteTextView textInputCategory;

    private TextInputEditText textInputName, textInputPriceFrom, textInputPriceTo;

    private MaterialButton submitBtn;

    public RequestProductForm(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        this.categoryService = new CategoryService(context);
        this.interestService = new InterestService(context);
        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
        init();
    }

    private void init() {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.request_product_form, this);

        textInputCategory = findViewById(R.id.edit_text_category);
        textInputName = findViewById(R.id.edit_text_product_name);
        textInputPriceFrom = findViewById(R.id.edit_text_price_from);
        textInputPriceTo = findViewById(R.id.edit_text_price_to);
        submitBtn = findViewById(R.id.add_request_btn);

        submitBtn.setOnClickListener(v -> addInterest());

        fetchCategory();
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
                textInputCategory.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, categoryNames));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addInterest() {
        executor.submit(() -> {
            List<Attribute> attributes = new ArrayList<>();
            String name = textInputName.getText().toString();
            attributes.add(new Attribute("name", name));

            String categoryName = textInputCategory.getText().toString();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                categories.stream()
                        .filter(c -> c.getName().equals(categoryName))
                        .findFirst()
                        .ifPresent(category -> attributes.add(new Attribute("category", category.get_id())));
            }

            String priceFrom = textInputPriceFrom.getText().toString();
            attributes.add(new Attribute("priceFrom", priceFrom));

            String priceTo = textInputPriceTo.getText().toString();
            attributes.add(new Attribute("priceTo", priceTo));

            Interest interest = new Interest(Topic.PRODUCT_REQUEST.getLabel(), attributes);
            Call call = interestService.addNew(token, interest);
            try {
                Response response = call.execute();
                if(response.code() == 200) {
                    System.out.println("Create new interest");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        Toast.makeText(context, "Tạo thành công bài đăng", Toast.LENGTH_SHORT).show();
        context.startActivity(new Intent(context, UserFeedActivity.class));
    }
}
