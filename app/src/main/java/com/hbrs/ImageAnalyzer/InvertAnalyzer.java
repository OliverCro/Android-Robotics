package com.hbrs.ImageAnalyzer;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Analyzer to invert an image
 */
public class InvertAnalyzer extends ModularAnalyzer{
    @Override
    public Bitmap doAnalysis(Bitmap bitmap) {
        // Use as array because bitmap.setPixel is very slow
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // Calculate the inverse of r, g and b and parse back
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = 255 - Color.red(color);
            int g = 255 - Color.green(color);
            int b = 255 - Color.blue(color);
            pixels[i] = Color.rgb(r, g, b);
        }

        // Setting all pixels at once
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
