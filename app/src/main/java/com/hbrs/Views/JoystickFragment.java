package com.hbrs.Views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbrs.MainActivity;
import com.hbrs.ORB.ORB;
import com.hbrs.ORB.ORBManager;
import com.hbrs.R;

public class JoystickFragment extends Fragment {

    private ORB orb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_joystick, container, false);

        // Get ORB instance
        MainActivity mainActivity = (MainActivity) requireActivity();
        orb = ORBManager.getInstance(mainActivity);

        // Get Joystick from view & setup events
        JoystickView joystick = view.findViewById(R.id.joystickView);
        joystick.setOnMoveListener((x, y) -> {
            float maxSpeed = 1000f;

            float leftSpeed = y + x;
            float rightSpeed = y - x;

            int leftMotor = (int) (leftSpeed * maxSpeed);
            int rightMotor = (int) (rightSpeed * -maxSpeed); // minus = inverted motor

            orb.setMotor(ORB.M1, ORB.SPEED_MODE, leftMotor, 0);
            orb.setMotor(ORB.M4, ORB.SPEED_MODE, rightMotor, 0);

            Log.i("Test", String.format("X: %.4f, Y: %.4f | LM: %d, RM: %d", x, y, leftMotor, rightMotor));
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
