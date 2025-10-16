package com.hbrs.ORB;

import com.hbrs.MainActivity;

public class ORBManager {
    private static ORB orb;
    private static boolean connected;

    public static ORB getInstance(MainActivity context) {
        if (orb == null) {
            orb = new ORB(context);
        }
        return orb;
    }


}

