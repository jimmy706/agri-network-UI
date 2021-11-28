package com.agrinetwork;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.agrinetwork.components.SampleProductAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.decorator.HorizontalProductSpacingItemDecorator;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.product.Product;
import com.agrinetwork.entities.product.SampleProduct;
import com.agrinetwork.helpers.SampleProductConverter;
import com.agrinetwork.helpers.SessionStatusRetriever;
import com.agrinetwork.service.PlanService;
import com.agrinetwork.service.ProductService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Response;

public class CreateProductFromSampleActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PlanService planService;
    private ProductService productService;
    private String token;
    private String planId;

    private MaterialCardView cardView;
    private TextView planTitle, planDueDate;
    private RecyclerView sampleProductList;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product_from_sample);

        SessionStatusRetriever sessionStatusRetriever = new SessionStatusRetriever(this);
        token = sessionStatusRetriever.getToken();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        planId = bundle.getString("planId");

        planService = new PlanService(this);
        productService = new ProductService(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        cardView = findViewById(R.id.card_view);
        planTitle = findViewById(R.id.plan_name);
        planDueDate = findViewById(R.id.plan_duedate);
        sampleProductList = findViewById(R.id.sample_products);

        fetchPlan();
    }

    private void fetchPlan() {
        Future<Plan> future = executor.submit(() -> {
            Call call = planService.getPlanById(token, planId);
            Response response = call.execute();
            if (response.code() == 200) {
                Gson gson = new Gson();
                Type type = new TypeToken<Plan>(){}.getType();
                return gson.fromJson(response.body().string(), type);
            }
            return null;
        });

        try {
            Plan plan = future.get();
            renderData(plan);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderData(Plan plan) {
        planTitle.setText(plan.getName());

        SimpleDateFormat sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
        String startDate = sdf.format(plan.getFrom());
        String endDate = sdf.format(plan.getTo());
        planDueDate.setText(startDate + " - " + endDate);

        List<SampleProduct> sampleProducts = plan.getSampleResults();
        SampleProductAdapter adapter = new SampleProductAdapter(this, sampleProducts);
        adapter.setClickListener(sampleProduct -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("Chọn sản phầm này?")
                    .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                        SampleProductConverter converter = new SampleProductConverter(sampleProduct);
                        Product product = converter.toProduct(plan);
                        createProductFromPlan(product);
                    })
                    .setNegativeButton(R.string.cancel,null);
            dialogBuilder.create().show();
        });

        sampleProductList.setAdapter(adapter);
        sampleProductList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sampleProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlanInfoActivity.class);
            intent.putExtra("planId", planId);
            startActivity(intent);
        });
    }

    private void createProductFromPlan(Product product) {
        Future<Product> future = executor.submit(()->{
           Call call = productService.addFromPlan(token, planId, product);
               Response response = call.execute();
               if (response.code() == 201) {
                   response.close();
                   return new Product();
               }
           return null;
        });

        try {
            Product productResult = future.get();
            if (productResult != null) {
                Toast.makeText(this,"Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, UserFeedActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}