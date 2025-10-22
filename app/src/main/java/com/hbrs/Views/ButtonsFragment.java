package com.hbrs.Views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbrs.ORB.ORBManager;
import com.hbrs.R;

public class ButtonsFragment extends Fragment implements SpeedUpdatable {
    private int maxSpeed = 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_buttons, container, false);

        // Find all buttons
        Button btnForward = view.findViewById(R.id.btn_forward);
        Button btnBackward = view.findViewById(R.id.btn_backward);
        Button btnLeft = view.findViewById(R.id.btn_left);
        Button btnRight = view.findViewById(R.id.btn_right);
        Button btnStop = view.findViewById(R.id.btn_stop);

        // Set click listeners
        btnForward.setOnClickListener(v -> ORBManager.move("Btn: Forward", -maxSpeed, +maxSpeed));
        btnBackward.setOnClickListener(v -> ORBManager.move("Btn: Backward", maxSpeed, -maxSpeed));
        btnLeft.setOnClickListener(v -> ORBManager.move("Btn: Left", maxSpeed, maxSpeed));
        btnRight.setOnClickListener(v -> ORBManager.move("Btn: Right", -maxSpeed, -maxSpeed));
        btnStop.setOnClickListener(v -> ORBManager.move("Btn: Stop", 0, 0));

        return view;
    }

    @Override
    public void updateMaxSpeed(int speed) {
        this.maxSpeed = speed;
    }
}
