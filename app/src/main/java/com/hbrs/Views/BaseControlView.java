package com.hbrs.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.hbrs.R;

public abstract class BaseControlView extends View {

    // === Style and Size Attributes ===
    // Default view size in pixels (used if no layout constraints are given)
    protected int defaultSizePx;
    protected int baseCircleColor;
    protected int handleCircleColor;
    protected int centerDotColor;
    protected float baseRadiusRatio;
    protected float centerDotRadiusRatio = 0.08f;
    protected float handleRadiusRatio = 0.28f;

    // === Paints (for drawing shapes) ===
    protected Paint baseCirclePaint;
    protected Paint centerDotPaint;
    protected Paint handlePaint;

    // === Geometry and Layout ===
    protected final PointF viewCenter = new PointF();
    protected float baseCircleRadius;

    // === Output (normalized control movement) ===
    protected float xPercent = 0f;
    protected float yPercent = 0f;

    // === Listener for movement events ===
    /**
     * Listener interface to receive movement updates from the control
     * Values are normalized from -1.0 to +1.0
     */
    public interface OnControlMoveListener {
        void onMove(float xPercent, float yPercent);
    }
    protected OnControlMoveListener moveListener;
    public void setOnControlMoveListener(OnControlMoveListener listener) {
        this.moveListener = listener;
    }

    // === Reference angles for interpolation ===
    protected static final float[] REFERENCE_ANGLES = {0, 45, 90, 135, 180, 225, 270, 315, 360};
    protected static final float[][] REFERENCE_VECTORS = {
            {1f, 0f},    // 0° -> Right
            {0.5f, 0.5f},// 45° -> Up-right
            {0f, 1f},    // 90° -> Up
            {-0.5f, 0.5f},// ...
            {-1f, 0f},
            {-0.5f, -0.5f},
            {0f, -1f},
            {0.5f, -0.5f},
            {1f, 0f}        // 360° -> Right (loop closure)
    };

    // === Constructors ===
    public BaseControlView(Context context) {
        super(context);
        initialize(context, null);
    }

    public BaseControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public BaseControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    // === Initialization ===
    // Reads attributes, applies defaults, and prepares paint objects
    protected void initialize(Context context, AttributeSet attrs) {
        // Default visual values
        defaultSizePx = dpToPx(200);
        baseCircleColor = 0xFF888888;
        handleCircleColor = 0xFF00FF00;
        centerDotColor = 0xFF000000;
        baseRadiusRatio = 0.33f;

        // Apply XML attributes if provided
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BaseControlView);
            defaultSizePx = ta.getDimensionPixelSize(R.styleable.BaseControlView_defaultSize, defaultSizePx);
            baseCircleColor = ta.getColor(R.styleable.BaseControlView_baseColor, baseCircleColor);
            handleCircleColor = ta.getColor(R.styleable.BaseControlView_handleColor, handleCircleColor);
            centerDotColor = ta.getColor(R.styleable.BaseControlView_centerDotColor, centerDotColor);
            baseRadiusRatio = ta.getFloat(R.styleable.BaseControlView_baseRadiusRatio, baseRadiusRatio);
            handleRadiusRatio = ta.getFloat(R.styleable.BaseControlView_handleRadiusRatio, handleRadiusRatio);
            ta.recycle();
        }

        // Initialize paints
        baseCirclePaint = createPaint(baseCircleColor);
        handlePaint = createPaint(handleCircleColor);
        centerDotPaint = createPaint(centerDotColor);
    }

    // Utility function to create a simple filled Paint with given color
    private Paint createPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        return paint;
    }

    // Converts dp -> px
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // === Measurement and Layout ===
    // Ensures the view is measured as a square
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desired = defaultSizePx;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        if (size == 0) size = desired;
        setMeasuredDimension(size, size);
    }

    // Calculates geometric center and base radius once size is known
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        viewCenter.set(width / 2f, height / 2f);
        baseCircleRadius = Math.min(width, height) * baseRadiusRatio;
        onBaseSizeChanged();
    }

     // Optional callback for subclasses to react to size changes
    protected void onBaseSizeChanged() { }

    // === Rendering ===
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the static base circle
        canvas.drawCircle(viewCenter.x, viewCenter.y, baseCircleRadius, baseCirclePaint);

        // Draw the small center dot
        float centerDotRadius = baseCircleRadius * centerDotRadiusRatio;
        canvas.drawCircle(viewCenter.x, viewCenter.y, centerDotRadius, centerDotPaint);

        // Ask subclass to draw its handle/knob
        drawHandle(canvas);
    }

    // Subclasses must implement this to render the handle at its position
    protected abstract void drawHandle(Canvas canvas);

    // === Vector Math and Movement Utilities ===
    // Converts polar coordinates (angle, strength) into normalized x/y movement
    protected void computePercentFromAngleAndStrength(float angleDegrees, float strength) {
        if (angleDegrees < 0) angleDegrees += 360f;

        // Find nearest lower reference angle for interpolation
        int i = 0;
        while (i < REFERENCE_ANGLES.length - 1 && angleDegrees > REFERENCE_ANGLES[i + 1]) i++;

        // Fractional position between two reference angles
        float t = (angleDegrees - REFERENCE_ANGLES[i]) / (REFERENCE_ANGLES[i + 1] - REFERENCE_ANGLES[i]);

        // Interpolate between direction vectors
        float[] v1 = REFERENCE_VECTORS[i];
        float[] v2 = REFERENCE_VECTORS[i + 1];
        xPercent = (v1[0] + t * (v2[0] - v1[0])) * strength;
        yPercent = (v1[1] + t * (v2[1] - v1[1])) * strength;

        // Notify listener
        if (moveListener != null) moveListener.onMove(xPercent, yPercent);
    }

    // Computes an angle (degrees) from vector dx, dy (0° = right, 90° = up)
    protected float angleFromDxDy(float dx, float dy) {
        float angle = (float) Math.toDegrees(Math.atan2(-dy, dx));
        if (angle < 0) angle += 360f;
        return angle;
    }

    // Clamps a movement vector (dx, dy) to stay within the base circle
    protected float[] clampVectorToBase(float dx, float dy) {
        float distance = (float) Math.hypot(dx, dy);
        if (distance <= baseCircleRadius)
            return new float[]{dx, dy, distance / baseCircleRadius};

        // Scale vector down if outside the base circle
        float scale = baseCircleRadius / distance;
        return new float[]{dx * scale, dy * scale, 1f};
    }
}
