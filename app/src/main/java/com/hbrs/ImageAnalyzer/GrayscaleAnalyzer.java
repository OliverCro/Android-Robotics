package com.hbrs.ImageAnalyzer;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Create gray scaled representation of image
 */
public class GrayscaleAnalyzer extends ModularAnalyzer{
    @Override
    public Bitmap doAnalysis(Bitmap bitmap) {
        // Use as array because bitmap.setPixel is very slow
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // Calculate the gray value for every pixel
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

            pixels[i] = Color.rgb(gray, gray, gray);
        }

        // Set all pixel at once
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
