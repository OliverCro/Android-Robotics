package com.hbrs.Views;

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

public class GyroFragment extends Fragment implements SpeedUpdatable{

    private int maxSpeed;
    private GyroView gyroView;
    private TextView tv_displacement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gyro, container,false);

        maxSpeed = getResources().getInteger(R.integer.Speed_START);

        tv_displacement = view.findViewById(R.id.tv_displacement);
        gyroView = view.findViewById(R.id.gyroView);

        gyroView.start();
        if (gyroView != null) {
            gyroView.setOnTiltChangeListener((x, y) -> {
                float leftSpeed = y + x;
                float rightSpeed = y - x;

                int leftMotor = (int) (leftSpeed * -maxSpeed);
                int rightMotor = (int) (rightSpeed * +maxSpeed);

                ORBManager.move(String.format("Joy : X= %.3f Y= %.3f", x, y), leftMotor, rightMotor);

                requireActivity().runOnUiThread(() ->
                        tv_displacement.setText(String.format("X: %.3f | Y: %.3f", x, y))
                );
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        gyroView.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        gyroView.stop();
    }

    @Override
    public void updateMaxSpeed(int speed) {
        maxSpeed = speed;
    }
}
