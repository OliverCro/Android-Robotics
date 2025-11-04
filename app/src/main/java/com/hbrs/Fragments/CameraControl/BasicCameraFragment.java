package com.hbrs.Fragments.CameraControl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.hbrs.R;

import java.util.concurrent.ExecutionException;

public class BasicCameraFragment extends Fragment {

    PreviewView cameraView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera_basic, container, false);

        cameraView = view.findViewById(R.id.previewView);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture
                = ProcessCameraProvider.getInstance(getActivity());

        cameraProviderFuture.addListener(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                }
                catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(getActivity()));



        return view;
    }

    void bindPreview(ProcessCameraProvider cameraProvider)
    {
        // use case: select a camera
        CameraSelector cameraSelector
                = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // use case: preview
        Preview preview     = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        // bind all use cases (maximum 3) to the camera provider
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this,
                cameraSelector,
                preview);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
