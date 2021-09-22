package com.agrinetwork.ui.networks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.agrinetwork.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class MyNetworkFragment extends Fragment {
    private static final int NUM_PAGES = 3;
    private static final int[] TAB_TITLES = {R.string.recommend_users_tab_title, R.string.following_users_tab_title, R.string.follower_tab_title};
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_my_network, container, false);

        viewPager = root.findViewById(R.id.pager);
        pagerAdapter = new MyNetworkSliderPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        tabLayout = root.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(getString(TAB_TITLES[position]));
        }).attach();

        return root;
    }

    private class MyNetworkSliderPagerAdapter extends FragmentStateAdapter {


        public MyNetworkSliderPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String title = getString(TAB_TITLES[position]);
            switch (position) {
                case 1:
                    return MyFollowFragment.newInstance(title, FollowTabFragmentType.FOLLOWINGS);
                case 2:
                    return MyFollowFragment.newInstance(title, FollowTabFragmentType.FOLLOWERS);
                default:
                    return RecommendUsersFragment.newInstance(title);
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}