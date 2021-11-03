package com.agrinetwork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.components.ProductAdapter;
import com.agrinetwork.components.SliderAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.decorator.HorizontalProductSpacingItemDecorator;
import com.agrinetwork.entities.Product;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.ProductDetail;
import com.agrinetwork.entities.User;
import com.agrinetwork.helpers.CurrencyFormatter;
import com.agrinetwork.service.ProductService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Variables.POST_DATE_FORMAT, new Locale("vi", "VN"));
    private String userId;
    private List<ProductCategory> productCategoryList = new ArrayList<>();

    private List<Product> productsFromOwn = new ArrayList<>();
    private List<Product> productsOwnResponse = new ArrayList<>();

    private List<Product> productsRelated = new ArrayList<>();
    private List<Product> productsRelatedResponse = new ArrayList<>();


    private ProductService productService;
    private ProductDetail productDetail;
    private String productId;
    private String token;
    private ProductAdapter productFromOwnAdapter;
    private ProductAdapter productRelatedAdapter;

    private SliderView sliderView;
    private TextView productName, quantity, numView, price, dateCreate,userName;
    private MaterialButton buttonContact;
    private View imagesWrapper;
    private CircleImageView avatar;
    private ChipGroup productCategory;
    private ImageView imageView;
    private RecyclerView fromOwnerProductList;
    private RecyclerView fromRelatedProductList;
    private MaterialToolbar toolbar;
    private User own;



    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_product_detail);

        productService = new ProductService(this);

        Intent intent = getIntent();
        productId = intent.getExtras().getString("productId");

        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");

        toolbar = findViewById(R.id.toolbar);
        sliderView = findViewById(R.id.image_slider);
        productName = findViewById(R.id.product_name_detail);
        quantity = findViewById(R.id.quantity_num);
        numView = findViewById(R.id.product_views);
        price = findViewById(R.id.price_num);
        dateCreate = findViewById(R.id.date);
        userName = findViewById(R.id.user_name);
        imagesWrapper = findViewById(R.id.images_wrapper);
        imageView = findViewById(R.id.image_view);
        avatar = findViewById(R.id.avatar_own);
        productCategory = findViewById(R.id.category_product);
        buttonContact = findViewById(R.id.btn_contact);

        fromOwnerProductList = findViewById(R.id.products_by_own);
        fromOwnerProductList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        fromOwnerProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        fromRelatedProductList = findViewById(R.id.products_related);
        fromRelatedProductList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        fromRelatedProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        fetchProductDetail();

       avatar.setOnClickListener(v->{
           contactOwn(userId);
       });

        buttonContact.setOnClickListener(v->{
            Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + own.getPhoneNumber()));
            startActivity(phoneCallIntent);
        });

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void fetchProductDetail(){
        Call getProduct = productService.getProductById(token,productId);
        getProduct.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getProduct, @NonNull IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NonNull Call getProduct, @NonNull Response response) throws IOException {
                Gson gson = new Gson();
                String dataResponse = response.body().string();
                productDetail = gson.fromJson(dataResponse, ProductDetail.class);
                System.out.println(productDetail);
                ProductDetailActivity.this.runOnUiThread(()->{
                    renderData();
                });

            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private  void renderData(){

        String nameDetail = productDetail.getName();
        productName.setText(nameDetail);

        String quantityFormat = productDetail.getQuantity() + " "+ productDetail.getQuantityType();
        quantity.setText(quantityFormat);

        String priceText = CurrencyFormatter.format(productDetail.getPrice()) + CurrencyFormatter.CURRENCY_SUFFIX;
        price.setText(priceText);

        String numOfView = Integer.toString(productDetail.getNumberOfViews());
        numView.setText(numOfView);

        String postProductDate = dateFormat.format(productDetail.getCreatedDate());
        dateCreate.setText(postProductDate);

         own = productDetail.getOwner();
        String fullName = own.getFirstName() + " " + own.getLastName();
        userId = own.get_id();
        userName.setText(fullName);

        String avatarUrl = own.getAvatar();
        if(avatarUrl != null && !avatarUrl.isEmpty()){
            Picasso.get().load(avatarUrl)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(avatar);
        }


        List<String> productImages = productDetail.getThumbnails();
        if(!productImages.isEmpty()){
            Picasso picasso = Picasso.get();
            if(productImages.size()== 1){
                picasso.load(productImages.get(0)).into(imageView);
                sliderView.setVisibility(View.GONE);
            }
            else{
                List<Bitmap> imageBitmaps = new ArrayList<>();
                SliderAdapter<Bitmap> sliderAdapter = new SliderAdapter<>(imageBitmaps);
                sliderView.setSliderAdapter(sliderAdapter);
                productImages.forEach(img -> {
                    picasso.load(img).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            System.out.println("Image added");
                            imageBitmaps.add(bitmap);
                            sliderAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            e.printStackTrace();
                            System.out.println("Failed to load image: " + e.getMessage());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });

                });
            }
        }
        else {
            imagesWrapper.setVisibility(View.GONE);
        }

        productCategoryList = productDetail.getCategories();
        for(ProductCategory item: productCategoryList){
            final Chip chip = new Chip(this);
            chip.setText(item.getName());
            productCategory.addView(chip);

            chip.setOnClickListener((chipClickListener)->{
                Toast.makeText(this, ""+ item.getName(), Toast.LENGTH_SHORT).show();
                Intent intentSearchByCategory = new Intent(this,ProductsActivity.class);
                intentSearchByCategory.putExtra("idCategory",item.get_id());
                intentSearchByCategory.putExtra("nameCategory",item.getName());
                this.startActivity(intentSearchByCategory);
            });


        }


        productsOwnResponse = productDetail.getFromOwnerProducts();
        productsFromOwn.clear();
        if (!productsOwnResponse.isEmpty()){
            productsFromOwn.addAll(productsOwnResponse);
            productFromOwnAdapter = new ProductAdapter(productsFromOwn,this);
            fromOwnerProductList.setAdapter(productFromOwnAdapter);
        }

        productsRelatedResponse = productDetail.getRelatedProducts();
        productsRelated.clear();
        if(!productsRelatedResponse.isEmpty()){
            productsRelated.addAll(productsRelatedResponse);
            productRelatedAdapter = new ProductAdapter(productsRelated,this);
            fromRelatedProductList.setAdapter(productRelatedAdapter);
        }

    }

    private void contactOwn(String userId){
        Intent intent = new Intent(this,UserWallActivity.class);
        intent.putExtra("userId",userId);
        startActivity(intent);
    }

}
