package com.example.navdrawernew;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DeviceCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;

public class BluetoothActivity extends AppCompatActivity {

    List<BluetoothDevice> paired_Devices = new ArrayList<BluetoothDevice>(); //Store Paired Devices
    List<BluetoothDevice>  pairedDevicesList = new ArrayList<>();            // Extra list required
    ArrayAdapter pairedDevicesAdapter;                                       // Adapter for Paired Devices
    List<BluetoothDevice> available_Devices = new ArrayList<BluetoothDevice>();  //Store available devices
    ArrayAdapter availableDevicesAdapter;                                        //Adapter for available devices

    private BluetoothAdapter mbluetoothAdapter;                               //most reliable default source when library fails

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  LAYOUT ELEMENTS  //
    private MyListView availableDevices, pairedDevices;
    private Switch bluetoothSwitch;
    private TextView label_availableDevices, label_pairedDevices;
    private View lineSeperation;
    private Bluetooth bluetooth;
    private Button scanButton;
    private TextView deviceName;

//////////////////////////////////////////   DEFINING REGISTERS FOR BLUETOOTH //////////////////////////////////////////////////////////

    private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothTurningOn() {
        }

        @Override
        public void onBluetoothTurningOff() {
        }

        @Override
        public void onBluetoothOff() {
            //Toast.makeText(BluetoothActivity.this, "Turned off", Toast.LENGTH_LONG).show();
            setLayoutVisibility(false);
        }

        @Override
        public void onBluetoothOn() {

           // Toast.makeText(BluetoothActivity.this, "Turned on", Toast.LENGTH_LONG).show();
            afterON();
            setLayoutVisibility(true);
            // doStuffWhenBluetoothOn() ...
        }

        @Override
        public void onUserDeniedActivation() {
            // handle activation denial...
        }
    };

    private DiscoveryCallback discoveryCallback  = new DiscoveryCallback() {
        @Override
        public void onDiscoveryStarted() {
            if( available_Devices!=null) available_Devices.clear();
            availableDevicesAdapter.notifyDataSetChanged();

            scanButton.setText("SCANNING");
            //Log.d("Discovery", "Started....");
            Toast.makeText(BluetoothActivity.this, "Discovery Started", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDiscoveryFinished() {
            scanButton.setText("SCAN");
            //Log.d("Discovery", "Finished....");
            Toast.makeText(BluetoothActivity.this, "Discovery Finished", Toast.LENGTH_LONG).show();
            //Log.d("DETECTED DEVICES",available_Devices.toString());
            //Log.d("ADAPTER DEVICES",String.valueOf(availableDevicesAdapter.getCount()));
            //Log.d("ADAPTER DEVICES","END");
        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            //Log.d("FOUND!!!", "Found a device" + device.getName());
            //Toast.makeText(BluetoothActivity.this, "FOUND DEVICE", Toast.LENGTH_LONG).show();
            //label_pairedDevices.setText(device.getName());
            available_Devices.add(device);
            availableDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDevicePaired(BluetoothDevice device) {
            paired_Devices.add(device);
            available_Devices.remove(device);
            availableDevicesAdapter.notifyDataSetChanged();
            pairedDevicesAdapter.notifyDataSetChanged();

        }

        @Override
        public void onDeviceUnpaired(BluetoothDevice device) {
            paired_Devices.remove(device);
            pairedDevicesAdapter.notifyDataSetChanged();
            bluetooth.startScanning();
        }

        @Override
        public void onError(int errorCode) {
            Log.d("ERROR","ERROR while unpairing");
            Toast.makeText(BluetoothActivity.this, "SOme ERror ", Toast.LENGTH_LONG).show();

        }
    };

    private DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            Toast.makeText(BluetoothActivity.this, "Connected to "+device.getName(), Toast.LENGTH_LONG).show();
            // bluetooth.send("You There?");
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {

        }

        @Override
        public void onMessage(byte[] message) {

        }

        @Override
        public void onError(int errorCode) {

        }

        @Override
        public void onConnectError(BluetoothDevice device, String message) {
            Log.d("CONNECTION ERROR",message);
        }
    };

///////////// ON CREATE ////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);


        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        ////////////////////////LAYOUT ELEMENTS INITIALIZE //////////////////////////

        bluetoothSwitch = findViewById(R.id.bluetoothSwitch);
        availableDevices = findViewById(R.id.listAvailableDevices);
        pairedDevices = findViewById(R.id.listPairedDevices);
        lineSeperation = findViewById(R.id.lineSeparation);
        label_availableDevices = findViewById(R.id.labelAvailableDevices);
        label_pairedDevices = findViewById(R.id.labelPairedDevices);
        scanButton = findViewById(R.id.scanButton);
        deviceName = findViewById(R.id.deviceName);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");



        ////////////////////////// SETTING UP THE ADAPTERS /////////////////////////

       pairedDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this,  android.R.layout.simple_list_item_single_choice, paired_Devices) {
           @Override
           public View getView(int position, View convertView, ViewGroup parent) {
               TextView view = (TextView) super.getView(position, convertView, parent);
               // Replace text with my own
               view.setText(getItem(position).getName());
               return view;
           }
       };

       availableDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this,  android.R.layout.simple_list_item_single_choice, available_Devices) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                // Replace text with my own
                view.setText(getItem(position).getName());
                return view;
                //
            }
        };

        availableDevices.setAdapter(availableDevicesAdapter);
        pairedDevices.setAdapter(pairedDevicesAdapter);


