package com.agrinetwork;


import android.content.Intent;

import android.os.Bundle;

import android.view.View;


import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agrinetwork.databinding.ActivityUserFeedBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class UserFeedActivity extends AppCompatActivity {

    private ActivityUserFeedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            startActivity(new Intent(this, CreatePostActivity.class));
        });


    }

}