package com.agrinetwork.components;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.agrinetwork.MainActivity;
import com.agrinetwork.ProfileMangerActivity;
import com.agrinetwork.R;

import com.agrinetwork.config.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

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
        String avatarUrl =  sharedPreferencesAvatar.getString(Variables.CURRENT_LOGIN_USER_AVATAR,"");
        if(avatarUrl != null && !avatarUrl.isEmpty()) {
            Picasso.get().load(avatarUrl).into(avatar);
        }



        avatar.setOnClickListener(v ->{
             SharedPreferences sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
             String userId =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID,"");

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
