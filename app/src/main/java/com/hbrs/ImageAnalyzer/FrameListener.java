package com.hbrs.ImageAnalyzer;

import android.graphics.Bitmap;

/**
 * Callback interface used by analyzers to send frames
 */
public interface FrameListener {

    // Called when a new frame (as a Bitmap) is available.
    void onFrameAvailable(Bitmap bitmap);
}
