package com.hbrs.Fragments.CameraControl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbrs.ImageAnalyzer.CameraController;
import com.hbrs.ImageAnalyzer.GrayscaleAnalyzer;
import com.hbrs.ImageAnalyzer.InvertAnalyzer;
import com.hbrs.ImageAnalyzer.RedAnalyzer;
import com.hbrs.ORB.ORB;
import com.hbrs.ORB.ORBManager;
import com.hbrs.R;
import com.hbrs.ImageAnalyzer.ModularAnalyzer;
import com.hbrs.ImageAnalyzer.PassThroughAnalyzer;

/**
 * CameraX fragment that captures frames and using the analyzer on it
 *
 * - The analyzed result is displayed in an ImageView.
 */
public class CameraAnalyzerFragment extends Fragment {

    private CameraController cameraController = null;
    private ImageView imageView;
    private Spinner analyzerSpinner;
    private ModularAnalyzer oldAnalyzer = null;
    private Button toggleAnalyzer;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        cameraController = CameraController.getInstance();

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_camera_analyzer, container, false);

        imageView = view.findViewById(R.id.analyzedImage);
        analyzerSpinner = view.findViewById(R.id.analyzer_spinner);
        toggleAnalyzer = view.findViewById(R.id.toggle_filter);
        toggleAnalyzer.setOnClickListener(this::OnStopClicked);


        // Create options for spinner
        String[] options = {"Red Filter", "Passthrough", "Grayscale", "Inverted Colors"};

        // Create ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to Spinner
        analyzerSpinner.setAdapter(adapter);

        // Handle selection events
        analyzerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ModularAnalyzer analyzer = null;

                switch (position) {
                    case 3:
                        analyzer = new PassThroughAnalyzer();
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        cameraController.setAnalyzer(analyzer);
                        break;
                    case 1:
                        analyzer = new GrayscaleAnalyzer();
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        cameraController.setAnalyzer(analyzer);
                        break;
                    case 2:
                        analyzer = new InvertAnalyzer();
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        cameraController.setAnalyzer(analyzer);
                        break;
                    case 0:
                        analyzer = new RedAnalyzer(requireActivity());
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        cameraController.setAnalyzer(analyzer);
                        break;
                }

                oldAnalyzer = analyzer;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // optional: do nothing
            }
        });

        return view;
    }

    public void OnStopClicked(View view) {
        if (cameraController.getAnalyzer() != null) {
            cameraController.setAnalyzer(null);
            ORBManager.move("Camera Analyzer",0,0);
        } else {
            cameraController.setAnalyzer(oldAnalyzer);
        }
    }

    @Override
    public void onResume() {
        // Create modular Analyzer to use
        super.onResume();

        ModularAnalyzer analyzer = null;

        if (oldAnalyzer != null) {
            analyzer = oldAnalyzer;
        } else
        {
            analyzer = new PassThroughAnalyzer();
        }

        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));

        // Set analyzer for the Camera
        cameraController.setAnalyzer(analyzer);
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraController.setAnalyzer(null);

        ORBManager.move("Stop",0, 0);
    }

    @Override
    public void onDestroy() {
        ORBManager.move("Camera Analyzer",0,0);
        super.onDestroy();
    }
}
