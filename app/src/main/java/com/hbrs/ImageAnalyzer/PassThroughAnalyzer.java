package com.hbrs.ImageAnalyzer;

import android.graphics.Bitmap;

/**
 * A simple analyzer that outputs the same image that comes in
 */
public class PassThroughAnalyzer extends ModularAnalyzer {

    @Override
    public Bitmap doAnalysis(Bitmap bitmap) {
        // Do nothing
        return bitmap;
    }
}
