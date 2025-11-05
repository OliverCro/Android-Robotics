package com.hbrs.Fragments.CameraControl;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hbrs.ImageAnalyzer.CameraController;
import com.hbrs.R;
import com.hbrs.Adapter.ViewPagerAdapter;

public class CameraControlFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.fragment_camera_control, container, false);

        checkPermission();

        // Get instances
        TabLayout tabLayout = view.findViewById(R.id.tl_camera_control);
        ViewPager2 viewPager = view.findViewById(R.id.vp_camera_control);

        // Adapter Setup
        ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity());

        // Add tabs with fragments
        adapter.addFragment(new CameraAnalyzerFragment(), "Analyzer");
        adapter.addFragment(new BasicCameraFragment(), "Camera");

        viewPager.setAdapter(adapter);
        // Don't allow swiping gestures to change tabs
        viewPager.setUserInputEnabled(false);

        // Link TabLayout and ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> { tab.setText(adapter.getTitle(position));}
        ).attach();

        return view;
    }

    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireActivity(), "Give Camera Permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraController.getInstance().stopCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