//////////////////////////////////////  INITIALIZE BLUETOOTH ////////////////////////////////////////////
        bluetooth = new Bluetooth(this);            //initialize
        bluetooth.setBluetoothCallback(bluetoothCallback); //assigning receiver for on/off
        bluetooth.setDiscoveryCallback(discoveryCallback); // assigning receiver for discovery and pairing
        bluetooth.setDeviceCallback(deviceCallback);
        bluetooth.onStart(); //registers receivers implicitly
        deviceName.setText(mbluetoothAdapter.getName());

//////////////////////////////////////////  LISTENERS  ////////////////////////////////////////

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //bluetooth.enable();
                    bluetooth.showEnableDialog(BluetoothActivity.this);
                } else {
                    //bluetooth.stopScanning();
                    bluetooth.disable();
                    Toast.makeText(BluetoothActivity.this, "Turned off", Toast.LENGTH_LONG).show();

                }
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mbluetoothAdapter.isDiscovering()) {

                    bluetooth.startScanning();
                }else {
                    //scanButton.setText("STOP");

                    mbluetoothAdapter.cancelDiscovery(); //using adapter method:cancelDiscovery() because
                                                         //for some reason library's bluetooth.stopScanning() is slow and problematic
                    //bluetooth.stopScanning();

                }

            }
        });

        availableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String name = parent.getItemAtPosition(position).toString();

                BluetoothDevice bt = (BluetoothDevice) parent.getItemAtPosition(position);
                bluetooth.pair(bt);
                Toast.makeText(BluetoothActivity.this, "Pairing with "+bt.getName(), Toast.LENGTH_LONG).show();



                //Log.d("CHECK",bt.getName()+"===="+name);
            }
        });
        pairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice bt = (BluetoothDevice) parent.getItemAtPosition(position);




                AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);

                builder.setTitle(bt.getName());

                builder.setMessage(bt.getAddress());

                builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        bluetooth.connectToDevice(bt);
                        Toast.makeText(BluetoothActivity.this, "Connecting...", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Unpair", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        bluetooth.unpair(bt);
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();


                //Log.d("CHECK",bt.getName()+"===="+name);
            }
        });
        //////////// FOLLOWING LISTENERS ARE ONLY FOR TESTING LAYOUT AND NOT NECESSARY ////////////////////
        /*
        pairedDevices.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        availableDevices.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
*/
//////////////////////////////////////////  RUNTIME PERMISSION CHECK  ////////////////////////////////////////
        //this part is required as devices aren't discovered until runtime premission is allowed once for every app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }
    ///////////// ON START ////////////////

    @Override
    protected void onStart() {
        super.onStart();


        if (mbluetoothAdapter.isEnabled()) {
            //Toast.makeText(BluetoothActivity.this, "Already on", Toast.LENGTH_LONG).show();
            bluetoothCallback.onBluetoothOn();
            //calling this as it is not implictly called due to some reasons
            //bluetooth.showEnableDialog(BluetoothActivity.this);
            bluetoothSwitch.setChecked(true);

        }else{
            setLayoutVisibility(false);
        }

    }
///////////// ON STOP ////////////////

    @Override
    protected void onStop() {
        super.onStop();

    }


///////////// ON DESTROY ////////////////

    @Override
    protected void onDestroy() {
        bluetooth.onStop();
        super.onDestroy();
    }

    ///////////// EXTRAAAA METHODS ////////////////


    public void afterON() {
        paired_Devices.clear();
        pairedDevicesAdapter.notifyDataSetChanged();

       pairedDevicesList = bluetooth.getPairedDevices();
        for (BluetoothDevice b: pairedDevicesList){
            paired_Devices.add(b);
            pairedDevicesAdapter.notifyDataSetChanged();

        }
        //pairedDevices.setAdapter(pairedDevicesAdapter);
        pairedDevicesAdapter.notifyDataSetChanged();
        //Log.d("PAIRED DEVICES",paired_Devices.toString());
       // Log.d("ADAPTER DEVICES",String.valueOf(pairedDevicesAdapter.getCount()));
       // Log.d("ADAPTER DEVICES","END");

        bluetooth.startScanning();
    }

    public void setLayoutVisibility(boolean value){
        if (value==true){

            label_availableDevices.setVisibility(View.VISIBLE);
            label_pairedDevices.setVisibility(View.VISIBLE);
            availableDevices.setVisibility(View.VISIBLE);
            lineSeperation.setVisibility(View.VISIBLE);
            pairedDevices.setVisibility(View.VISIBLE);
            scanButton.setVisibility(View.VISIBLE);

        }else{
            label_availableDevices.setVisibility(View.INVISIBLE);
            label_pairedDevices.setVisibility(View.INVISIBLE);
            availableDevices.setVisibility(View.INVISIBLE);
            lineSeperation.setVisibility(View.INVISIBLE);
            pairedDevices.setVisibility(View.INVISIBLE);
            scanButton.setVisibility(View.INVISIBLE);
        }
    }

}


