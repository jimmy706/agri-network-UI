package com.agrinetwork.components;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.agrinetwork.MainActivity;
import com.agrinetwork.SearchUserActivity;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.R;

import com.agrinetwork.config.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

public class NavigationBar extends RelativeLayout {
    private FirebaseAuth firebaseAuth;
    private FirebaseMessaging firebaseMessaging;
    private final Context context;
    private final SharedPreferences sharedPreferences;

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.navigation_bar, this);
        ImageView avatar = this.findViewById(R.id.avatar_user);
        EditText search = this.findViewById(R.id.search_text);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();

        String avatarUrl =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_AVATAR,"");
        if(avatarUrl != null && !avatarUrl.isEmpty()) {
            Picasso.get()
                    .load(avatarUrl)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(avatar);
        }

        PopupMenu popupMenu = new PopupMenu(context, avatar);
        popupMenu.getMenuInflater().inflate(R.menu.user_profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if(itemId == R.id.view_profile){
                String userId =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID,"");
                Intent intent = new Intent(context, UserWallActivity.class);
                intent.putExtra("userId",userId);
                context.startActivity(intent);
            }
            else if(itemId == R.id.logout) {
                logout();
            }
            return true;
        });

        avatar.setOnClickListener(v ->{
            popupMenu.show();
         });

        search.setOnEditorActionListener((textView, id, keyEvent) -> {
            String searchText= textView.getText().toString();
            if(id == EditorInfo.IME_ACTION_SEARCH) {
                handleSearch(searchText);
                return true;
            }
            return false;
        });


    }

    private void logout() {
        String currentUserId =  sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID,"");
        firebaseMessaging.unsubscribeFromTopic("add_friend_to_" + currentUserId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Variables.CURRENT_LNG_LOCATION);
        editor.remove(Variables.CURRENT_LAT_LOCATION);
        editor.apply();
        firebaseAuth.signOut();
        context.startActivity(new Intent(context,MainActivity.class));
    }

    private void handleSearch(String searchText) {
        Intent intentSearch = new Intent(context,SearchUserActivity.class);
        intentSearch.putExtra("search",searchText);
        context.startActivity(intentSearch);
    }

}
