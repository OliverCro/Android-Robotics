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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button connectBtn = view.findViewById(R.id.btn_connect);
        connectBtn.setOnClickListener(v -> {
            // Call MainActivity’s connect handler
            MainActivity main = (MainActivity) requireActivity();
            main.OnClickConnect(v);
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
