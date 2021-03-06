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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.UserDetail;
import com.agrinetwork.service.MediaService;
import com.agrinetwork.service.UserService;
import com.agrinetwork.ui.profile.MenuOwnFragment;
import com.agrinetwork.ui.profile.OwnPlansFragment;
import com.agrinetwork.ui.profile.OwnPostFragment;
import com.agrinetwork.ui.profile.OwnProductFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class UserWallActivity extends AppCompatActivity {
    private static final int[] TAB_TITLES = {R.string.tab_own_post, R.string.tab_own_product, R.string.plan};
    private static final int NUM_PAGES = 3;

    private UserService userService;
    private SharedPreferences sharedPreferences;

    private ImageView avatarProfile;
    private TextView userName, userLocation, contact, email, countFollower, countFollowing, countFriend;
    private MaterialButton btnEdit, btnApproveFriendRq, btnRejectFriendRq, btnAddFriend, btnContact;
    private ToggleButton followBtn;
    private LinearLayout friendRequestWrapper, interactBtnWrapper;
    private RelativeLayout friendCounterWrapper,followingCounterWrapper,followerCounterWrapper;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FragmentStateAdapter stateAdapter;
    private String userId;


    private boolean isOwner;
    private UserDetail user;
    private String token;
    private MediaService mediaService;

    private final String  FOLLOWINGS = "FOLLOWINGS";
    private final String  FOLLOWERS = "FOLLOWERS";

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
        userId =  intent.getExtras().getString("userId");

        sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
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

        MaterialToolbar toolbar = findViewById(R.id.back);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });


         btnEdit.setOnClickListener(v ->{

             Intent intentNew = new Intent(this,UpdateUserActivity.class);
             intentNew.putExtra("id", userId);
             startActivity(intentNew);
         });

        avatarProfile = findViewById(R.id.avatar_profile);
        userName = findViewById(R.id.user_name);
        userLocation = findViewById(R.id.location);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);
        countFollower = findViewById(R.id.count_follower);
        countFollowing = findViewById(R.id.count_following);
        countFriend = findViewById(R.id.count_friend);
        friendRequestWrapper = findViewById(R.id.friend_request_wrapper);
        friendCounterWrapper = findViewById(R.id.friend_wrapper);
        btnApproveFriendRq = findViewById(R.id.approve_friend_rq);
        btnRejectFriendRq = findViewById(R.id.reject_friend_rq);
        btnAddFriend = findViewById(R.id.add_friend_btn);
        btnContact = findViewById(R.id.contact_btn);
        followingCounterWrapper = findViewById(R.id.following_wrapper);
        followerCounterWrapper = findViewById(R.id.follower_wrapper);

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

        friendCounterWrapper.setOnClickListener(v -> {
            Intent intentFriends = new Intent(this, FriendsActivity.class);
            intentFriends.putExtra("userId", userId);
            startActivity(intentFriends);
        });

        followingCounterWrapper.setOnClickListener(v ->{
            Intent intentFollowing = new Intent(this,FollowActivity.class);
            intentFollowing.putExtra("type",FOLLOWINGS);
            intentFollowing.putExtra("userId", userId);
            startActivity(intentFollowing);
        });

        followerCounterWrapper.setOnClickListener(v->{
            Intent intentFollower = new Intent(this,FollowActivity.class);
            intentFollower.putExtra("type",FOLLOWERS);
            intentFollower.putExtra("userId", userId);
            startActivity(intentFollower);
        });

        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab);
        stateAdapter = new UserWallPageAdapter(this);
        viewPager.setAdapter(stateAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(getString(TAB_TITLES[position]));
        }).attach();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
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
                        Toast.makeText(UserWallActivity.this, "???? h???y k???t b???n", Toast.LENGTH_SHORT).show();
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
      String textDistrict = user.getDistrict();
      String textWard = user.getWard();
      userLocation.setText(textWard +", " + textDistrict + ", " + textProvince);

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
                        Toast.makeText(UserWallActivity.this, "C???p nh???t avatar th??nh c??ng", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Variables.CURRENT_LOGIN_USER_AVATAR, user.getAvatar());
                        editor.apply();
                    }
                    else {
                        Toast.makeText(UserWallActivity.this, "???? g???p l???i x???y ra", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(UserWallActivity.this, "B???n ???? th??nh b???n b?? c???a " + user.getLastName(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(UserWallActivity.this, "B???n ???? t??? ch???i l???i m???i k???t b???n", Toast.LENGTH_SHORT).show();
                    renderData();
                });
            }
        });
    }

    private class UserWallPageAdapter extends FragmentStateAdapter {

        public UserWallPageAdapter(@NonNull FragmentActivity fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String title = getString(TAB_TITLES[position]);
            switch (position) {
                case 0:
                    return OwnPostFragment.newInstance(title, userId);
                case 1:
                    return OwnProductFragment.newInstance(title, userId);
                case 2:
                    return OwnPlansFragment.newInstance(userId);
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

}
