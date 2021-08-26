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

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.UserTypes;
import com.agrinetwork.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Register extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserService userService;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        userService = new UserService(this);

        TextInputEditText emailInput = findViewById(R.id.edit_text_email);
        TextInputEditText phoneNumberInput = findViewById(R.id.edit_text_phone);
        TextInputEditText passwordInput = findViewById(R.id.edit_text_password);
        TextInputEditText firstNameInput = findViewById(R.id.edit_text_first_name);
        TextInputEditText lastNameInput = findViewById(R.id.edit_text_last_name);
        MaterialAutoCompleteTextView userTypeInput = findViewById(R.id.edit_text_usertype);

        List<String> userTypes = Arrays.stream(UserTypes.values()).map(UserTypes::getLabel).collect(Collectors.toList());
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userTypes);
        userTypeInput.setAdapter(userTypeAdapter);

        MaterialButton registerButton = findViewById(R.id.register_btn);
        TextView signInLink = findViewById(R.id.move_to_sign_in);
        signInLink.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String phoneNumber = phoneNumberInput.getText().toString();
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String userType = userTypeInput.getText().toString();

            User user = new User();
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setType(userType);

            requestAddUser(user);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Toast.makeText(Register.this, "User created",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Register.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Log.w("RegisterFailed", "signUpWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, task.getException().getMessage(),
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
}