package com.hbrs.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hbrs.R;

public class JoystickView extends View {

    // === Customizable attributes ===
    private int defaultSizePx;
    private int baseColor;
    private int handleColor;
    private int centerDotColor;
    private float baseRadiusRatio;
    private float handleRadiusRatio;

    // === Paints ===
    private Paint basePaint;
    private Paint handlePaint;
    private Paint centerDotPaint;

    // === Geometry ===
    private float baseRadius;
    private float handleRadius;
    private PointF center;
    private PointF handlePosition;

    // === Motion ===
    private float xPercent = 0f;
    private float yPercent = 0f;

    private Handler holdHandler = new Handler();
    private boolean isHolding = false;
    private final int HOLD_INTERVAL_MS = 100; // interval for repeated calls

    private OnMoveListener moveListener;

    // Reference angles (every 45°) and vectors
    private static final float[] REF_ANGLES = {0, 45, 90, 135, 180, 225, 270, 315, 360};
    private static final float[][] REF_VECTORS = {
            {1f, 0f},
            {0.5f, 0.5f},
            {0f, 1f},
            {-0.5f, 0.5f},
            {-1f, 0f},
            {-0.5f, -0.5f},
            {0f, -1f},
            {0.5f, -0.5f},
            {1f, 0f}
    };

    public interface OnMoveListener {
        void onMove(float xPercent, float yPercent);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.moveListener = listener;
    }

    private final Runnable holdRunnable = new Runnable() {
        @Override
        public void run() {
            if (isHolding && moveListener != null) {
                moveListener.onMove(xPercent, -yPercent);
                holdHandler.postDelayed(this, HOLD_INTERVAL_MS);
            }
        }
    };

    private void startHoldRunnable() {
        holdHandler.removeCallbacks(holdRunnable);
        holdHandler.postDelayed(holdRunnable, HOLD_INTERVAL_MS);
    }

    private void stopHoldRunnable() {
        holdHandler.removeCallbacks(holdRunnable);
    }

    // === Constructors ===
    public JoystickView(Context context) {
        super(context);
        init(context, null);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Default values
        defaultSizePx = dpToPx(200);
        baseColor = Color.GRAY;
        handleColor = Color.GREEN;
        centerDotColor = Color.BLACK;
        baseRadiusRatio = 0.33f;       // base = 1/3 of view
        handleRadiusRatio = 0.28f;     // handle = 28% of base

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.JoystickView);
            defaultSizePx = ta.getDimensionPixelSize(R.styleable.JoystickView_defaultSize, defaultSizePx);
            baseColor = ta.getColor(R.styleable.JoystickView_baseColor, baseColor);
            handleColor = ta.getColor(R.styleable.JoystickView_handleColor, handleColor);
            centerDotColor = ta.getColor(R.styleable.JoystickView_centerDotColor, centerDotColor);
            baseRadiusRatio = ta.getFloat(R.styleable.JoystickView_baseRadiusRatio, baseRadiusRatio);
            handleRadiusRatio = ta.getFloat(R.styleable.JoystickView_handleRadiusRatio, handleRadiusRatio);
            ta.recycle();
        }

        // Setup paints
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(baseColor);
        basePaint.setStyle(Paint.Style.FILL);

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(handleColor);
        handlePaint.setStyle(Paint.Style.FILL);

        centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerDotPaint.setColor(centerDotColor);
        centerDotPaint.setStyle(Paint.Style.FILL);
        centerDotPaint.setStrokeWidth(15f);

        center = new PointF();
        handlePosition = new PointF();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // === Measurement ===
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = defaultSizePx;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;

        if (widthMode == MeasureSpec.EXACTLY)
            width = widthSize;
        else if (widthMode == MeasureSpec.AT_MOST)
            width = Math.min(desiredSize, widthSize);
        else
            width = desiredSize;

        if (heightMode == MeasureSpec.EXACTLY)
            height = heightSize;
        else if (heightMode == MeasureSpec.AT_MOST)
            height = Math.min(desiredSize, heightSize);
        else
            height = desiredSize;

        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    // === Layout size changed ===
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        center.set(width / 2f, height / 2f);
        handlePosition.set(center.x, center.y);

        baseRadius = Math.min(width, height) * baseRadiusRatio;
        handleRadius = baseRadius * handleRadiusRatio;
    }

    // === Drawing ===
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(center.x, center.y, baseRadius, basePaint);
        canvas.drawPoint(center.x, center.y, centerDotPaint);
        canvas.drawCircle(handlePosition.x, handlePosition.y, handleRadius, handlePaint);
    }

    // === Touch handling ===
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float dx = event.getX() - center.x;
        float dy = event.getY() - center.y;
        float distance = (float) Math.hypot(dx, dy); // same as sqrt(dx²+dy²)

        switch (event.getAction()) {

            // ACTION_DOWN -> A pressed gesture has started
            // ACTION_MOVE -> A change has happened during a press gesture
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Start repeated updates
                isHolding = true;
                startHoldRunnable();

                // Keep handle within joystick base
                float clampedDistance = Math.min(distance, baseRadius);
                float ratio = clampedDistance / (distance == 0 ? 1 : distance); // avoid div by zero
                handlePosition.set(center.x + dx * ratio, center.y + dy * ratio);

                // Calculate angle (0° = right, counterclockwise positive)
                float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                if (angle < 0) angle += 360f;

                // Find surrounding reference angles
                int indexREF = 0;
                for (; indexREF < REF_ANGLES.length - 1; indexREF++) {
                    if (angle <= REF_ANGLES[indexREF + 1]) {
                        break;
                    }
                }

                // Linear interpolation between reference vectors
                float t = (angle - REF_ANGLES[indexREF]) / (REF_ANGLES[indexREF + 1] - REF_ANGLES[indexREF]);
                float[] startVec = REF_VECTORS[indexREF];
                float[] endVec = REF_VECTORS[indexREF + 1];

                xPercent = (startVec[0] + t * (endVec[0] - startVec[0])) * (clampedDistance / baseRadius);
                yPercent = (startVec[1] + t * (endVec[1] - startVec[1])) * (clampedDistance / baseRadius);

                invalidate();
                break;

            // ACTION_UP     -> A pressed gesture has finished
            // ACTION_CANCEL -> he current gesture has been aborted
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Stop repeated calls
                isHolding = false;
                stopHoldRunnable();

                // Reset to center and notify
                handlePosition.set(center.x, center.y);
                xPercent = 0f;
                yPercent = 0f;
                break;
        }

        // Notify listener
        if(moveListener != null) {
            moveListener.onMove(xPercent, -yPercent);
        }
        invalidate();
        return true;
    }

}
