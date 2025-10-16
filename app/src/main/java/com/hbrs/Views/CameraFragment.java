package com.hbrs.Views;

import android.os.Bundle;
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

public class CameraFragment extends Fragment {

    private ORB orb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Get ORB instance
        MainActivity mainActivity = (MainActivity) requireActivity();
        orb = ORBManager.getInstance(mainActivity);

        // Set listener

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
