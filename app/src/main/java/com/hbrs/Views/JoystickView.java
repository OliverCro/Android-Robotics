package com.hbrs.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class JoystickView extends BaseControlView {

    // === Geometry ===
    private final PointF handlePosition = new PointF();

    // === Continuous move updates ===
    private final Handler repeatHandler = new Handler();
    private boolean isHolding = false;
    private static final int HOLD_INTERVAL_MS = 100;

    private final Runnable holdRunnable = new Runnable() {
        @Override
        public void run() {
            if (isHolding && moveListener != null) {
                moveListener.onMove(xPercent, yPercent);
                repeatHandler.postDelayed(this, HOLD_INTERVAL_MS);
            }
        }
    };

    // === Constructors ===
    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        textPaint.setColor(0xFFFFFFFF); // white
        textPaint.setTextSize(36f);     // adjust as needed
        textPaint.setTextAlign(Paint.Align.CENTER);
    }


    // Called when size changes, used to reset handle position
    @Override
    protected void onBaseSizeChanged() {
        handlePosition.set(viewCenter.x, viewCenter.y);
    }

    @Override
    protected void drawHandle(Canvas canvas) {
        float handleRadius = baseCircleRadius * handleRadiusRatio;
        canvas.drawCircle(handlePosition.x, handlePosition.y, handleRadius, handlePaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the handle
        drawHandle(canvas);

        // Draw text labels around the circle
        float r = baseCircleRadius + 20f; // offset from circle
        float cx = viewCenter.x;
        float cy = viewCenter.y;

        canvas.drawText("MF", cx, cy - r, textPaint);
        canvas.drawText("MB", cx, cy + r, textPaint);
        canvas.drawText("TR", cx + r, cy, textPaint);
        canvas.drawText("TL", cx - r, cy, textPaint);
    }


    // === Touch Interaction ===
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float dx = event.getX() - viewCenter.x;
        float dy = event.getY() - viewCenter.y;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                isHolding = true;
                repeatHandler.removeCallbacks(holdRunnable);
                repeatHandler.postDelayed(holdRunnable, HOLD_INTERVAL_MS);

                // Keep handle inside base circle
                float[] clamped = clampVectorToBase(dx, dy);
                handlePosition.set(viewCenter.x + clamped[0], viewCenter.y + clamped[1]);

                // Compute angle + normalized strength
                float angle = angleFromDxDy(dx, dy);
                computePercentFromAngleAndStrength(angle, clamped[2]);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                isHolding = false;
                repeatHandler.removeCallbacks(holdRunnable);

                // Reset handle and output
                handlePosition.set(viewCenter.x, viewCenter.y);
                xPercent = 0f;
                yPercent = 0f;
                if (moveListener != null) moveListener.onMove(0f, 0f);
                break;
            }
        }

        invalidate();
        return true;
    }
}
