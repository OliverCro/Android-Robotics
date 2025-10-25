package com.hbrs.Utils;

import android.widget.Button;
import com.hbrs.MainActivity;

public class ButtonStateHelper {

    public static void updateButton(Button button, MainActivity.ConnectionState state) {
        if (button == null) return;

        switch (state) {
            case DISCONNECTED:
                button.setText("Connect");
                button.setEnabled(true);
                break;
            case CONNECTING:
                button.setText("Connecting...");
                button.setEnabled(false);
                break;
            case CONNECTED:
                button.setText("Disconnect");
                button.setEnabled(true);
                break;
            case FAILED:
                button.setText("Failed — Try Again");
                button.setEnabled(true);
                break;
            case NOPERMISSION:
                button.setText("No BT-Permission");
                button.setEnabled(true);
                break;
        }
    }
}
