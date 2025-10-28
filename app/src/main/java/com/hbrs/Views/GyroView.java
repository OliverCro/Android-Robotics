package com.hbrs.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

import com.hbrs.R;

public class GyroView extends View implements SensorEventListener {

    // === Customizable attributes ===
    private int baseColor;
    private int knobColor;
    private int centerDotColor;
    private int textColor;
    private float baseStrokeWidth;
    private float knobRadiusRatio;

    // === Paints ===
    private Paint basePaint;
    private Paint knobPaint;
    private Paint centerDotPaint;
    private Paint textPaint;

    // === Geometry ===
    private float centerX, centerY, baseRadius;
    private PointF knobPosition = new PointF();

    // === Orientation data ===
    private float pitch = 0f;
    private float roll = 0f;
    private float xPercent = 0f;
    private float yPercent = 0f;

    // === Reference angles for interpolation ===
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

    // === Sensors ===
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    private static final float ALPHA = 0.97f;

    // === Listener ===
    public interface OnTiltChangeListener {
        void onTiltChanged(float xPercent, float yPercent);
    }
    private OnTiltChangeListener listener;
    public void setOnTiltChangeListener(OnTiltChangeListener l) { listener = l; }

    // === Constructors ===
    public GyroView(Context context) {
        super(context);
        init(context, null);
    }

    public GyroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GyroView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // === Init ===
    private void init(Context context, AttributeSet attrs) {
        // Defaults
        baseColor = Color.DKGRAY;
        knobColor = Color.CYAN;
        centerDotColor = Color.GREEN;
        textColor = Color.WHITE;
        baseStrokeWidth = 6f;
        knobRadiusRatio = 0.1f;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GyroView);
            baseColor = ta.getColor(R.styleable.GyroView_gyroBaseColor, baseColor);
            knobColor = ta.getColor(R.styleable.GyroView_knobColor, knobColor);
            centerDotColor = ta.getColor(R.styleable.GyroView_gyroCenterDotColor, centerDotColor);
            textColor = ta.getColor(R.styleable.GyroView_textColor, textColor);
            baseStrokeWidth = ta.getDimension(R.styleable.GyroView_baseStrokeWidth, baseStrokeWidth);
            knobRadiusRatio = ta.getFloat(R.styleable.GyroView_knobRadiusRatio, knobRadiusRatio);
            ta.recycle();
        }

        // Paint setup
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(baseColor);
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(baseStrokeWidth);

        knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobPaint.setColor(knobColor);
        knobPaint.setStyle(Paint.Style.FILL);

        centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerDotPaint.setColor(centerDotColor);
        centerDotPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Sensor setup
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    // === Lifecycle control ===
    public void start() {
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (magnetometer != null)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    // === Measurement ===
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 2f * 0.8f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Base circle
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);

        // Calculate knob position based on pitch/roll
        float offsetX = (roll / 45f) * baseRadius;
        float offsetY = (-pitch / 45f) * baseRadius;

        // Clamp knob to circle boundary
        float distance = (float) Math.hypot(offsetX, offsetY);
        if (distance > baseRadius) {
            float scale = baseRadius / distance;
            offsetX *= scale;
            offsetY *= scale;
            distance = baseRadius; // ensure consistent distance for later
        }

        // Update knob position
        knobPosition.set(centerX + offsetX, centerY + offsetY);

        // Draw knob and center dot
        float knobRadius = baseRadius * knobRadiusRatio;
        canvas.drawCircle(knobPosition.x, knobPosition.y, knobRadius, knobPaint);
        canvas.drawCircle(centerX, centerY, knobRadius * 0.3f, centerDotPaint);

        // Joystick-style interpolation
        if (distance > 0f) {
            // Calculate direction angle (0° = right, counterclockwise positive)
            float angle = (float) Math.toDegrees(Math.atan2(-offsetY, offsetX));
            if (angle < 0) angle += 360f;

            // Find the two reference directions to interpolate between
            int i = 0;
            while (i < REF_ANGLES.length - 1 && angle > REF_ANGLES[i + 1]) i++;

            float t = (angle - REF_ANGLES[i]) / (REF_ANGLES[i + 1] - REF_ANGLES[i]);
            float[] v1 = REF_VECTORS[i];
            float[] v2 = REF_VECTORS[i + 1];

            float strength = distance / baseRadius;
            xPercent = (v1[0] + t * (v2[0] - v1[0])) * strength;
            yPercent = (v1[1] + t * (v2[1] - v1[1])) * strength;
        } else {
            xPercent = 0f;
            yPercent = 0f;
        }

        // Notify listener
        if (listener != null) {
            listener.onTiltChanged(xPercent, yPercent);
        }
    }

    // === Sensor logic ===
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = ALPHA;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
        }

        float[] R = new float[9];
        float[] I = new float[9];
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            float[] orientation = new float[3];
            SensorManager.getOrientation(R, orientation);

            pitch = (float) Math.toDegrees(orientation[1]);
            roll = (float) Math.toDegrees(orientation[2]);

            // Clamp pitch and roll to ±45°
            if (pitch > 45f) pitch = 45f;
            if (pitch < -45f) pitch = -45f;
            if (roll > 45f) roll = 45f;
            if (roll < -45f) roll = -45f;

            invalidate();
        }
    }

    // === Measurement ===
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Keep it a perfect square by using the smaller dimension
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
