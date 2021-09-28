package com.agrinetwork;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.UserDetail;
import com.agrinetwork.service.MediaService;
import com.agrinetwork.service.UserService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserWallActivity extends AppCompatActivity {
    private UserService userService;
    private ImageView avatarProfile;
    private TextView userName, province, contact, email, countFollower, countFollowing;
    private MaterialButton btnEdit;
    private ToggleButton followBtn;

    private boolean isOwner;
    private UserDetail user;
    private String token;
    private MediaService mediaService;

    private final ActivityResultLauncher<Intent> pickAvatarResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            Uri avatarUri = data.getData();
            if(avatarUri != null) {
                avatarProfile.setImageURI(avatarUri);
                uploadAvatar(avatarUri);
            }
        }
    }) ;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_wall);

        userService = new UserService(this);
        mediaService = new MediaService(this);

        Intent intent = getIntent();
        String userId =  intent.getExtras().getString("userId");

        SharedPreferences sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        String userIdLogin =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID,"");
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        isOwner = userId.equals(userIdLogin);

        followBtn = findViewById(R.id.follow_btn);
        followBtn.setOnClickListener(v -> {
            if(user.isFollowed()) {
                unfollow();
            }
            else {
                follow();
            }
        });

        btnEdit = findViewById(R.id.btn_edit);

        LinearLayout interactBtnWrapper = findViewById(R.id.interact_buttons_wrapper);
        if (isOwner) {
            btnEdit.setVisibility(View.VISIBLE);
            interactBtnWrapper.setVisibility(View.GONE);
        } else {
            btnEdit.setVisibility(View.GONE);
            interactBtnWrapper.setVisibility(View.VISIBLE);
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
        countFollower = findViewById(R.id.count_follower);
        countFollowing = findViewById(R.id.count_following);

        avatarProfile.setOnClickListener(v -> {
            if(isOwner) {
                Intent pickAvatarIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                pickAvatarIntent.putExtra(Intent.ACTION_PICK, true);
                pickAvatarIntent.setAction(Intent.ACTION_GET_CONTENT);
                pickAvatarResultLauncher.launch(pickAvatarIntent);
            }
        });


        fetchUserDetail(userId);
    }

    private  void fetchUserDetail(String id){
        Call getUserByIdCall = userService.getById(token, id);
        getUserByIdCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getUserLogin, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call getbyId, @NonNull Response response) throws IOException {
                Gson gson = new Gson();
                String jsonData = response.body().string();
                user = gson.fromJson(jsonData, UserDetail.class);
                UserWallActivity.this.runOnUiThread(()-> {
                    renderData();
                });

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void  renderData(){
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

      countFollowing.setText(Integer.toString(user.getNumberOfFollowings()));
      countFollower.setText(Integer.toString(user.getNumberOfFollowers()));

      followBtn.setChecked(user.isFollowed());
    }

    private void uploadAvatar(Uri uri) {
        try {
            Call call = mediaService.uploadImage(uri, token);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.code() == 201) {
                        String url = response.body().string();
                        user.setAvatar(url);
                        updateAvatar();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAvatar() {
        Call call = userService.update(user, token);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                UserWallActivity.this.runOnUiThread(()-> {
                    if(response.code() == 200) {
                        Toast.makeText(UserWallActivity.this, "Cập nhật avatar thành công", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(UserWallActivity.this, "Đã gặp lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void follow() {
        Call call = userService.follow(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                   UserWallActivity.this.runOnUiThread(() -> {
                       user.setNumberOfFollowers(user.getNumberOfFollowers() + 1);
                       user.setFollowed(true);
                       renderData();
                   });
                }
            }
        });
    }

    private void unfollow() {
        Call call = userService.unfollow(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    UserWallActivity.this.runOnUiThread(()-> {
                        user.setNumberOfFollowers(user.getNumberOfFollowers() - 1);
                        user.setFollowed(false);
                        renderData();
                    });
                }
            }
        });
    }

}
