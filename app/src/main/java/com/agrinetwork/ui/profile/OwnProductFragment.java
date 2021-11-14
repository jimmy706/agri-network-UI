package com.agrinetwork.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.components.ProductOwnAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PaginationResponse;
import com.agrinetwork.entities.Product;
import com.agrinetwork.service.ProductService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OwnProductFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_USER_ID = "userId";
    private ProductService productService;
    private PaginationResponse<Product> productPaginationResponse;
    private List<Product>productList = new ArrayList<>();
    private ProductOwnAdapter productAdapter;
    private String title;
    private String token;
    private String userWallId;
    private ProductService.SearchProductCriteria idOwn;
    private int page = 1;
    private final Gson gson = new Gson();
    private RecyclerView productFromOwn;
    private boolean hasNext = false;
    private TextView showTextNoProduct;

    public OwnProductFragment(){

    }

    public static OwnProductFragment newInstance(String title, String userId){
        OwnProductFragment ownProductFragment = new OwnProductFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_USER_ID, userId);
        ownProductFragment.setArguments(args);
        return ownProductFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            this.title = bundle.getString(ARG_TITLE);
            this.userWallId = bundle.getString(ARG_USER_ID);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate){
        View root = inflater.inflate(R.layout.fragment_own_product, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        productService = new ProductService(getContext());
        idOwn = new ProductService.SearchProductCriteria();
        idOwn.setOwner(userWallId);
        idOwn.setPage(page);

        showTextNoProduct = root.findViewById(R.id.no_product);

        productFromOwn = root.findViewById(R.id.list_product);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        productFromOwn.setLayoutManager(gridLayoutManager);
        productAdapter = new ProductOwnAdapter(productList,getContext());
        productFromOwn.setAdapter(productAdapter);



        productFromOwn.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleIndex = gridLayoutManager.findLastVisibleItemPosition();
                if(hasNext && lastVisibleIndex >= productList.size() - 2) {
                    loadMoreProductsOwn();
                }
            }
        });



        fetchProductFormOwn();
        return root;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchProductFormOwn(){
       Call getProductFormOwn = productService.searchProducts(token, idOwn);
        getProductFormOwn.enqueue(new Callback()  {
            @Override
            public void onFailure(@NonNull Call getProductFormOwn, @NonNull IOException e) {
                e.printStackTrace();
            }
            @SuppressLint({"ResourceAsColor", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call getProductFormOwn, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String productResponse = response.body().string();
                    Type productType = new TypeToken<PaginationResponse<Product>>(){}.getType();
                    productPaginationResponse = gson.fromJson(productResponse,productType);
                    getActivity().runOnUiThread(()->{
                        if(!productPaginationResponse.getDocs().isEmpty()){
                            productList.addAll(productPaginationResponse.getDocs());
                            productAdapter.notifyDataSetChanged();
                            hasNext =productPaginationResponse.isHasNextPage();

                        }
                        else {
                            showTextNoProduct.setVisibility(View.VISIBLE);
                        }

                    });
                }
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void loadMoreProductsOwn (){
        page += 1;
        idOwn.setPage(page);

        Call getProductFormOwn = productService.searchProducts(token, idOwn);
        getProductFormOwn.enqueue(new Callback()  {
            @Override
            public void onFailure(@NonNull Call getProductFormOwn, @NonNull IOException e) {
                e.printStackTrace();
            }
            @SuppressLint({"ResourceAsColor", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call getProductFormOwn, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String productResponse = response.body().string();
                    Type productType = new TypeToken<PaginationResponse<Product>>(){}.getType();
                    productPaginationResponse = gson.fromJson(productResponse,productType);
                    getActivity().runOnUiThread(()->{
                        if(!productPaginationResponse.getDocs().isEmpty()){
                            productList.addAll(productPaginationResponse.getDocs());
                            productAdapter.notifyDataSetChanged();
                            hasNext =productPaginationResponse.isHasNextPage();
                        }
                        System.out.println(productList);

                    });
                }
            }
        });

    }

}
