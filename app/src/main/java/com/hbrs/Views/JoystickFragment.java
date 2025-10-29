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

public class JoystickFragment extends Fragment implements SpeedUpdatable {

    private JoystickView jv_movement;
    private TextView tv_displacement;

    private int maxSpeed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_joystick, container, false);

        jv_movement = view.findViewById(R.id.jv_movement);
        tv_displacement = view.findViewById(R.id.tv_displacement);

        maxSpeed = getResources().getInteger(R.integer.Speed_START);

        if (jv_movement != null) {
            jv_movement.setOnControlMoveListener((x, y) -> {
                float leftSpeed = y + x;
                float rightSpeed = y - x;

                int leftMotor = (int) (leftSpeed * maxSpeed);
                int rightMotor = (int) (rightSpeed * maxSpeed);

                ORBManager.move(String.format("Joy : X= %.3f Y= %.3f", x, y), leftMotor, rightMotor);

                requireActivity().runOnUiThread(() ->
                        tv_displacement.setText(String.format("X: %.3f | Y: %.3f", x, y))
                );
            });
        }

        return view;
    }

    @Override
    public void updateMaxSpeed(int speed) {
        this.maxSpeed = speed;
    }
}
