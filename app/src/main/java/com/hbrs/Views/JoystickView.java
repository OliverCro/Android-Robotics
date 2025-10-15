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

    private Paint basePaint;
    private Paint handlePaint;

    private float baseRadius;
    private float handleRadius;
    private PointF center;
    private PointF handlePosition;

    private float currentXPercent = 0f;
    private float currentYPercent = 0f;

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.GRAY);
        basePaint.setStyle(Paint.Style.FILL);

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.BLUE);
        handlePaint.setStyle(Paint.Style.FILL);

        center = new PointF();
        handlePosition = new PointF();

        setClickable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        center.set(w / 2f, h / 2f);
        handlePosition.set(center.x, center.y);

        baseRadius = Math.min(w, h) / 3f;
        handleRadius = baseRadius / 2.5f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(center.x, center.y, baseRadius, basePaint);
        canvas.drawCircle(handlePosition.x, handlePosition.y, handleRadius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        float dx = touchX - center.x;
        float dy = touchY - center.y;

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (distance < baseRadius) {
                    handlePosition.set(touchX, touchY);
                } else {
                    float ratio = baseRadius / distance;
                    float constrainedX = center.x + dx * ratio;
                    float constrainedY = center.y + dy * ratio;
                    handlePosition.set(constrainedX, constrainedY);
                }

                currentXPercent = (handlePosition.x - center.x) / baseRadius;
                currentYPercent = (handlePosition.y - center.y) / baseRadius;

                // Only trigger click if joystick is NOT centered
                if (Math.abs(currentXPercent) > 0.05f || Math.abs(currentYPercent) > 0.05f) {
                    performClick();
                }

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Reset joystick to center
                handlePosition.set(center.x, center.y);
                currentXPercent = 0f;
                currentYPercent = 0f;

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

    public float getCurrentXPercent() {
        return currentXPercent;
    }

    public float getCurrentYPercent() {
        return currentYPercent;
    }
}
