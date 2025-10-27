//*******************************************************************
/*!
\file   BT_DeviceListActivity.java
\author Thomas Breuer
\date   21.02.2019
\brief
*/
package com.hbrs.Bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.core.app.ActivityCompat;
import com.hbrs.R;

import java.util.ArrayList;
import java.util.Set;

//*******************************************************************
public class BT_DeviceListActivity extends Activity
{
    static Activity parent;

    // Custom RESULT_CODES
    public static final int RESULT_NOPERMISSION = Activity.RESULT_FIRST_USER;
    public static final int RESULT_BTOFF = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_DEVICE_NO_BT = Activity.RESULT_FIRST_USER + 2;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    BluetoothAdapter BT_Adapter;
    Set<BluetoothDevice> BT_PairedDevices;
    BluetoothDevice BT_Device;

    public static void  start(Activity _parent, int requestCode)
    {
        parent = _parent;

        Intent serverIntent = new Intent(parent, BT_DeviceListActivity.class);
        parent.startActivityForResult(serverIntent, requestCode);
    }

    public static BluetoothDevice getDeviceFromIntent(Intent data) {
        String addr = data.getExtras().getString(BT_DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        if (addr != null && addr.length() > 0) {
            return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
        }
        return null;
    }

    //---------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Check if app is allowed to use Bluetooth
        if (ActivityCompat.checkSelfPermission(parent, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");

            // Set result and finish this Activity
            setResult(RESULT_NOPERMISSION, intent);
            finish();
            return;
        }

        // BT is allowed
        BT_Adapter = BluetoothAdapter.getDefaultAdapter();

        // Check if Bluetooth is supported
        if (BT_Adapter == null) {

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
            setResult(RESULT_DEVICE_NO_BT, intent);
            finish();
            return;
        }

        // Check if Bluetooth is enabled
        if (!BT_Adapter.isEnabled()) {

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
            setResult(RESULT_BTOFF, intent);
            finish();
            return;
        }

        setContentView(R.layout.device_list);
        createDeviceList();
    }

    //---------------------------------------------------------------
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    //---------------------------------------------------------------
    class myOnItemClickListener implements AdapterView.OnItemClickListener
    {
        //-----------------------------------------------------------
        @Override
        public void onItemClick(AdapterView<?> arg0, android.view.View arg1, int pos, long id) {
            BT_Device = (BluetoothDevice) BT_PairedDevices.toArray()[pos];
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, BT_Device.getAddress());

            // Set result and finish this Activity
            setResult(RESULT_OK, intent);
            finish();
        }
    }



    //---------------------------------------------------------------
    public void createDeviceList()
    {
        BT_Adapter       = BluetoothAdapter.getDefaultAdapter();
        BT_PairedDevices = BT_Adapter.getBondedDevices();

        ArrayList<String> list = new ArrayList<String>();

        for(BluetoothDevice BT_Device : BT_PairedDevices)
        {
            list.add(new String( BT_Device.getName()));
        }

        final ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                list);

        ListView lv = (ListView)findViewById(R.id.paired_devices);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener( new myOnItemClickListener() );
    }
}
