package com.agrinetwork;



import android.Manifest;
import android.content.Context;
import android.content.Intent;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.agrinetwork.config.Variables;
import com.agrinetwork.service.UserService;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agrinetwork.databinding.ActivityUserFeedBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

@RequiresApi(api = Build.VERSION_CODES.O)
public class UserFeedActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION_CODE = 99;

    private ActivityUserFeedBinding binding;
    private UserService userService;
    private FirebaseMessaging firebaseMessaging;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(this);
        binding = ActivityUserFeedBinding.inflate(getLayoutInflater());

        sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        firebaseMessaging = FirebaseMessaging.getInstance();
        subscribeCurrentUserToMessagingTopics();
        requestAccessLocation();

        setContentView(binding.getRoot());
        View viewFragment = findViewById(R.id.nav_host_fragment_activity_user_feed);
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        viewFragment.setPadding(0,0,0,bottomAppBar.getHeight());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().getItem(2).setEnabled(false); // Disable center item so that floating button can click


        FloatingActionButton btnAddPost = findViewById(R.id.btn_add_post);
        btnAddPost.bringToFront();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_networks, R.id.navigation_products, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_user_feed);
        NavigationUI.setupWithNavController(navView, navController);

        btnAddPost.setOnClickListener(v -> {
            showBottomSheetAddNewPost();
        });


    }

    private void showBottomSheetAddNewPost() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.add_post_bottom_sheet_popup);

        LinearLayout addRegularPost = bottomSheetDialog.findViewById(R.id.add_regular_post);
        addRegularPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        LinearLayout addSellPost = bottomSheetDialog.findViewById(R.id.add_sell_post);
        addSellPost.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm sản phẩm", Toast.LENGTH_SHORT).show();
        });

        LinearLayout addPlanningPost = bottomSheetDialog.findViewById(R.id.add_planning_post);
        addPlanningPost.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm kế hoạch sản xuất", Toast.LENGTH_SHORT).show();
        });
        bottomSheetDialog.show();
    }

    private void subscribeCurrentUserToMessagingTopics() {
        String currentUserId =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID,"");
        firebaseMessaging.subscribeToTopic("add_friend_to_" + currentUserId);
    }



    private void requestAccessLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.message_location_permission)
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                            ActivityCompat.requestPermissions(UserFeedActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_LOCATION_CODE);
                        });

                AlertDialog permissionDialog = alertBuilder.create();
                permissionDialog.show();
            }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION_CODE);
            }
        }
        else {
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double lng = location.getLongitude();
            double lat = location.getLatitude();

            Toast.makeText(this, "Lat: " + lat + ", long: " + lng , Toast.LENGTH_SHORT).show();
        }
    }
}