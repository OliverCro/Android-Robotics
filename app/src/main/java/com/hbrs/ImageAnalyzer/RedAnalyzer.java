package com.hbrs.ImageAnalyzer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RedAnalyzer extends ModularAnalyzer {

    private final Context context;

    public RedAnalyzer(Context context) {
        this.context = context;
    }

    @Override
    public Bitmap doAnalysis(Bitmap bitmap) {
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        boolean[] isRedMask = new boolean[width * height];

        // Rot erkennen + Rest grau
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

            isRedMask[i] = isRed;

            if (!isRed) {
                int gray = (r * 30 + g * 59 + b * 11) / 100;
                pixels[i] = Color.rgb(gray, gray, gray);
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);


        boolean[] visited = new boolean[width * height];
        List<Rect> ballBoxes = new ArrayList<>();

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                if (!isRedMask[idx] || visited[idx]) continue;

                int minX = x, maxX = x;
                int minY = y, maxY = y;
                int area = 0; // Anzahl roter Pixel in dieser Komponente

                Deque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{x, y});
                visited[idx] = true;

                while (!queue.isEmpty()) {
                    int[] p = queue.removeFirst();
                    int cx = p[0];
                    int cy = p[1];

                    area++;

                    if (cx < minX) minX = cx;
                    if (cx > maxX) maxX = cx;
                    if (cy < minY) minY = cy;
                    if (cy > maxY) maxY = cy;

                    for (int k = 0; k < 4; k++) {
                        int nx = cx + dx[k];
                        int ny = cy + dy[k];
                        if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

                        int nIdx = ny * width + nx;
                        if (!visited[nIdx] && isRedMask[nIdx]) {
                            visited[nIdx] = true;
                            queue.addLast(new int[]{nx, ny});
                        }
                    }
                }

                int boxWidth = maxX - minX + 1;
                int boxHeight = maxY - minY + 1;

                // kleine Flecken ignorieren
                int minSize = 40; //
                if (boxWidth < minSize || boxHeight < minSize) continue;

                // Ball Check

                // Aspect Ratio ~ 1
                float aspect = boxWidth > boxHeight
                        ? (float) boxWidth / boxHeight
                        : (float) boxHeight / boxWidth;
                if (aspect > 1.3f) {
                    // zu länglich -> kein Ball
                    continue;
                }

                // Füllgrad
                float boxArea = boxWidth * boxHeight;
                float fill = area / boxArea; // Anteil rote Pixel in der Box

                // idealer Kreis in Box: ~0.78
                if (fill < 0.5f || fill > 0.9f) {
                    // zu leer oder zu voll (komische Form)
                    continue;
                }

                // Wenn beide Checks passen, als Ball akzeptieren
                ballBoxes.add(new Rect(minX, minY, maxX, maxY));
            }
        }

        //Bälle markieren
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.GREEN);

        for (Rect rect : ballBoxes) {
            canvas.drawRect(rect, paint);
        }

        return bitmap;
    }
}
