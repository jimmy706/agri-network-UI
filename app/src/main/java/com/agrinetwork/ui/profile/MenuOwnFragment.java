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

import androidx.fragment.app.Fragment;

import com.agrinetwork.MainActivity;
import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MenuOwnFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_USER_ID = "userId";
    private String title;
    private TextView linkPageUserWall;
    private Button logOut;
    private String userWallId;
    private FirebaseMessaging firebaseMessaging;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPref;

    public MenuOwnFragment(){

    }

    public static MenuOwnFragment newInstance(String title, String userId){
        MenuOwnFragment menuOwnFragment = new MenuOwnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_USER_ID, userId);
        menuOwnFragment.setArguments(args);
        return menuOwnFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            this.title = bundle.getString(ARG_TITLE);
            this.userWallId = bundle.getString(ARG_USER_ID);
        }
    }

    @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate){
        View root = inflater.inflate(R.layout.fragment_menu_own, container, false);

        linkPageUserWall = root.findViewById(R.id.menu_own_profile);
        logOut = root.findViewById(R.id.menu_own_logout);
        sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();

        linkPageUserWall.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), UserWallActivity.class);
            intent.putExtra("userId", userWallId);
            startActivity(intent);
        });

        logOut.setOnClickListener(p->{
            firebaseMessaging.unsubscribeFromTopic("add_friend_to_" + userWallId);
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
