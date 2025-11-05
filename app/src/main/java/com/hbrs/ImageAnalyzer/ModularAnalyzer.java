package com.hbrs.ImageAnalyzer;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

/**
 * Base for all analyzers
 *
 * analyzer:
 * - receives image frames from CameraX
 * - can send results over FrameListener
 */
public abstract class ModularAnalyzer implements ImageAnalysis.Analyzer {

    // --------------------------------------------------------------------
    //      Listener
    // --------------------------------------------------------------------
    private FrameListener listener;

    public void setListener(FrameListener l) { this.listener = l; }

    // --------------------------------------------------------------------
    //      Analysis + Boilerplate
    // --------------------------------------------------------------------

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        try {
            // convert imageProxy -> bitmap
            Bitmap bitmap = imageProxy.toBitmap();

            // Rotate the image to up
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            if (rotationDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                bitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            // Do the analysis
            bitmap = doAnalysis(bitmap);

            // Invoke possible listener
            if (listener != null) {
                listener.onFrameAvailable(bitmap);
            }
        } finally {
            // MUST close to avoid freezing CameraX
            imageProxy.close();
        }
    }

    // ------------------------------------------------------------------
    //      Point of interrest
    // ------------------------------------------------------------------

    // Specific Analyzer has to implement
    public abstract Bitmap doAnalysis(Bitmap bitmap);
}

