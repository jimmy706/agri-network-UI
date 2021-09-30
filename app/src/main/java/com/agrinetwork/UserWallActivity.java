package com.agrinetwork;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

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
    private TextView userName, province, contact, email, countFollower, countFollowing, countFriend;
    private MaterialButton btnEdit, btnApproveFriendRq, btnRejectFriendRq, btnAddFriend, btnContact;
    private ToggleButton followBtn;
    private LinearLayout friendRequestWrapper, interactBtnWrapper;

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

        interactBtnWrapper = findViewById(R.id.interact_buttons_wrapper);
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
             intentNew.putExtra("id", userId);
             startActivity(intentNew);
         });

        avatarProfile = findViewById(R.id.avatar_profile);
        userName = findViewById(R.id.user_name);
        province = findViewById(R.id.province);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);
        countFollower = findViewById(R.id.count_follower);
        countFollowing = findViewById(R.id.count_following);
        countFriend = findViewById(R.id.count_friend);
        friendRequestWrapper = findViewById(R.id.friend_request_wrapper);
        btnApproveFriendRq = findViewById(R.id.approve_friend_rq);
        btnRejectFriendRq = findViewById(R.id.reject_friend_rq);
        btnAddFriend = findViewById(R.id.add_friend_btn);
        btnContact = findViewById(R.id.contact_btn);

        avatarProfile.setOnClickListener(v -> {
            if(isOwner) {
                Intent pickAvatarIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                pickAvatarIntent.putExtra(Intent.ACTION_PICK, true);
                pickAvatarIntent.setAction(Intent.ACTION_GET_CONTENT);
                pickAvatarResultLauncher.launch(pickAvatarIntent);
            }
        });

        btnApproveFriendRq.setOnClickListener(v -> approveFriendRequest());
        btnRejectFriendRq.setOnClickListener(v -> rejectFriendRequest());

        btnAddFriend.setOnClickListener(v -> {
            if(user.isFriend()) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setMessage(getText(R.string.unfriend_asking));
                dialogBuilder.setPositiveButton(getText(R.string.accept), (dialogInterface, i) -> {
                    unfriend();
                });
                dialogBuilder.setNegativeButton(getText(R.string.cancel), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
            else {
                if(user.isPendingFriendRequest()) {
                    cancelFriendRequest();
                }
                else {
                    sendFriendRequest();
                }
            }
        });

        btnContact.setOnClickListener(v -> {
            Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhoneNumber()));
            startActivity(phoneCallIntent);
        });

        fetchUserDetail(userId);
    }

    private void unfriend() {
        Call call = userService.unfriend(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                UserWallActivity.this.runOnUiThread(()-> {
                    if(response.code() == 200) {
                        user.setFriend(false);
                        renderData();
                        Toast.makeText(UserWallActivity.this, "Đã hủy kết bạn", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void cancelFriendRequest() {
        Call call = userService.cancelFriendRequest(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                UserWallActivity.this.runOnUiThread(() -> {
                    if(response.code() == 200) {
                        user.setPendingFriendRequest(false);
                        renderData();
                    }
                });
            }
        });
    }

    private void sendFriendRequest() {
        Call call = userService.sendFriendRequest(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                UserWallActivity.this.runOnUiThread(() -> {
                    if(response.code() == 200) {
                        user.setPendingFriendRequest(true);
                        renderData();
                    }
                });
            }
        });
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
      countFriend.setText(Integer.toString(user.getNumberOfFriends()));

      followBtn.setChecked(user.isFollowed());

      if(user.isHasFriendRequest()) {
          friendRequestWrapper.setVisibility(View.VISIBLE);
          interactBtnWrapper.setVisibility(View.GONE);

      }
      else {
          friendRequestWrapper.setVisibility(View.GONE);
          if(!isOwner) {
              interactBtnWrapper.setVisibility(View.VISIBLE);
          }
      }

      boolean isPendingFriendRequest = user.isPendingFriendRequest();
      boolean isFriend = user.isFriend();

        if(isFriend) {
         btnAddFriend.setText(getText(R.string.friended));
      }
      else {
          if(isPendingFriendRequest) {
              btnAddFriend.setText(getText(R.string.remove_friend_rq));
          }
          else {
              btnAddFriend.setText(getText(R.string.add_friend));
          }
      }
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

    private void approveFriendRequest() {
        Call call = userService.approveFriendRequest(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                UserWallActivity.this.runOnUiThread(() -> {
                    user.setHasFriendRequest(false);
                    user.setFriend(true);
                    user.setNumberOfFriends(user.getNumberOfFriends() + 1);
                    user.setNumberOfFollowers(user.getNumberOfFollowers() + 1);
                    user.setFollowed(true);
                    Toast.makeText(UserWallActivity.this, "Bạn đã thành bạn bè của " + user.getLastName(), Toast.LENGTH_SHORT).show();
                    renderData();
                });
            }
        });
    }

    private void rejectFriendRequest() {
        Call call = userService.rejectFriendRequest(token, user.get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                UserWallActivity.this.runOnUiThread(() -> {
                    user.setHasFriendRequest(false);
                    user.setFriend(false);
                    Toast.makeText(UserWallActivity.this, "Bạn đã từ chối lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    renderData();
                });
            }
        });
    }

}
