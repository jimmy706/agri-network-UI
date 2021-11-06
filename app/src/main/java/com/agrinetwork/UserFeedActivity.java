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

import com.agrinetwork.components.dialog.NotifyExpiredPlanDialog;
import com.agrinetwork.components.dialog.PickCreatePlanSourceDialog;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.plan.PlanStatus;
import com.agrinetwork.service.PlanService;
import com.agrinetwork.service.UserService;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.agrinetwork.databinding.ActivityUserFeedBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class UserFeedActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION_CODE = 99;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ActivityUserFeedBinding binding;
    private UserService userService;
    private PlanService planService;
    private String token;
    private FirebaseMessaging firebaseMessaging;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(this);
        planService = new PlanService(this);
        binding = ActivityUserFeedBinding.inflate(getLayoutInflater());

        sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        firebaseMessaging = FirebaseMessaging.getInstance();

        Intent activityIntent = getIntent();
        int tab = -1;
        if(activityIntent.getExtras() != null) {
            tab = activityIntent.getExtras().getInt("tab", -1);
        }

        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_user_feed);
        NavigationUI.setupWithNavController(navView, navController);
        if(tab != -1) {
            navController.navigate(tab);
        }

        btnAddPost.setOnClickListener(v -> {
            showBottomSheetAddNewPost();
        });

        getMyExpiredPlans();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_REQUEST_LOCATION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                double lng = location.getLongitude();
                double lat = location.getLatitude();
                com.agrinetwork.entities.Location newLocation = new com.agrinetwork.entities.Location(lat, lng);
                updateLocation(newLocation);
            }
        }

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
            startActivity(new Intent(this,CreateProductActivity.class));
        });

        LinearLayout addProductRequestPost = bottomSheetDialog.findViewById(R.id.add_request_product);
        addProductRequestPost.setOnClickListener(v -> {
            startActivity(new Intent(this, RequestProductActivity.class));
        });

        LinearLayout addPlanningPost = bottomSheetDialog.findViewById(R.id.add_planning_post);
        addPlanningPost.setOnClickListener(v -> {
            new PickCreatePlanSourceDialog(this).show();
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
            getCurrentLocation();
        }
    }

    private void updateLocation(com.agrinetwork.entities.Location newLocation) {
        double lat = Double.parseDouble(sharedPreferences.getString(Variables.CURRENT_LAT_LOCATION, "-360"));
        double lng = Double.parseDouble(sharedPreferences.getString(Variables.CURRENT_LNG_LOCATION, "-360"));
        com.agrinetwork.entities.Location currentLocation = new com.agrinetwork.entities.Location(lat, lng);
        if(!currentLocation.isValid() || !currentLocation.equals(newLocation)) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                Call call = userService.updateLocation(token, newLocation);
                try {
                    Response response = call.execute();
                    if(response.code() == 200) {
                       UserFeedActivity.this.runOnUiThread(() -> {
                           SharedPreferences.Editor editor = sharedPreferences.edit();
                           editor.putString(Variables.CURRENT_LAT_LOCATION, Double.toString(newLocation.getLat()));
                           editor.putString(Variables.CURRENT_LNG_LOCATION, Double.toString(newLocation.getLng()));
                           editor.apply();
                       });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void getMyExpiredPlans() {
        Future<List<Plan>> future = executor.submit(() -> {
            String userId = sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID, "");

            PlanService.SearchPlanCriteria criteria = new PlanService.SearchPlanCriteria(true);
            criteria.setOwner(userId);
            Call call = planService.searchPlan(token, criteria);
            Response response = call.execute();
            if(response.code() == 200) {
                Type type = new TypeToken<List<Plan>>(){}.getType();
                Gson gson = new Gson();
                return gson.fromJson(response.body().string(), type);
            }
            return null;
        });

        try {
            List<Plan> plans = future.get();
            if (plans != null && !plans.isEmpty()) {
                System.out.println(Arrays.toString(plans.toArray()));
                int countExpiredButNotHarvest = 0;
                for (Plan plan : plans) {
                    if (PlanStatus.EXPIRED.getLabel().equals(plan.getStatus())) {
                        countExpiredButNotHarvest++;
                    }
                }
                if (countExpiredButNotHarvest > 0) {
                    String message = "Bạn có " + countExpiredButNotHarvest + " kế hoạch đã xong, hãy tạo sản phẩm thu hoạch!";
                    NotifyExpiredPlanDialog notifyExpiredPlanDialog = new NotifyExpiredPlanDialog(this, plans, message);
                    notifyExpiredPlanDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}