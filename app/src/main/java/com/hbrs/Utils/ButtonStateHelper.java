package com.hbrs.Utils;

import android.widget.Button;
import com.hbrs.MainActivity;
import com.hbrs.R;

public class ButtonStateHelper {

    public static void updateButton(Button button, MainActivity.ConnectionState state) {
        if (button == null) return;

        switch (state) {
            case DISCONNECTED:
                button.setText(R.string.Connection_Connect);
                button.setEnabled(true);
                break;
            case CONNECTING:
                button.setText(R.string.Connection_Connecting);
                button.setEnabled(false);
                break;
            case CONNECTED:
                button.setText(R.string.Connection_Disconnect);
                button.setEnabled(true);
                break;
            case FAILED:
                button.setText(R.string.Connection_Failed);
                button.setEnabled(true);
                break;
            case PERMISSION:
                button.setText(R.string.Connection_Permission);
                button.setEnabled(true);
                break;
        }
    }
}
