package com.agrinetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.agrinetwork.components.NavigationBar;

import android.widget.Button;

import com.agrinetwork.config.Variables;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agrinetwork.databinding.ActivityUserFeedBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class UserFeedActivity extends AppCompatActivity {

    private ActivityUserFeedBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View viewFragment = findViewById(R.id.nav_host_fragment_activity_user_feed);
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        viewFragment.setPadding(0,0,0,bottomAppBar.getHeight());

        firebaseAuth = FirebaseAuth.getInstance();

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



        View viewNavigation = findViewById(R.id.navigation_bar);
        viewNavigation.setOnClickListener(v ->{
            startActivity((new Intent(this, ProfileMangerActivity.class)));
        });





        Button logoutBtn = binding.logoutBtn;
        logoutBtn.setOnClickListener(v -> {
            firebaseAuth.signOut();
            SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(Variables.ID_TOKEN_LABEL);
            editor.apply();

            startActivity(new Intent(UserFeedActivity.this, MainActivity.class));
        });

    }

}