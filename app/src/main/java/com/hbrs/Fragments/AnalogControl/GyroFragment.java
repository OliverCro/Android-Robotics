package com.hbrs.Fragments.AnalogControl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbrs.ORB.ORBManager;
import com.hbrs.R;
import com.hbrs.Views.GyroView;

public class GyroFragment extends Fragment implements SpeedUpdatable {

    private int maxSpeed;
    private GyroView gyroView;
    private TextView tv_displacement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gyro, container,false);

        maxSpeed = getResources().getInteger(R.integer.Speed_START);

        // Get elements from view
        tv_displacement = view.findViewById(R.id.tv_displacement);
        gyroView = view.findViewById(R.id.gyroView);

        gyroView.start();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(gyroView != null) {
            gyroView.setOnControlMoveListener((x, y) -> {
                float leftSpeed = y + x;
                float rightSpeed = y - x;

                int leftMotor = (int) (leftSpeed * maxSpeed);
                int rightMotor = (int) (rightSpeed * maxSpeed);

                ORBManager.move(String.format("Gyro : X= %.3f Y= %.3f", x, y), leftMotor, rightMotor);

                requireActivity().runOnUiThread(() ->
                        tv_displacement.setText(String.format("X: %.3f | Y: %.3f", x, y))
                );
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(gyroView != null) {
            gyroView.setOnControlMoveListener(null);
        }
    }

    @Override
    public void onDestroy() {;
        super.onDestroy();
    }

    @Override
    public void updateMaxSpeed(int speed) {
        maxSpeed = speed;
    }
}
