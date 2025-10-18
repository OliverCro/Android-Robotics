package com.hbrs.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
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
    private OnMoveListener moveListener;

    public interface OnMoveListener {
        void onMove(float xPercent, float yPercent);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.moveListener = listener;
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
                    handlePosition.set(center.x + dx * ratio, center.y + dy * ratio);
                }

                xPercent = (handlePosition.x - center.x) / baseRadius;
                yPercent = (handlePosition.y - center.y) / baseRadius;

                // Normalize diagonals
                float absX = Math.abs(xPercent);
                float absY = Math.abs(yPercent);
                float sum = absX + absY;
                if (sum > 1f) {
                    float factor = 1f / sum;
                    xPercent *= factor;
                    yPercent *= factor;
                }

                if (moveListener != null) {
                    moveListener.onMove(xPercent, -yPercent);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handlePosition.set(center.x, center.y);
                xPercent = 0f;
                yPercent = 0f;
                if (moveListener != null) {
                    moveListener.onMove(0f, 0f);
                }
                invalidate();
                break;
        }
        return true;
    }
}
