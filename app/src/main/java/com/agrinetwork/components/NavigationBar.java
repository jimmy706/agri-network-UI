package com.agrinetwork.components;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.agrinetwork.MainActivity;
import com.agrinetwork.ProfileMangerActivity;
import com.agrinetwork.R;
import com.agrinetwork.RegisterActivity;
import com.agrinetwork.UserFeedActivity;

public class NavigationBar extends RelativeLayout {


    public NavigationBar(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.navigation_bar, this);
        ImageView avatar = this.findViewById(R.id.avatar_user);
        EditText search = this.findViewById(R.id.search_text);

//        avatar.setOnClickListener(v ->{
//            context.startActivity((new Intent(v.getContext(), NavigationBar.class)));
//        });


    }



}
