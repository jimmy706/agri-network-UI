package com.agrinetwork;



import android.content.Intent;


import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.agrinetwork.service.UserService;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agrinetwork.databinding.ActivityUserFeedBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

@RequiresApi(api = Build.VERSION_CODES.O)
public class UserFeedActivity extends AppCompatActivity {

    private ActivityUserFeedBinding binding;
    private UserService userService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(this);
        binding = ActivityUserFeedBinding.inflate(getLayoutInflater());
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
}