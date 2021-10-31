package com.agrinetwork.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.agrinetwork.R;

import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.UserDetail;
import com.agrinetwork.service.UserService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ProfileMenuFragment extends Fragment {
    private static final int NUM_PAGES = 4;
    private static final int[] TAB_TITLES = {R.string.tab_menu_own, R.string.tab_own_product, R.string.tab_own_post, R.string.plan};
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;
    private TextView fullName, countFollower,countFollowing,countFriend;
    private CircleImageView avatar;
    private UserService userService;
    private String token;
    private String currentLoginUserId;
    private Gson gson;
    private UserDetail user;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_profile_menu, container, false);
        viewPager = root.findViewById(R.id.pager);
        pagerAdapter = new ProfileMenuPageAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = root.findViewById(R.id.tab);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(getString(TAB_TITLES[position]));
        }).attach();


        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        currentLoginUserId = sharedPref.getString(Variables.CURRENT_LOGIN_USER_ID, "");
        userService = new UserService(getContext());
        avatar = root.findViewById(R.id.avatar_profile);
        fullName = root.findViewById(R.id.full_name_in_profile);
        countFollower = root.findViewById(R.id.count_follower);
        countFollowing = root.findViewById(R.id.count_following);
        countFriend = root.findViewById(R.id.count_friend);
        personalInformation();


        avatar.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), UserWallActivity.class);
            intent.putExtra("userId",currentLoginUserId);
            startActivity(intent);
        });

        return root;


    }

    private class ProfileMenuPageAdapter extends FragmentStateAdapter {

        public ProfileMenuPageAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String title = getString(TAB_TITLES[position]);
            switch (position) {
                case 0:
                    return MenuOwnFragment.newInstance(title);
                case 1:
                    return OwnProductFragment.newInstance(title);
                case 2:
                    return OwnPostFragment.newInstance(title);
                case 3:
                    return OwnPlansFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    public void personalInformation(){
        Call getInformationOwn = userService.getById(token,currentLoginUserId);
        getInformationOwn.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                gson = new Gson();
                String dataResponse = response.body().string();
                user = gson.fromJson(dataResponse,UserDetail.class);
                getActivity().runOnUiThread(()->{
                    renderData();
                });
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void renderData(){
        String textFullName = user.getFirstName() + " " + user.getLastName();
        fullName.setText(textFullName);

        countFollower.setText(Integer.toString(user.getNumberOfFollowers()));
        countFollowing.setText(Integer.toString(user.getNumberOfFollowings()));
        countFriend.setText(Integer.toString(user.getNumberOfFriends()));

        String textUrlAvatar = user.getAvatar();
        if(textUrlAvatar != null && !textUrlAvatar.isEmpty()){
            Picasso.get().load(textUrlAvatar)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(avatar);
        }

    }
}