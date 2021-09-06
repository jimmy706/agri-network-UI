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

import com.agrinetwork.entities.Province;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.UserTypes;
import com.agrinetwork.helpers.TextValidator;
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
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserService userService;
    private ProvinceService provinceService;

    private TextInputEditText emailInput, phoneNumberInput, passwordInput, firstNameInput, lastNameInput;

    private MaterialAutoCompleteTextView provinceInput, userTypeInput;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        userService = new UserService(this);
        provinceService = new ProvinceService(this);

        emailInput = findViewById(R.id.edit_text_email);
        phoneNumberInput = findViewById(R.id.edit_text_phone);
        passwordInput = findViewById(R.id.edit_text_password);
        firstNameInput = findViewById(R.id.edit_text_first_name);
        lastNameInput = findViewById(R.id.edit_text_last_name);

        provinceInput = findViewById(R.id.edit_text_province);
        getAllProvinces(provinceInput);


        userTypeInput = findViewById(R.id.edit_text_usertype);
        List<String> userTypes = Arrays.stream(UserTypes.values()).map(UserTypes::getLabel).collect(Collectors.toList());
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userTypes);
        userTypeInput.setAdapter(userTypeAdapter);

        validateFields();

        TextView signInLink = findViewById(R.id.move_to_sign_in);

        signInLink.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        MaterialButton registerButton = findViewById(R.id.register_btn);
        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String phoneNumber = phoneNumberInput.getText().toString();
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String userType = userTypeInput.getText().toString();
            String province = provinceInput.getText().toString();

            User user = new User();
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setType(userType);
            user.setProvince(province);

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
    }

    private void requestAddUser(User user) {
        userService.add(user).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println(response);
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
                Gson gson = new Gson();
                Province[] provinces = gson.fromJson(response.body().string(), Province[].class);
                List<String> provinceNames = new ArrayList<>();
                for(Province p : provinces) {
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