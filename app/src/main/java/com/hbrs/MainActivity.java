package com.hbrs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.hbrs.Bluetooth.BT_DeviceListActivity;
import com.hbrs.ORB.ORB;
import com.hbrs.ORB.ORBManager;
import com.hbrs.Utils.ButtonStateHelper;
import com.hbrs.Views.CameraFragment;
import com.hbrs.Views.HomeFragment;
import com.hbrs.Views.AnalogControlFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    private ORB orb;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private Button drawerConnectBtn;
    private boolean isConnectingIntentActive = false;

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED,
        PERMISSION
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an orb
        orb = ORBManager.getInstance(this);

        orb.setLostConnectionListener(() -> runOnUiThread(() -> {
            setConnectionState(ConnectionState.DISCONNECTED);
            Toast.makeText(this, "Connection Lost!", Toast.LENGTH_SHORT).show();
        }));

        // Toolbar + Drawer setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        drawerConnectBtn = headerView.findViewById(R.id.btn_connect);
        drawerConnectBtn.setOnClickListener(this::OnClickConnect);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new HomeFragment())
                    .commit();
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_analog) {
            // Create JoystickFragment and set to contentFrame
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new AnalogControlFragment())
                    .commit();
        } else if (id == R.id.nav_camera) {
            // Create CameraFragment and set to contentFrame
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new CameraFragment())
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void OnClickConnect(View view) {
        if (isConnectingIntentActive) {
            return;
        }

        if (connectionState != ConnectionState.CONNECTED) {
            BT_DeviceListActivity.start(this, 50);
        }

        switch (connectionState) {
            case DISCONNECTED:
            case FAILED:
            case PERMISSION:
                setConnectionState(ConnectionState.CONNECTING);
                isConnectingIntentActive = true;
                OnClickConnect(drawerConnectBtn);
                break;

            case CONNECTED:
                // Disconnect from ORB
                orb.closeBT();
                setConnectionState(ConnectionState.DISCONNECTED);
                break;

            case CONNECTING:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode)
        {
            case 50:
                isConnectingIntentActive = false;

                switch(resultCode)
                {
                    case BT_DeviceListActivity.RESULT_OK:
                        Log.i("Test",BT_DeviceListActivity.getDeviceFromIntent(data).toString());
                        setConnectionState(ConnectionState.CONNECTING);

                        orb.openBT(BT_DeviceListActivity.getDeviceFromIntent(data), new ORB.ConnectionCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    setConnectionState(ConnectionState.CONNECTED);
                                    Log.i("Test", "Connection established");
                                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                                    ORBManager.ConfigureMotors();
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                runOnUiThread(() -> {
                                    setConnectionState(ConnectionState.FAILED);
                                    Log.i("Test", "Connection Failed");
                                    Toast.makeText(MainActivity.this, "Failed connection", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });

                        break;

                    case BT_DeviceListActivity.RESULT_CANCELED:
                        Log.i("Test","canceled");
                        setConnectionState(ConnectionState.DISCONNECTED);
                        break;

                    case BT_DeviceListActivity.RESULT_NOPERMISSION:
                        Log.i("Test", "No Bluetooth permission");
                        setConnectionState(ConnectionState.PERMISSION);
                        break;

                    default:
                        Log.i("Test", "No match in result code");
                        setConnectionState(ConnectionState.FAILED);
                        break;
                }
                break;
        }
    }

    public void setConnectionState(ConnectionState newState) {
        connectionState = newState;

        // Update drawer Button
        ButtonStateHelper.updateButton(drawerConnectBtn, newState);

        // Update HomeFragment button if visible
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.content_frame);

        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).updateButtonState(newState);
        }
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    protected void onDestroy() {
        orb.close();
        super.onDestroy();
    }
}
