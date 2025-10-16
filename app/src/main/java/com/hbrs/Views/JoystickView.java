package com.hbrs.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

    // Circles to Paint
    private Paint basePaint;
    private Paint handlePaint;

    // Proportions
    private float baseRadius;
    private float handleRadius;

    // Positions
    private PointF center;
    private PointF handlePosition;

    // Last handle displacement
    private float currentXPercent = 0f;
    private float currentYPercent = 0f;
    private OnMoveListener moveListener;


    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Create handle and base paint
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.GRAY);
        basePaint.setStyle(Paint.Style.FILL);

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.GREEN);
        handlePaint.setStyle(Paint.Style.FILL);

        center = new PointF();
        handlePosition = new PointF();

        // Make clickable to use OnClick
        setClickable(true);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        // Adjust for new Sizes
        center.set(width / 2f, height / 2f);
        handlePosition.set(center.x, center.y);

        baseRadius = Math.min(width, height) / 3f;
        handleRadius = baseRadius / 3.5f;
    }

    public interface OnMoveListener {
        void onMove(float xPercent, float yPercent);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.moveListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw elements of Joystick
        super.onDraw(canvas);

        canvas.drawCircle(center.x, center.y, baseRadius, basePaint);
        canvas.drawCircle(handlePosition.x, handlePosition.y, handleRadius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Keep track of finger position
        float touchX = event.getX();
        float touchY = event.getY();

        // x, y and center displacement
        float dx = touchX - center.x;
        float dy = touchY - center.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        switch (event.getAction()) {
            // Pressing and Moving
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Press inside circle?
                if (distance < baseRadius) {
                    // Adjust within circle
                    handlePosition.set(touchX, touchY);
                } else {
                    // Adjust outside circle
                    float ratio = baseRadius / distance;
                    float constrainedX = center.x + dx * ratio;
                    float constrainedY = center.y + dy * ratio;
                    handlePosition.set(constrainedX, constrainedY);
                }

                // Set values to read from while onClick event
                currentXPercent = (handlePosition.x - center.x) / baseRadius;
                currentYPercent = (handlePosition.y - center.y) / baseRadius;

                // Scale diagonals so corners are 0.5 instead of 1.0
                float absX = Math.abs(currentXPercent);
                float absY = Math.abs(currentYPercent);
                float sum = absX + absY;
                if (sum > 1f) {
                    float factor = 1f / sum;
                    currentXPercent *= factor;
                    currentYPercent *= factor;
                }


                // Only trigger click if joystick is NOT centered
                if (Math.abs(currentXPercent) > 0.025f || Math.abs(currentYPercent) > 0.025f) {
                    performClick();

                    if (moveListener != null) {
                        moveListener.onMove(currentXPercent, getCurrentYPercent());
                    }
                }

                invalidate();
                break;

            // Aboarded or ended gesture
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Reset joystick to center
                handlePosition.set(center.x, center.y);
                currentXPercent = 0f;
                currentYPercent = 0f;

                performClick();
                invalidate();
                break;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        // Required for accessibility and onClick to work properly
        return super.performClick();
    }

    /***
     *
     * @return left = - | right = +
     */
    public float getCurrentXPercent() {
        return currentXPercent;
    }

    /***
     *
     * @return up = + | down = -
     */
    public float getCurrentYPercent() {
        return -1.0f * currentYPercent;
    }
}
