package com.agrinetwork.components;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.agrinetwork.MainActivity;
import com.agrinetwork.ProfileMangerActivity;
import com.agrinetwork.R;
import com.agrinetwork.RegisterActivity;
import com.agrinetwork.UserFeedActivity;
import com.agrinetwork.config.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.jar.Attributes;

public class NavigationBar extends RelativeLayout {
    FirebaseAuth firebaseAuth;


    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.navigation_bar, this);
        ImageView avatar = this.findViewById(R.id.avatar_user);
        EditText search = this.findViewById(R.id.search_text);
        Button logOutBtn = this.findViewById(R.id.btn_logout);

        SharedPreferences sharedPreferencesAvatar = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        String avatarUrl =  sharedPreferencesAvatar.getString("currentLoginUserAvatar","");
        Picasso.get().load(avatarUrl).into(avatar);



        avatar.setOnClickListener(v ->{
             SharedPreferences sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
            String userId =  sharedPreferences.getString("currentLoginUserId","");

             Intent intent = new Intent(context,ProfileMangerActivity.class);
            intent.putExtra("userId",userId);
             context.startActivity(intent);

         });


        firebaseAuth = FirebaseAuth.getInstance();
        logOutBtn.setOnClickListener(v ->{
                firebaseAuth.signOut();
                context.startActivity(new Intent(context,MainActivity.class));
        });

    }

}
