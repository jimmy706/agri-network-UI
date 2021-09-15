package com.agrinetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProfileMangerActivity extends AppCompatActivity {
    private UserService userService;
    private ImageView avatarProfile;
    private TextView userName, provice, contact, email;
    private Button btnEdit, btnRemove;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_user);
        userService = new UserService(this);

        Intent intent = getIntent();
        String id =  intent.getExtras().getString("userId");
        fetchUserDetail(id);


        MaterialToolbar iconBack = findViewById(R.id.back);
        iconBack.setNavigationOnClickListener(view -> {
            startActivity(new Intent(this, UserFeedActivity.class));
        });

        avatarProfile = findViewById(R.id.avatar_profile);
        userName = findViewById(R.id.user_name);
        provice = findViewById(R.id.provice);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);

        fetchUserDetail(id);
    }

    private  void fetchUserDetail(String id){
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
                ProfileMangerActivity.this.runOnUiThread(()-> {
                    renderData(user);
                });

            }
        });

    }

    private void  renderData(User user){
      String fullName = user.getFirstName() + " " + user.getLastName();
      userName.setText(fullName);

      String textProvice = user.getProvince();
      provice.setText(textProvice);

      String textContact = user.getPhoneNumber();
      contact.setText(textContact);

      String emailAddress = user.getEmail();
      email.setText(emailAddress);

      String urlImg = user.getAvatar();
        Picasso.get().load(urlImg).into(avatarProfile);

    }

}
