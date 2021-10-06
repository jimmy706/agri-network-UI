package com.agrinetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.District;
import com.agrinetwork.entities.DistrictDetail;
import com.agrinetwork.entities.Place;
import com.agrinetwork.entities.Province;
import com.agrinetwork.entities.ProvinceDetail;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.Ward;
import com.agrinetwork.service.CountryService;
import com.agrinetwork.service.ProvinceService;
import com.agrinetwork.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateUserActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Gson gson = new Gson();

    private final List<Province> provinces = new ArrayList<>();
    private final List<District> districts = new ArrayList<>();
    private final List<Ward> wards = new ArrayList<>();

    private UserService userService;
    private ProvinceService provinceService;
    private CountryService countryService;

    private TextInputEditText emailText,  firstNameInput, lastNameInput,phoneNumberInput;
    private MaterialAutoCompleteTextView provinceInput, districtInput, wardInput;
    private User userState;
    private String token;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        SharedPreferences sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token =  sharedPreferences.getString(Variables.ID_TOKEN_LABEL,"");

        userService = new UserService(this);
        provinceService = new ProvinceService(this);
        countryService = new CountryService(this);

        Intent intent = this.getIntent();
        String id =  intent.getExtras().getString("id");

        MaterialToolbar iconBack = findViewById(R.id.backToWall);
        iconBack.setNavigationOnClickListener(view -> {
            finish();
        });

        emailText = findViewById(R.id.display_email);
        firstNameInput = findViewById(R.id.update_first_name);
        lastNameInput = findViewById(R.id.update_last_name);
        phoneNumberInput = findViewById(R.id.update_phone);
        provinceInput = findViewById(R.id.update_province);
        districtInput = findViewById(R.id.edit_text_district);
        wardInput = findViewById(R.id.edit_text_ward);
        getAllProvinces(provinceInput);

        fetchUserBeforeUpdate(id);

        MaterialButton updateButton = findViewById(R.id.update_btn);
        updateButton.setOnClickListener(v -> {
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String phoneNumber = phoneNumberInput.getText().toString();
            String province = provinceInput.getText().toString();
            String district = districtInput.getText().toString();
            String ward = wardInput.getText().toString();

            User updatingUser = new User();
                    updatingUser.setFirstName(firstName);
                    updatingUser.setLastName(lastName);
                    updatingUser.setPhoneNumber(phoneNumber);
                    updatingUser.setProvince(province);
                    updatingUser.setDistrict(district);
                    updatingUser.setWard(ward);
                    String avatar = userState.getAvatar();
                    updatingUser.setAvatar(avatar);

            requestUpdateUser(updatingUser,token);


            Toast.makeText(UpdateUserActivity.this, "Cập nhật tài khoản thành công", Toast.LENGTH_SHORT).show();
            Intent intentNew = new Intent(this, UserWallActivity.class);
            intentNew.putExtra("userId",id);
            startActivity(intentNew);
        });

        provinceInput.setOnItemClickListener((adapterView, view, i, l) -> {
            String name = provinceInput.getText().toString();
            Optional<Province> province = provinces.stream().filter(p -> p.getName().equals(name)).findFirst();
            province.ifPresent(value -> getDistrictsByProvince(value, true));
        });

        districtInput.setOnItemClickListener((adapterView, view, i, l) -> {
            String name = districtInput.getText().toString();
            Optional<District> district = districts.stream().filter(d -> d.getName().equals(name)).findFirst();
            district.ifPresent(value -> getWardsByDistrict(value, true));
        });
    }

    private  void requestUpdateUser(User user, String token){
    Call updateUser = userService.update(user, token);
        updateUser.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call updateUser, @NonNull Response response) throws IOException {
                System.out.println(response);
            }
        });

    }

    private  void fetchUserBeforeUpdate(String id){

        Call call = userService.getById(token, id);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getUserLogin, @NonNull IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Gson gson = new Gson();
                String jsonData = response.body().string();
                userState = gson.fromJson(jsonData, User.class);

                UpdateUserActivity.this.runOnUiThread(()-> {
                    renderBeforeUpdate(userState);
                });

            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void  renderBeforeUpdate(User userResponse){

            String emailAddress = userResponse.getEmail();
            emailText.setText(emailAddress);

            String firstName = userResponse.getFirstName();
            firstNameInput.setText(firstName);

            String lastName = userResponse.getLastName();
            lastNameInput.setText(lastName);

            String phoneNumber = userResponse.getPhoneNumber();
            phoneNumberInput.setText(phoneNumber);

            String province = userResponse.getProvince();
            provinceInput.setText(province);

            String district = userResponse.getDistrict();
            districtInput.setText(district);

            String ward = userResponse.getWard();
            wardInput.setText(ward);

            Optional<Province> currentPickedProvince = provinces.stream().filter(p -> p.getName().equals(province)).findFirst();
            currentPickedProvince.ifPresent(p -> getDistrictsByProvince(p, false));
    }

    private void getAllProvinces(MaterialAutoCompleteTextView provinceInput) {
        provinceService.getAll().enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Province[] provincesResponse = gson.fromJson(response.body().string(), Province[].class);
                provinces.addAll(Arrays.asList(provincesResponse));

                UpdateUserActivity.this.runOnUiThread(()-> {
                    ArrayAdapter<String> provincesAdapter = new ArrayAdapter<>(UpdateUserActivity.this,
                            android.R.layout.simple_list_item_1,
                            provinces.stream().map(Province::getName).collect(Collectors.toList()));
                    provinceInput.setAdapter(provincesAdapter);
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getDistrictsByProvince(Province province, boolean shouldResetText) {
        Future<ProvinceDetail> provinceDetailFuture = executorService.submit(() -> {
           Call call = countryService.getProvinceDetail(province.getAreaCode());
           Response response = call.execute();
           if(response.code() == 200) {
               return gson.fromJson(response.body().string(), ProvinceDetail.class);
           }
           return null;
        });

        try {
            ProvinceDetail provinceDetail = provinceDetailFuture.get();

            if(provinceDetail != null){
                if(shouldResetText) {
                    districtInput.setText("");
                }
                districts.clear();

                districts.addAll(provinceDetail.getDistricts());
                ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                        UpdateUserActivity.this,
                        android.R.layout.simple_list_item_1,
                        districts.stream().map(Place::getName).collect(Collectors.toList()));
                districtInput.setAdapter(districtAdapter);

                Optional<District> currentPickedDistrict = districts.stream().filter(d -> d.getName().equals(userState.getDistrict())).findFirst();
                currentPickedDistrict.ifPresent(d -> getWardsByDistrict(d, false));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getWardsByDistrict(District district, boolean shouldResetText) {
        Future<DistrictDetail> districtDetailFuture = executorService.submit(() -> {
           Call call = countryService.getDistrictDetail(district.getCode());
           Response response = call.execute();
           if(response.code() == 200) {
               return gson.fromJson(response.body().string(), DistrictDetail.class);
           }
           return null;
        });

        try {
            DistrictDetail districtDetail = districtDetailFuture.get();
            if(districtDetail != null) {
                if(shouldResetText) {
                    wardInput.setText("");
                }
                wards.clear();

                wards.addAll(districtDetail.getWards());
                ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                        UpdateUserActivity.this,
                        android.R.layout.simple_list_item_1,
                        wards.stream().map(Place::getName).collect(Collectors.toList()));
                wardInput.setAdapter(districtAdapter);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
