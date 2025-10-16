package com.hbrs.Views;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private int maxSpeed = 1000;
    private JoystickFragment joystickFragment;
    private ButtonsFragment buttonsFragment;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        joystickFragment = new JoystickFragment();
        buttonsFragment = new ButtonsFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return joystickFragment;
        else return buttonsFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void setMaxSpeed(int speed) {
        this.maxSpeed = speed;
        joystickFragment.updateMaxSpeed(speed);
        buttonsFragment.updateMaxSpeed(speed);
    }
}
