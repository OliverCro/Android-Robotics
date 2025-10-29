package com.hbrs.ORB;

import android.util.Log;

import com.hbrs.MainActivity;

public class ORBManager {
    private static ORB orb;

    public static ORB getInstance(MainActivity context) {
        if (orb == null) {
            orb = new ORB(context);
        }
        return orb;
    }

    public static void move(String source, int leftMotor, int rightMotor) {
        if(orb == null) {
            Log.i("Test", "Orb is NULL!");
            return;
        }

        orb.setMotor(ORB.M1, ORB.SPEED_MODE, -leftMotor, 0);
        orb.setMotor(ORB.M4, ORB.SPEED_MODE, rightMotor, 0);

        Log.i("Test", String.format("%s | LM: %d RM: %d", source, leftMotor, rightMotor));
    }

    public static void ConfigureMotors() {
        if(orb == null) {
            Log.i("Test", "Orb is NULL!");
            return;
        }

        orb.configMotor(ORB.M1, 144, 50, 50, 30);
        orb.configMotor(ORB.M4, 144, 50, 50, 30);
    }
}

