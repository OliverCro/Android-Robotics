package com.hbrs.Fragments.CameraControl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.hbrs.Adapter.FragmentVisibilityListener;
import com.hbrs.ImageAnalyzer.CameraController;
import com.hbrs.R;

public class BasicCameraFragment extends Fragment implements FragmentVisibilityListener {

    PreviewView preview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_basic, container, false);

        preview = view.findViewById(R.id.previewView);

        return view;
    }

    @Override
    public void onVisible() {
        if (preview != null) {
            CameraController.getInstance().registerPreview(preview);
        }
    }

    @Override
    public void onHidden() {
        if (preview != null) {
            CameraController.getInstance().unregisterPreview(preview);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
