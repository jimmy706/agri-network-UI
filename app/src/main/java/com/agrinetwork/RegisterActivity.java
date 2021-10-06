package com.agrinetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.agrinetwork.entities.District;
import com.agrinetwork.entities.DistrictDetail;
import com.agrinetwork.entities.Place;
import com.agrinetwork.entities.Province;
import com.agrinetwork.entities.ProvinceDetail;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.UserTypes;
import com.agrinetwork.entities.Ward;
import com.agrinetwork.helpers.TextValidator;
import com.agrinetwork.service.CountryService;
import com.agrinetwork.service.ProvinceService;
import com.agrinetwork.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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


public class RegisterActivity extends AppCompatActivity {
    private final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private FirebaseAuth firebaseAuth;
    private UserService userService;
    private ProvinceService provinceService;
    private CountryService countryService;



    private final List<Province>  provinces = new ArrayList<>();
    private final List<District> districts = new ArrayList<>();
    private final List<Ward> wards = new ArrayList<>();

    private TextInputEditText emailInput, phoneNumberInput, passwordInput, firstNameInput, lastNameInput;
    private MaterialAutoCompleteTextView provinceInput, userTypeInput, districtInput, wardInput;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        userService = new UserService(this);
        provinceService = new ProvinceService(this);
        countryService = new CountryService(this);

        emailInput = findViewById(R.id.edit_text_email);
        phoneNumberInput = findViewById(R.id.edit_text_phone);
        passwordInput = findViewById(R.id.edit_text_password);
        firstNameInput = findViewById(R.id.edit_text_first_name);
        lastNameInput = findViewById(R.id.edit_text_last_name);
        districtInput = findViewById(R.id.edit_text_district);
        wardInput = findViewById(R.id.edit_text_ward);

        provinceInput = findViewById(R.id.edit_text_province);
        provinceInput.setOnItemClickListener((adapterView, view, i, l) -> {
            String name = provinceInput.getText().toString();
            Optional<Province> province = provinces.stream().filter(p -> p.getName().equals(name)).findFirst();
            province.ifPresent(value -> getDistrictFromProvince(value.getAreaCode()));
        });

        districtInput.setOnItemClickListener((adapterView, view, i, l) -> {
            String name = districtInput.getText().toString();
            Optional<District> district = districts.stream().filter(d -> d.getName().equals(name)).findFirst();
            district.ifPresent(value -> getWardsFromDistrict(value.getCode()));
        });

        userTypeInput = findViewById(R.id.edit_text_usertype);
        List<String> userTypes = Arrays.stream(UserTypes.values()).map(UserTypes::getLabel).collect(Collectors.toList());
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userTypes);
        userTypeInput.setAdapter(userTypeAdapter);

        MaterialButton registerButton = findViewById(R.id.register_btn);
        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String phoneNumber = phoneNumberInput.getText().toString();
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String userType = userTypeInput.getText().toString();
            String province = provinceInput.getText().toString();
            String district = districtInput.getText().toString();
            String ward = wardInput.getText().toString();

            User user = new User();
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setType(userType);
            user.setProvince(province);
            user.setDistrict(district);
            user.setWard(ward);

            requestAddUser(user);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Tạo tài khoản thành công, đăng nhập để tiếp tục",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            firebaseAuth.signOut(); // Require user login
                            startActivity(intent);
                        }
                        else {
                            Log.w("RegisterFailed", "signUpWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        TextView signInLink = findViewById(R.id.move_to_sign_in);
        signInLink.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        validateFields();
        getAllProvinces(provinceInput);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getDistrictFromProvince(int areaCode) {
        Future<ProvinceDetail> provinceDetailFuture = executorService.submit(() -> {
            Call call = countryService.getProvinceDetail(areaCode);
            Response response = call.execute();
            if(response.code() == 200) {
                return gson.fromJson(response.body().string(), ProvinceDetail.class);
            }
            return null;
        });

        try {
            districts.clear();
            districtInput.setText("");

            ProvinceDetail provinceDetail = provinceDetailFuture.get();
            districts.addAll(provinceDetail.getDistricts());

            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                    RegisterActivity.this,
                    android.R.layout.simple_list_item_1,
                    districts.stream().map(Place::getName).collect(Collectors.toList()));
            districtInput.setAdapter(districtAdapter);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getWardsFromDistrict(int code) {
        Future<DistrictDetail> districtDetailFuture = executorService.submit(() -> {
           Call call = countryService.getDistrictDetail(code);
           Response response = call.execute();
           if(response.code() == 200) {
               return gson.fromJson(response.body().string(), DistrictDetail.class);
           }
           return null;
        });

        try {
            wards.clear();
            wardInput.setText("");

            DistrictDetail districtDetail = districtDetailFuture.get();
            wards.addAll(districtDetail.getWards());

            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                    RegisterActivity.this,
                    android.R.layout.simple_list_item_1,
                    wards.stream().map(Place::getName).collect(Collectors.toList()));
            wardInput.setAdapter(districtAdapter);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void requestAddUser(User user) {
        userService.add(user).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });
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
                Province[] provincesData = gson.fromJson(response.body().string(), Province[].class);
                provinces.addAll(Arrays.asList(provincesData));
                List<String> provinceNames = new ArrayList<>();
                for(Province p : provincesData) {
                    provinceNames.add(p.getName());
                }

                RegisterActivity.this.runOnUiThread(()-> {
                    ArrayAdapter<String> provincesAdapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_list_item_1, provinceNames);
                    provinceInput.setAdapter(provincesAdapter);
                });
            }
        });
    }

    private void validateFields() {
        emailInput.addTextChangedListener(new TextValidator(emailInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.email_required));
                }
            }
        });

        phoneNumberInput.addTextChangedListener(new TextValidator(phoneNumberInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.phone_required));
                }
            }
        });

        passwordInput.addTextChangedListener(new TextValidator(passwordInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.password_required));
                }
            }
        });

        firstNameInput.addTextChangedListener(new TextValidator(firstNameInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.first_name_required));
                }
            }
        });

        lastNameInput.addTextChangedListener(new TextValidator(lastNameInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.last_name_require));
                }
            }
        });

        userTypeInput.addTextChangedListener(new TextValidator(userTypeInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.usertype_require));
                }
            }
        });

        provinceInput.addTextChangedListener(new TextValidator(provinceInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.province_required));
                }
            }
        });
    }
}