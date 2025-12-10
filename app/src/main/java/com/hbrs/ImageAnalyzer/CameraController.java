package com.hbrs.ImageAnalyzer;

import android.content.Context;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CameraController:
 * - Keeps camera active while either preview or analyzer exists
 * - Shuts down camera when both are gone or when stopCamera() is called
 */
public class CameraController {

    private static volatile CameraController instance;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageAnalysis imageAnalysis;

    private PreviewView currentPreviewView;
    private ModularAnalyzer modularAnalyzer;

    private LifecycleOwner lifecycleOwner;
    private Context appContext;

    private boolean initialized = false;

    private CameraController() {
    }

    public static CameraController getInstance() {
        if (instance == null) {
            synchronized (CameraController.class) {
                if (instance == null) instance = new CameraController();
            }
        }
        return instance;
    }

    public ModularAnalyzer getAnalyzer() {
        return getInstance().modularAnalyzer;
    }

    // ---------------------------------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------------------------------

    // Call once in Activity.onCreate() or parent fragment
    public void init(@NonNull LifecycleOwner owner, @NonNull Context context) {
        if (initialized) return;
        initialized = true;

        lifecycleOwner = owner;
        appContext = context.getApplicationContext();

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(appContext);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview = new Preview.Builder().build();
                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                rebindIfNeeded();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(appContext));

    }

    // ---------------------------------------------------------------------------------------------
    // Preview Management
    // ---------------------------------------------------------------------------------------------

    // Register a single preview (called in fragment onVisible/onCreateView)
    public void registerPreview(@NonNull PreviewView view) {
        currentPreviewView = view;
        attachSurfaceProvider(view);
        rebindIfNeeded();

    }

    // Unregister preview (called in fragment onHidden/onDestroyView)
    public void unregisterPreview(@NonNull PreviewView view) {
        if (currentPreviewView == view) {
            try {
                preview.setSurfaceProvider(null);
            } catch (Exception ignored) {
            }
            currentPreviewView = null;
            rebindIfNeeded();
        }

    }

    // ---------------------------------------------------------------------------------------------
    // Analyzer Management
    // ---------------------------------------------------------------------------------------------

    // Sets your ModularAnalyzer (with internal FrameListeners)
    public void setAnalyzer(@Nullable ModularAnalyzer analyzer) {
        modularAnalyzer = analyzer;
        rebindIfNeeded();

    }

    // ---------------------------------------------------------------------------------------------
    // Camera Binding Logic
    // ---------------------------------------------------------------------------------------------

    // Rebinds or stops camera depending on current preview/analyzer state
    private void rebindIfNeeded() {
        if (cameraProvider == null || lifecycleOwner == null) return;

        boolean shouldRun = (currentPreviewView != null || modularAnalyzer != null);

        if (!shouldRun) {
            stopCameraInternal();
            return;
        }

        try {
            cameraProvider.unbindAll();

            if (modularAnalyzer != null) {
                ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
                imageAnalysis.setAnalyzer(analysisExecutor, modularAnalyzer);
            } else {
                imageAnalysis.clearAnalyzer();
            }

            // decide which use-cases to bind
            if (currentPreviewView != null && modularAnalyzer != null) {
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis);
                attachSurfaceProvider(currentPreviewView);
            } else if (currentPreviewView != null) { // only analyzer
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview);
                attachSurfaceProvider(currentPreviewView);
            } else { // only analyzer
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // ---------------------------------------------------------------------------------------------
    // Camera Shutdown
    // ---------------------------------------------------------------------------------------------

    // Public method to force camera shutdown regardless of state
    public void stopCamera() {
        stopCameraInternal();

    }

    private void stopCameraInternal() {
        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
        } catch (Exception ignored) {
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------------------------------

    private void attachSurfaceProvider(@NonNull PreviewView previewView) {
        if (preview == null) return;
        if (previewView.isAttachedToWindow()) {
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
        } else {
            previewView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    preview.setSurfaceProvider(((PreviewView) v).getSurfaceProvider());
                    v.removeOnAttachStateChangeListener(this);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }
}
