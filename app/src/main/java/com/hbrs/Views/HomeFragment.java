package com.hbrs.Views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbrs.MainActivity;
import com.hbrs.R;

public class HomeFragment extends Fragment {

    private Button connectBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        connectBtn = view.findViewById(R.id.btn_connect);

        connectBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).OnClickConnect(v);
        });

        // Sync initial state
        ((MainActivity) requireActivity()).setConnectionState(
                ((MainActivity) requireActivity()).getConnectionState()
        );

        return view;
    }

    public void updateButtonState(MainActivity.ConnectionState state) {
        if (connectBtn == null) return;

        switch (state) {
            case DISCONNECTED:
                connectBtn.setText("Connect");
                connectBtn.setEnabled(true);
                break;
            case CONNECTING:
                connectBtn.setText("Connecting...");
                connectBtn.setEnabled(false);
                break;
            case CONNECTED:
                connectBtn.setText("Disconnect");
                connectBtn.setEnabled(true);
                break;
            case FAILED:
                connectBtn.setText("Failed — Try Again");
                connectBtn.setEnabled(true);
                break;
        }
    }
}
