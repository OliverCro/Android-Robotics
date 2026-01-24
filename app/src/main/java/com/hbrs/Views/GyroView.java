package com.hbrs.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;

import com.hbrs.R;

public class GyroView extends BaseControlView implements SensorEventListener {

    // === Geometry ===
    private final PointF handlePosition = new PointF();

    // === Sensor data ===
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;

    // Deadzone to prevent movement under minimal displacement
    private float deadZoneRatio = 0.05f;
    private int deadZoneColor = 0x55FFFFFF;

    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];

    // Keep 85 % old orientation data, only 15 % new orientation data
    private static final float FILTER_ALPHA = 0.85f;

    private float pitch = 0f;  // front/back tilt
    private float roll = 0f;   // side tilt
    private final float maxAngle = 20f; // Maximum angle x (degree) to max output

    // === Constructors ===
    public GyroView(Context context) {
        this(context, null);
    }

    public GyroView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initGyro(context, attrs);
    }

    public GyroView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        initGyro(context, attrs);
    }

    private void initGyro(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GyroView);
            deadZoneRatio = ta.getFloat(R.styleable.GyroView_deadZoneRatio, deadZoneRatio);
            deadZoneColor = ta.getColor(R.styleable.GyroView_deadZoneColor, deadZoneColor);
            ta.recycle();
        }
    }

    // === Lifecycle management ===
    public void start() {
        // SENSOR_DELAY_GAME approx 50Hz refresh
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (magnetometer != null)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    // === Drawing handle + deadzone ===
    @Override
    protected void drawHandle(Canvas canvas) {
        // Compute desired displacement based on tilt angles
        float offsetX = (roll / maxAngle) * baseCircleRadius;
        float offsetY = (-pitch / maxAngle) * baseCircleRadius;

        // Clamp to base circle
        float[] clamped = clampVectorToBase(offsetX, offsetY);
        float clampedX = clamped[0];
        float clampedY = clamped[1];
        float strength = clamped[2];

        // Draw Deadzone
        Paint deadZonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deadZonePaint.setStyle(Paint.Style.STROKE);
        deadZonePaint.setStrokeWidth(2f);
        deadZonePaint.setColor(deadZoneColor);
        canvas.drawCircle(viewCenter.x, viewCenter.y, baseCircleRadius * deadZoneRatio, deadZonePaint);

        // Deadzone logic
        if (strength < deadZoneRatio) {
            // Inside deadzone -> reset to center
            handlePosition.set(viewCenter.x, viewCenter.y);
            xPercent = 0f;
            yPercent = 0f;

            // Notify listener of neutral position
            if (moveListener != null) {
                moveListener.onMove(0f, 0f);
            }

            // Draw centered handle
            float handleRadius = baseCircleRadius * handleRadiusRatio;
            canvas.drawCircle(viewCenter.x, viewCenter.y, handleRadius, handlePaint);
            return;
        }

        // Position handle normally outside deadzone
        handlePosition.set(viewCenter.x + clampedX, viewCenter.y + clampedY);

        // Draw the handle
        float handleRadius = baseCircleRadius * handleRadiusRatio;
        canvas.drawCircle(handlePosition.x, handlePosition.y, handleRadius, handlePaint);

        // Compute movement percent (normalized -1..1)
        float angle = angleFromDxDy(offsetX, offsetY);
        computePercentFromAngleAndStrength(angle, strength);
    }

    // === Sensor logic ===
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Smooth noisy sensor data by combining with previous data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            for (int i = 0; i < 3; i++)
                gravity[i] = FILTER_ALPHA * gravity[i] + (1 - FILTER_ALPHA) * event.values[i];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            for (int i = 0; i < 3; i++)
                geomagnetic[i] = FILTER_ALPHA * geomagnetic[i] + (1 - FILTER_ALPHA) * event.values[i];
        }

        // Determine device rotation
        float[] R = new float[9];
        float[] I = new float[9];
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            float[] orientation = new float[3];
            SensorManager.getOrientation(R, orientation);

            // convert from radius to degree
            pitch = (float) Math.toDegrees(orientation[1]);
            roll = (float) Math.toDegrees(orientation[2]);

            // Clamp to the maximum angle
            pitch = Math.max(-maxAngle, Math.min(maxAngle, pitch));
            roll = Math.max(-maxAngle, Math.min(maxAngle, roll));

            // trigger redraw
            invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
