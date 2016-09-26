package br.iesb.messapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.iesb.messapp.adapters.BlueToothListAdapter;
import br.iesb.messapp.model.Device;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 8000;
    private static final UUID BLUETOOTH_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");
    private static final String BLUETOOTH_NAME = "messapp";

    private ProgressDialog progress;
    private BlueToothListAdapter blueToothListAdapter;
    private Realm realm;
    private RealmConfiguration realmConfig;
    private List<Device> deviceList;
    private BluetoothAdapter bluetoothAdapter;
    private boolean loadedBondedDevices = false;

    private RecyclerView btRecyclerView;
    private RecyclerView.Adapter btListAdapter;
    private RecyclerView.LayoutManager btLayoutManager;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                progress = ProgressDialog.show(BluetoothActivity.this,
                        getResources().getString(R.string.progress_bt_title),
                        getResources().getString(R.string.progress_bt_desc));
                progress.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progress.dismiss();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                createOrUpdateDevice(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabBluetooth);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBlueTooth();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deviceList = new ArrayList<>();
        btRecyclerView = (RecyclerView) findViewById(R.id.bluetooth_recycler_view);
        btRecyclerView.setHasFixedSize(true);
        btLayoutManager = new LinearLayoutManager(this);
        btRecyclerView.setLayoutManager(btLayoutManager);
        btListAdapter = new BlueToothListAdapter(deviceList){
            @Override
            public void onClickListener(int position) {

            }
        };

        btRecyclerView.setAdapter(btListAdapter);

        createReceiver();
        checkBlueTooth();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);
        super.onStop();
    }

    private void createReceiver(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
    }

    private void LoadBondedDevices(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice bluetoothDevice : pairedDevices){
                createOrUpdateDevice(bluetoothDevice);
            }
        }
        loadedBondedDevices = true;
    }

    private void continueDoDiscovery(){
        if (!loadedBondedDevices){
            LoadBondedDevices();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void createOrUpdateDevice(BluetoothDevice bluetoothDevice){
        String mac = bluetoothDevice.getAddress();
        String name = bluetoothDevice.getName();
        Device device = realm.where(Device.class).equalTo("mac", mac).findFirst();
        realm.beginTransaction();
        if (device == null) {
            device = realm.createObject(Device.class);
            device.setMac(bluetoothDevice.getAddress());
        }
        device.setName(name);
        realm.commitTransaction();
        if (!deviceList.contains(device)) {
            deviceList.add(device);
        }
        btListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSIONS:
                if (grantResults.length >0) {
                    boolean permissionsGranted = true;
                    for (int grantResult : grantResults) {
                        permissionsGranted = permissionsGranted & (grantResult == PackageManager.PERMISSION_GRANTED);
                        //break;
                    }
                    if (permissionsGranted){
                        continueDoDiscovery();
                    }
                }
                break;
        }
    }

    private void checkBlueTooth(){
        //String[] requestPermissions = new String[2];
        List<String> requestPermissions = new ArrayList<>();

        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        if (hasPermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions.add(Manifest.permission.BLUETOOTH);
        }

        int newPermissions = requestPermissions.size();

        if (newPermissions == 0){
            continueDoDiscovery();
        } else {
            String[] permissions =  requestPermissions.toArray(new String[newPermissions]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    /*private void sendContact(Device device){
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = bluetoothAdapter
                            .listenUsingRfcommWithServiceRecord(BLUETOOTH_NAME, BLUETOOTH_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }*/

}
