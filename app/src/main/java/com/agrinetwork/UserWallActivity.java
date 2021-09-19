package com.agrinetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserWallActivity extends AppCompatActivity {
    private UserService userService;
    private ImageView avatarProfile;
    private TextView userName, province, contact, email;
    private Button btnEdit;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_user);
        userService = new UserService(this);
        btnEdit = findViewById(R.id.btn_edit);

        Intent intent = getIntent();
        String userId =  intent.getExtras().getString("userId");

        SharedPreferences sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        String userIdLogin =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID,"");


        if(userId.equals(userIdLogin)){

            btnEdit.setVisibility(View.VISIBLE);

        }else{
            btnEdit.setVisibility(View.INVISIBLE);

        }


        MaterialToolbar iconBack = findViewById(R.id.back);
        iconBack.setNavigationOnClickListener(view -> {

            startActivity(new Intent(this, UserFeedActivity.class));
        });


         btnEdit.setOnClickListener(v ->{

             Intent intentNew = new Intent(this,UpdateUserActivity.class);
             intentNew.putExtra("id",userId);
             startActivity(intentNew);


         });


        avatarProfile = findViewById(R.id.avatar_profile);
        userName = findViewById(R.id.user_name);
        province = findViewById(R.id.province);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);

        fetchUserDetail(userId);
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
                UserWallActivity.this.runOnUiThread(()-> {
                    renderData(user);
                });

            }
        });

    }

    private void  renderData(User user){
      String fullName = user.getFirstName() + " " + user.getLastName();
      userName.setText(fullName);

      String textProvince = user.getProvince();
      province.setText(textProvince);

      String textContact = user.getPhoneNumber();
      contact.setText(textContact);

      String emailAddress = user.getEmail();
      email.setText(emailAddress);

      String urlImg = user.getAvatar();
      if(urlImg != null && !urlImg.isEmpty()) {
          Picasso.get().load(urlImg).into(avatarProfile);
      }

    }

}
