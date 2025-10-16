package com.hbrs;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.hbrs.Bluetooth.BT_DeviceListActivity;
import com.hbrs.ORB.ORB;
import com.hbrs.ORB.ORBManager;
import com.hbrs.Views.CameraFragment;
import com.hbrs.Views.HomeFragment;
import com.hbrs.Views.JoystickFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ORB orb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orb = ORBManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Header connect button
        View headerView = navigationView.getHeaderView(0);
        Button connectBtn = headerView.findViewById(R.id.btn_connect);
        connectBtn.setOnClickListener(v -> OnClickConnect(v));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        // Set HomeFragment when app is created
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new HomeFragment())
                    .commit();
        }

        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_joystick) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new JoystickFragment())
                    .commit();
        } else if (id == R.id.nav_camera) {
            // TODO: add camera fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new CameraFragment())
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void OnClickConnect(View view) {
        BT_DeviceListActivity.start(this, 50);
    }

    @Override
    protected void onDestroy() {
        orb.close();
        super.onDestroy();
    }
}
