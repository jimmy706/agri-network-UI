package com.agrinetwork.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.agrinetwork.MainActivity;
import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MenuOwnFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private String title;
    private TextView linkPageUserWall;
    private Button logOut;
    private String currentLoginUserId;
    private FirebaseMessaging firebaseMessaging;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPref;

    public MenuOwnFragment(){

    }

    public static MenuOwnFragment newInstance(String title){
        MenuOwnFragment menuOwnFragment = new MenuOwnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        menuOwnFragment.setArguments(args);
        return menuOwnFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate){
        View root = inflater.inflate(R.layout.fragment_menu_own, container, false);

        linkPageUserWall = root.findViewById(R.id.menu_own_profile);
        logOut = root.findViewById(R.id.menu_own_logout);
        sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        currentLoginUserId = sharedPref.getString(Variables.CURRENT_LOGIN_USER_ID, "");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();

        linkPageUserWall.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), UserWallActivity.class);
            intent.putExtra("userId",currentLoginUserId);
            startActivity(intent);
        });

        logOut.setOnClickListener(p->{
            firebaseMessaging.unsubscribeFromTopic("add_friend_to_" + currentLoginUserId);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(Variables.CURRENT_LNG_LOCATION);
            editor.remove(Variables.CURRENT_LAT_LOCATION);
            editor.apply();
            firebaseAuth.signOut();
            getContext().startActivity(new Intent(getActivity(), MainActivity.class));
        });



        return root;
    }
}
