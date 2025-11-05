package com.hbrs.ORB;

import android.util.Log;
import com.hbrs.Activities.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class ORBManager {
    private static ORB orb;

    private static boolean canMoveForward = true, canMoveBackward = true;

    // Store last commanded motor speeds
    private static int lastLeftMotor = 0;
    private static int lastRightMotor = 0;

    static TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            float distanceFront = getDistanceFront();
            float heightBack = getHeightBack();

            // Correct NaN checks
            canMoveForward = !( !Float.isNaN(distanceFront) && distanceFront < 0.10 );
            canMoveBackward = !( !Float.isNaN(heightBack) && heightBack > 0.05 );

            // Stop motors only if trying to move into blocked direction
            if (!canMoveForward && lastLeftMotor > 0 && lastRightMotor > 0) {
                orb.setMotor(ORB.M1, ORB.SPEED_MODE, 0, 0);
                orb.setMotor(ORB.M4, ORB.SPEED_MODE, 0, 0);
            }

            if (!canMoveBackward && lastLeftMotor < 0 && lastRightMotor < 0) {
                orb.setMotor(ORB.M1, ORB.SPEED_MODE, 0, 0);
                orb.setMotor(ORB.M4, ORB.SPEED_MODE, 0, 0);
            }
        }
    };

    public static ORB getInstance(MainActivity context) {
        if (orb == null) {
            orb = new ORB(context);

            Timer timer = new Timer();
            timer.schedule(timerTask, 0, 50); // check every 50ms
        }
        return orb;
    }

    public static float getDistanceFront() {
        int val = orb.getSensorValue((byte)0);
        if (val != 30000) {
            return (float)(val * (343E-6/2));
        } else {
            return Float.NaN;
        }
    }

    public static float getHeightBack() {
        int val = orb.getSensorValue((byte)1);
        if (val != 30000) {
            return (float)(val * (343E-6/2));
        } else {
            return Float.NaN;
        }
    }

    public static void move(String source, int leftMotor, int rightMotor) {
        if (orb == null) {
            Log.i("Test", "Orb is NULL!");
            return;
        }

        lastLeftMotor = leftMotor;
        lastRightMotor = rightMotor;

        // Block commands into blocked directions
        if (!canMoveForward && leftMotor > 0 && rightMotor > 0) {
            Log.i("Test", "Forward blocked, movement ignored");
            return;
        }

        if (!canMoveBackward && leftMotor < 0 && rightMotor < 0) {
            Log.i("Test", "Backward blocked, movement ignored");
            return;
        }

        orb.setMotor(ORB.M1, ORB.SPEED_MODE, -leftMotor, 0);
        orb.setMotor(ORB.M4, ORB.SPEED_MODE, rightMotor, 0);

        Log.i("Test", String.format("%s | LM: %d RM: %d", source, leftMotor, rightMotor));
    }

    public static void Configure() {
        if (orb == null) {
            Log.i("Test", "Orb is NULL!");
            return;
        }

        orb.configMotor(ORB.M1, 144, 50, 50, 30);
        orb.configMotor(ORB.M4, 144, 50, 50, 30);

        orb.configSensor((byte)0, (byte)3, (byte)0, (byte)0);
        orb.configSensor((byte)1, (byte)3, (byte)0, (byte)0);
    }
}
