package com.agrinetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Province;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.ProvinceService;
import com.agrinetwork.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateUserActivity extends AppCompatActivity {

    private UserService userService;
    private ProvinceService provinceService;
    private TextInputEditText emailText,  firstNameInput, lastNameInput,phoneNumberInput;
    private MaterialAutoCompleteTextView provinceInput;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        userService = new UserService(this);
        provinceService = new ProvinceService(this);

        Intent intent = this.getIntent();
        String id =  intent.getExtras().getString("id");



        MaterialToolbar iconBack = findViewById(R.id.backToWall);
        iconBack.setNavigationOnClickListener(view -> {
            startActivity(new Intent(this, UserFeedActivity.class));
        });

        emailText = findViewById(R.id.display_email);
        firstNameInput = findViewById(R.id.update_first_name);
        lastNameInput = findViewById(R.id.update_last_name);
        phoneNumberInput = findViewById(R.id.update_phone);
        provinceInput = findViewById(R.id.update_province);
        getAllProvinces(provinceInput);


        fetchUserBeforeUpdate(id);

        MaterialButton updateButton = findViewById(R.id.update_btn);
        updateButton.setOnClickListener(v -> {

            SharedPreferences sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
            String token =  sharedPreferences.getString(Variables.ID_TOKEN_LABEL,"");

            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String phoneNumber = phoneNumberInput.getText().toString();

            String province = provinceInput.getText().toString();


            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);

            user.setProvince(province);
            requestUpdateUser(user,token);

            Toast  successful= Toast.makeText(this, "Cập nhật tài khoản thành công", Toast.LENGTH_LONG);
            successful.show();


        }

        );
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

        Call getbyId = userService.getbyId(id);
        getbyId.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getUserLogin, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call getbyId, @NonNull Response response) throws IOException {
                Gson gson = new Gson();
                String jsonData = response.body().string();
                User user = gson.fromJson(jsonData, User.class);
                UpdateUserActivity.this.runOnUiThread(()-> {
                    renderBeforeUpdate(user);
                });

            }
        });


    }

    private void  renderBeforeUpdate(User user){

            String emailAddress = user.getEmail();
            emailText.setText(emailAddress);
            emailText.setFocusable(false);

            String firstName = user.getFirstName();
            firstNameInput.setText(firstName);

            String lastName = user.getLastName();
            lastNameInput.setText(lastName);

            String phoneNumber = user.getPhoneNumber();
            phoneNumberInput.setText(phoneNumber);


            String province = user.getProvince();
            provinceInput.setText(province);
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
                Gson gson = new Gson();
                Province[] provinces = gson.fromJson(response.body().string(), Province[].class);
                List<String> provinceNames = new ArrayList<>();
                for(Province p : provinces) {
                    provinceNames.add(p.getName());
                }

                UpdateUserActivity.this.runOnUiThread(()-> {
                    ArrayAdapter<String> provincesAdapter = new ArrayAdapter<>(UpdateUserActivity.this, android.R.layout.simple_list_item_1, provinceNames);
                    provinceInput.setAdapter(provincesAdapter);
                });
            }
        });
    }


}
