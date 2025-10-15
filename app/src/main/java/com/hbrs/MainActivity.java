package com.hbrs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hbrs.Bluetooth.BT_DeviceListActivity;
import com.hbrs.ORB.ORB;
import com.hbrs.Views.JoystickView;

public class MainActivity extends AppCompatActivity {

    ORB orb;
    private JoystickView joystickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orb = new ORB( this );
        setContentView(R.layout.activity_main);
        joystickView = findViewById(R.id.joystickView);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void OnClickConnect(View view) {
        Log.i("Test", "BTN Connect Clicked");
        BT_DeviceListActivity.start(this, 50);
    }

    @Override
    public void onDestroy()
    {
        orb.close();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode)
        {
            case 50:
                switch(resultCode)
                {
                    case BT_DeviceListActivity.RESULT_OK:
                        Log.i("Test",BT_DeviceListActivity.getDeviceFromIntent(data).toString());
                        orb.openBT( BT_DeviceListActivity.getDeviceFromIntent(data));

                        orb.configMotor(ORB.M1, 144, 50, 50, 30);
                        orb.configMotor(ORB.M4, 144, 50, 50, 30);

                        break;
                    case BT_DeviceListActivity.RESULT_CANCELED:
                        Log.i("Test","canceled");
                        break;
                    default:
                        Log.i("Test", "Enable permissions first");
                        break;
                }
                break;
        }
    }

    public void OnClickLF(View view) {
        Log.i("Test", "BTN LF");
        orb.setMotor( ORB.M1, ORB.SPEED_MODE, -500, 0);
        orb.setMotor( ORB.M4, ORB.SPEED_MODE, 0, 0);
    }

    public void OnClickRF(View view) {
        Log.i("Test", "BTN RF");
        orb.setMotor( ORB.M1, ORB.SPEED_MODE, 0, 0);
        orb.setMotor( ORB.M4, ORB.SPEED_MODE, +500, 0);
    }

    public void OnClickLB(View view) {
        Log.i("Test", "BTN LB");
        orb.setMotor( ORB.M1, ORB.SPEED_MODE, +500, 0);
        orb.setMotor( ORB.M4, ORB.SPEED_MODE, 0, 0);
    }

    public void OnClickRB(View view) {
        Log.i("Test", "BTN RB");
        orb.setMotor( ORB.M1, ORB.SPEED_MODE, 0, 0);
        orb.setMotor( ORB.M4, ORB.SPEED_MODE, -500, 0);
    }

    public void OnClickJoystick(View view) {
        if (view instanceof JoystickView) {
            JoystickView joystick = (JoystickView) view;

            float x = joystick.getCurrentXPercent();
            float y = joystick.getCurrentYPercent();

            // Invert Y so up = forward
            y = -y;
            x = -x;

            // Clamp total power
            float maxSpeed = 1000;

            // Tank drive formula
            float leftSpeed = y + x;
            float rightSpeed = y - x;

            int leftMotor = (int) (leftSpeed * maxSpeed);
            int rightMotor = (int) (rightSpeed * -maxSpeed);

            orb.setMotor(ORB.M1, ORB.SPEED_MODE, leftMotor, 0);  // Left motor
            orb.setMotor(ORB.M4, ORB.SPEED_MODE, rightMotor, 0); // Right motor
        }
    }
}