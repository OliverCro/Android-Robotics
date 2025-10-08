//*******************************************************************
/*!
\file   MainActivity.java
\author Thomas Breuer
\date   30.09.2025
\brief  Activity Demo
*/

//*******************************************************************
package com.hbrs;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

//*********************************************************************
public class MainActivity extends AppCompatActivity
{
    private static  String TAG = "Test";

    //-----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    //-----------------------------------------------------------------
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    //-----------------------------------------------------------------
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    //-----------------------------------------------------------------
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    //-----------------------------------------------------------------
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    //-----------------------------------------------------------------
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    //-----------------------------------------------------------------
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    //-----------------------------------------------------------------
    /*
    add to AndroidManifest.xml:
     <application
       ...
        <activity
            ...
            android:configChanges="orientation|keyboard|keyboardHidden|screenLayout|uiMode|screenSize|smallestScreenSize"
            >
          
    */
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);
        Log.i(TAG, "onConfigurationChanged");
    }
}