package com.hbrs.Fragments.CameraControl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hbrs.Adapter.FragmentVisibilityListener;
import com.hbrs.ImageAnalyzer.CameraController;
import com.hbrs.ImageAnalyzer.GrayscaleAnalyzer;
import com.hbrs.ImageAnalyzer.InvertAnalyzer;
import com.hbrs.R;
import com.hbrs.ImageAnalyzer.ModularAnalyzer;
import com.hbrs.ImageAnalyzer.PassThroughAnalyzer;

/**
 * CameraX fragment that captures frames and using the analyzer on it
 *
 * - The analyzed result is displayed in an ImageView.
 */
public class CameraAnalyzerFragment extends Fragment implements FragmentVisibilityListener {

    private ImageView imageView;
    private Spinner analyzerSpinner;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_camera_analyzer, container, false);

        imageView = view.findViewById(R.id.analyzedImage);
        analyzerSpinner = view.findViewById(R.id.analyzer_spinner);

        // Create options for spinner
        String[] options = {"Passthrough", "Grayscale", "Inverted Colors"};

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
                ModularAnalyzer analyzer;
                switch (position) {
                    case 0:
                        analyzer = new PassThroughAnalyzer();
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        CameraController.getInstance().setAnalyzer(analyzer);
                        break;
                    case 1:
                        analyzer = new GrayscaleAnalyzer();
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        CameraController.getInstance().setAnalyzer(analyzer);
                        break;
                    case 2:
                        analyzer = new InvertAnalyzer();
                        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));
                        CameraController.getInstance().setAnalyzer(analyzer);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // optional: do nothing
            }
        });

        return view;
    }

    @Override
    public void onVisible() {
        // Create modular Analyzer to use
        ModularAnalyzer analyzer = new PassThroughAnalyzer();
        analyzer.setListener(bitmap -> imageView.post(() -> imageView.setImageBitmap(bitmap)));

        // Set analyzer for the Camera
        CameraController.getInstance().setAnalyzer(analyzer);
    }

    @Override
    public void onHidden() {
        CameraController.getInstance().setAnalyzer(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
