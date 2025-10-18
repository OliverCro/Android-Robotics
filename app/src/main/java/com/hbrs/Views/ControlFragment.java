package com.hbrs.Views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hbrs.R;

public class ControlFragment extends Fragment {

    private SeekBar seekBarSpeed;
    private TextView tvMaxSpeed;

    private int maxSpeed = 1000; // Default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_control, container, false);

        // Get instances
        tvMaxSpeed = view.findViewById(R.id.tv_max_speed);
        seekBarSpeed = view.findViewById(R.id.seekbar_speed);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        // Adapter Setup
        ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity());

        // Add tabs with fragments
        adapter.addFragment(new JoystickFragment(), "Joystick");
        adapter.addFragment(new ButtonsFragment(), "Buttons");

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        // Link TabLayout and ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> { tab.setText(adapter.getTitle(position));}
        ).attach();

        // Speed slider listener
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxSpeed = progress;
                tvMaxSpeed.setText("Max speed: " + maxSpeed);

                // Notify fragments about speed change
                adapter.setMaxSpeed(maxSpeed);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
