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

import com.hbrs.ORB.ORBManager;

public class RedAnalyzer extends ModularAnalyzer {

    private final Context context;

    // ================= PID Parameter =================
    private float kp = 2.0f;//1.2f;
    private float ki = 0.0f;
    private float kd = 0.5f;//0.35f;

    // ================= PID Zustand =================
    private float integral = 0;
    private float lastError = 0;

    // ================= PID Limits =================
    private float integralLimit = 1000;
    private int maxTurn = 400;

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

        // ================= Rot-Erkennung =================
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

        // ================= Connected Components =================
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
                int area = 0;

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

                        if (nx < 0 || nx >= width || ny < 0 || ny >= height)
                            continue;

                        int nIdx = ny * width + nx;
                        if (!visited[nIdx] && isRedMask[nIdx]) {
                            visited[nIdx] = true;
                            queue.addLast(new int[]{nx, ny});
                        }
                    }
                }

                int boxWidth = maxX - minX + 1;
                int boxHeight = maxY - minY + 1;

                if (boxWidth < 40 || boxHeight < 40) continue;

                float aspect = boxWidth > boxHeight
                        ? (float) boxWidth / boxHeight
                        : (float) boxHeight / boxWidth;
                if (aspect > 1.3f) continue;

                float fill = area / (float) (boxWidth * boxHeight);
                if (fill < 0.5f || fill > 0.9f) continue;

                ballBoxes.add(new Rect(minX, minY, maxX, maxY));
            }
        }

        // ================= Boxen zeichnen =================
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.GREEN);

        for (Rect r : ballBoxes) {
            canvas.drawRect(r, paint);
        }

        // ================= Ball verfolgen =================
        if (!ballBoxes.isEmpty()) {

            Rect target = ballBoxes.get(0);
            for (Rect r : ballBoxes) {
                if (r.width() * r.height() > target.width() * target.height()) {
                    target = r;
                }
            }

            canvas.drawRect(target, paint);

            followBall(
                    target.centerX(),
                    target.centerY(),
                    target.width() * target.height(),
                    width
            );

        } else {
            //sendMotorCommand(-350, 350); // Langsam drehen
            // TODO wollen wir das wirklich
            resetPID();
        }

        return bitmap;
    }

    // ================= PID Ball-Follower =================
    private void followBall(int cx, int cy, int size, int imageWidth) {

        int center = imageWidth / 2;
        int baseSpeed = 500;
        int backSpeed = -400;
        int targetSize = 17000;

        if (size > targetSize) {
            sendMotorCommand(backSpeed, backSpeed);
            resetPID();
            return;
        }

        float error = center - cx;

        integral += error;
        if (integral > integralLimit) integral = integralLimit;
        if (integral < -integralLimit) integral = -integralLimit;

        float derivative = error - lastError;
        lastError = error;

        float turn = kp * error + ki * integral + kd * derivative;

        if (turn > maxTurn) turn = maxTurn;
        if (turn < -maxTurn) turn = -maxTurn;

        int leftSpeed = (int) (baseSpeed + turn);
        int rightSpeed = (int) (baseSpeed - turn);

        sendMotorCommand(leftSpeed, rightSpeed);
    }

    private void resetPID() {
        integral = 0;
        lastError = 0;
    }

    private void sendMotorCommand(int left, int right) {
        ORBManager.move("RedAnalyzer PID Follow", left, right);
    }
}


// TODO P-D Regler implementieren um besser und exakter zu regeln
// TODO Merken welcher Analyzer ausgewählt war, wenn die App geschlafen hat diesen wieder auswählen.
