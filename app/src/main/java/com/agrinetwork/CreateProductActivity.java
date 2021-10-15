package com.agrinetwork;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.agrinetwork.components.SliderAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Product;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.QuantityType;
import com.agrinetwork.helpers.TextValidator;
import com.agrinetwork.service.CategoryService;
import com.agrinetwork.service.MediaService;
import com.agrinetwork.service.ProductService;
import com.agrinetwork.ui.products.ProductsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderView;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateProductActivity extends AppCompatActivity {
    private final Gson gson = new Gson();
    private CategoryService categoryService;
    private final List<ProductCategory> productCategoryList = new ArrayList<>();
    private  String token;
    private SharedPreferences sharedPreferences;
    private TextInputEditText nameInput, quantityInput, priceInput;
    private MaterialAutoCompleteTextView  unitInput;
    private MultiAutoCompleteTextView categoryInput;
    private MaterialToolbar toolbar;
    private ImageButton pickImgBtn;
    private SliderView sliderView;
    private RelativeLayout containerImage;
    private MediaService mediaService;
    private MaterialButton submitProduct;
    private ProductService productService;

    private final List<Uri> imageUris = new ArrayList<>();
    private final List<String> imageUrls = new ArrayList<>();
    private final SliderAdapter<Uri> sliderAdapter = new SliderAdapter<>(imageUris);
    private List<String> idCategory = new ArrayList<>();
    private List<String> categoryName = new ArrayList<>();
    private List<String> pickedCategoryName= new ArrayList<>();

    ActivityResultLauncher<Intent> pickImageResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if(data.getClipData() != null) {
                // Choice multiple images
                ClipData clipData = data.getClipData();
                    imageUris.clear();
                for(int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imgUri = clipData.getItemAt(i).getUri();
                    imageUris.add(imgUri);
                }
                sliderAdapter.notifyDataSetChanged();
            }
            else if(data.getData() != null) {
                // Choice one
                Uri image = data.getData();
                imageUris.clear();
                imageUris.add(image);
                sliderAdapter.notifyDataSetChanged();
            }
            displayPickedImage();

        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);
        sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
        categoryService = new CategoryService(this);
        mediaService = new MediaService(this);
        productService = new ProductService(this);

        nameInput = findViewById(R.id.edit_text_product);
        categoryInput = findViewById(R.id.edit_text_category);
        quantityInput = findViewById(R.id.edit_text_quantity);
        priceInput = findViewById(R.id.edit_text_price);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            startActivity(new Intent(this, UserFeedActivity.class));
        });


        unitInput = findViewById(R.id.edit_text_unit);
        List<String> quantityType = Arrays.stream(QuantityType.values()).map(QuantityType::getLabel).collect(Collectors.toList());
        ArrayAdapter<String> quantityTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,quantityType);
        unitInput.setAdapter(quantityTypeAdapter);


        pickImgBtn = findViewById(R.id.picked_image_btn);
        pickImgBtn.setOnClickListener(v ->{
            Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageResultLauncher.launch(pickImageIntent);
        });


        sliderView = findViewById(R.id.image_slider);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.SLIDE);


        containerImage = findViewById(R.id.picked_image_product);
        displayPickedImage();

        getCategories(categoryInput );
        validateForm();

        submitProduct = findViewById(R.id.add_product);
        submitProduct.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String quantity = quantityInput.getText().toString();
            String price = priceInput.getText().toString();
            String unit = unitInput.getText().toString();

            String textCategory = categoryInput.getText().toString();
            pickedCategoryName.clear();
            idCategory.clear();
            String[] pickCategoryName = textCategory.split(",");
            for(String itemName : pickCategoryName){
                pickedCategoryName.add(itemName.trim());
            }

            for(int i = 0; i < productCategoryList.size(); i++ ){
                   ProductCategory aCategory = productCategoryList.get(i);
                    if(pickedCategoryName.contains(aCategory.getName())){
                        idCategory.add(aCategory.get_id());
                    }
            }


            System.out.println(productCategoryList);
            System.out.println(pickedCategoryName);
            System.out.println(idCategory);

            Product product = new Product();
            product.setName(name);
            product.setCategories(idCategory);
            product.setQuantity(quantity);
            product.setPrice(price);
            product.setQuantityType(unit);
            product.setThumbnails(imageUrls);

            if(!name.isEmpty() && !idCategory.isEmpty() && !quantity.isEmpty() && !price.isEmpty() && !unit.isEmpty()){
                requestPostProduct(product);
            }else{
                Toast.makeText(this,"Không được để trống các trường",Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void getCategories(MultiAutoCompleteTextView categoryInput){
        Call getAllCategory = categoryService.getCategory(token);
        getAllCategory.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getAllCategory, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call getAllCategory, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String responseCategory = response.body().string();
                    Type categoryList = new TypeToken<List<ProductCategory>>(){}.getType();
                    List<ProductCategory> productTypes = gson.fromJson(responseCategory,categoryList);

                    productCategoryList.addAll(productTypes);

                    for(ProductCategory item : productTypes){
                       categoryName.add(item.getName());
                    }

                    CreateProductActivity.this.runOnUiThread(()->{
                        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(CreateProductActivity.this, android.R.layout.simple_list_item_1,categoryName);
                        categoryInput.setAdapter(categoryAdapter);
                        categoryInput.setThreshold(1);
                        categoryInput.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    });
                }
            }
        });
    }

    public void displayPickedImage() {
        if(containerImage != null){
            if(!imageUris.isEmpty()){
                containerImage.setVisibility(View.VISIBLE);
                imageUrls.clear();
                requestUploadImages();
            }
            else {
                containerImage.setVisibility(View.INVISIBLE);
            }
        }
    }
    private void requestUploadImages()  {
        final boolean[] uploadSuccess = {true};
        for(int i = 0; i < imageUris.size(); i++) {
            Uri image = imageUris.get(i);

            try {
                Call call = mediaService.uploadImage(image, token);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        uploadSuccess[0] = false;
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.code() == 201) {

                            String url = response.body().string();
                            imageUrls.add(url);
                        }
                    }
                });
            } catch (Exception e) {
                uploadSuccess[0] = false;
                e.printStackTrace();
            }

            if(!uploadSuccess[0]) {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPostProduct(Product product){
        Call createProduct = productService.addProduct(token,product);
        //System.out.println(product);
        createProduct.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                CreateProductActivity.this.runOnUiThread(()->{
                    Toast.makeText(CreateProductActivity.this,"Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateProductActivity.this, UserFeedActivity.class));
                });
          }
        });
    }

    private void validateForm(){
        nameInput.addTextChangedListener(new TextValidator(nameInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()){
                    textView.setError(getText(R.string.product_name_required));
                }
            }
        });

        categoryInput.addTextChangedListener(new TextValidator(categoryInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()){
                    textView.setError(getText(R.string.category_required));
                }
            }
        });

        quantityInput.addTextChangedListener(new TextValidator(quantityInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()){
                    textView.setError(getText(R.string.quantity_required));
                }
            }
        });

        priceInput.addTextChangedListener(new TextValidator(priceInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()){
                    textView.setError(getText(R.string.price_required));
                }
            }
        });

        unitInput.addTextChangedListener(new TextValidator(unitInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()){
                    textView.setError(getText(R.string.unit_required));
                }
            }
        });
    }
}
