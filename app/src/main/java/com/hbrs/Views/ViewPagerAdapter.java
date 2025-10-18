package com.hbrs.Views;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Add a fragment and its title
    public void addFragment(@NonNull Fragment fragment, @NonNull String title) {
        fragmentList.add(fragment);
        fragmentTitles.add(title);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public String getTitle(int position) {
        return fragmentTitles.get(position);
    }

    // Broadcast speed changes to all fragments
    public void setMaxSpeed(int speed) {
        for (Fragment fragment : fragmentList) {
            if (fragment instanceof SpeedUpdatable) {
                ((SpeedUpdatable) fragment).updateMaxSpeed(speed);
            }
        }
    }
}