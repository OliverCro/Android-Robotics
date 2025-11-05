package com.hbrs.Fragments.AnalogControl;

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
import com.hbrs.Adapter.ViewPagerAdapter;

public class AnalogControlFragment extends Fragment {

    private SeekBar seekBarSpeed;
    private TextView tvMaxSpeed;
    private TextView tvMinSpeed;
    private TextView tvCurrentSpeed;

    private int currentSpeed; // Default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_analog_control, container, false);

        // Get instances
        tvMinSpeed = view.findViewById(R.id.tv_min_speed);
        tvCurrentSpeed = view.findViewById(R.id.tv_current_speed);
        tvMaxSpeed = view.findViewById(R.id.tv_max_speed);
        seekBarSpeed = view.findViewById(R.id.seekbar_speed);
        TabLayout tabLayout = view.findViewById(R.id.tl_analog_control);
        ViewPager2 viewPager = view.findViewById(R.id.vp_analog_control);

        // Set initial Min, Max and current Speed strings
        tvMaxSpeed.setText(String.format("%,d", getResources().getInteger(R.integer.Speed_MAX)));
        tvMinSpeed.setText(String.format("%,d", getResources().getInteger(R.integer.Speed_MIN)));
        currentSpeed = getResources().getInteger(R.integer.Speed_START);

        // Adapter Setup
        ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity());

        // Add tabs with fragments
        adapter.addFragment(new JoystickFragment(), "Joystick");
        adapter.addFragment(new ButtonsFragment(), "Buttons");
        adapter.addFragment(new GyroFragment(), "Gyro");

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
                currentSpeed = progress;
                tvCurrentSpeed.setText("Max speed: " + String.format("%,d", currentSpeed));

                // Notify fragments about speed change
                adapter.setMaxSpeed(currentSpeed);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set initial current speed
        adapter.setMaxSpeed(currentSpeed);
        tvCurrentSpeed.setText("Max speed: " + String.format("%,d", currentSpeed));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
