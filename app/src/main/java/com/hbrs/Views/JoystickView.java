package com.hbrs.Views;

import android.animation.ValueAnimator;
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
    private Paint centerDotPaint;

    // Proportions
    private float baseRadius;
    private float handleRadius;

    // Positions
    private PointF center;
    private PointF handlePosition;

    // Last handle displacement
    private float xPercent = 0f;
    private float yPercent = 0f;
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

        centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerDotPaint.setColor(Color.BLACK);
        centerDotPaint.setStyle(Paint.Style.FILL);
        centerDotPaint.setStrokeWidth(15f);

        center = new PointF();
        handlePosition = new PointF();
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
        canvas.drawPoint(center.x, center.y, centerDotPaint);
        canvas.drawCircle(handlePosition.x, handlePosition.y, handleRadius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Keep track of finger position
        // x is tracked correctly | negative : left - positive : right
        // y is inverse           |       up : left - positive : down
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
                xPercent = (handlePosition.x - center.x) / baseRadius;
                yPercent = (handlePosition.y - center.y) / baseRadius;

                // Scale diagonals so corners are 0.5 instead of 1.0
                float absX = Math.abs(xPercent);
                float absY = Math.abs(yPercent);
                float sum = absX + absY;
                if (sum > 1f) {
                    float factor = 1f / sum;
                    xPercent *= factor;
                    yPercent *= factor;
                }

                // Only trigger click if joystick is NOT centered
                if (Math.abs(xPercent) > 0.025f || Math.abs(yPercent) > 0.025f) {

                    // Trigger listener
                    if (moveListener != null) {
                        moveListener.onMove(xPercent, -1.0f * yPercent);
                    }
                }

                invalidate();
                break;

            // Aboarded or ended gesture
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Reset joystick to center and stop robot
                handlePosition.set(center.x, center.y);
                xPercent = 0f;
                yPercent = 0f;

                // Trigger listener
                if (moveListener != null) {
                    moveListener.onMove(0.0f, 0.0f);
                }

                invalidate();
                break;
        }

        return true;
    }
}
