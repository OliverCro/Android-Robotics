package com.hbrs.ImageAnalyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class RedAnalyzer extends ModularAnalyzer {

    private final Context context;

    public RedAnalyzer(Context context) {
        this.context = context;
    }

    @Override
    public Bitmap doAnalysis(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int[] pixels = new int[width*height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            for (int i = 0; i < pixels.length; i++) {
                int c = pixels[i];
                int r = (c >> 16) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = c & 0xFF;

                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                int delta = max - min;

                boolean isRed = false;
                if (delta > 20 && max == r) {
                    float hue = (60 * ((g - b) / (float) delta) + 360) % 360;
                    float sat = delta / (float) max;
                    float val = max / 255f;
                    isRed = ((hue < 15 || hue > 345) && sat > 0.35f && val > 0.2f);
                }

                if (!isRed) {
                    int gray = (r * 30 + g * 59 + b * 11) / 100;
                    pixels[i] = Color.rgb(gray, gray, gray);
                }
            }

            bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            return bitmap;
        }
}
