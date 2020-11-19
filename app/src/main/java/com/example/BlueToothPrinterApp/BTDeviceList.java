package com.example.BlueToothPrinterApp;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BTDeviceList extends ListActivity {

    static public final int REQUEST_CONNECT_BT = 0x2300;

    static private final int REQUEST_ENABLE_BT = 0x1000;

    static private BluetoothAdapter mBluetoothAdapter = null;

    static private ArrayAdapter<String> listArrayAdapter = null;

    static private ArrayAdapter<BluetoothDevice> btDevices = null;

    private static final UUID SPP_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
// UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    static private BluetoothSocket mbtSocket = null;
    static private BluetoothDevice activeDevice = null;

    public static BluetoothDevice getActiveDevice() {
        return activeDevice;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        tv.setText("Select A Device : ");
        getListView().addHeaderView(tv);
        getListView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setTitle("Bluetooth Devices");

        try {
            if (initDevicesList() != 0) {
                this.finish();
            }
        } catch (Exception ex) {
            this.finish();
        }
        try {
            if (btDevices == null) {
                btDevices = new ArrayAdapter<>(getApplicationContext(), android.R.id.text1);
            }
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                listArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress() + "\n");
                btDevices.add(device);
                listArrayAdapter.notifyDataSetInvalidated();
            }

//            if (btDevices.getPosition(device) < 0) {
//                btDevices.add(device);
//                mArrayAdapter.add(device.getName() + "\n"
//                        + device.getAddress() + "\n");
//                mArrayAdapter.notifyDataSetInvalidated();
//            }
        } catch (Exception ex) {
            ex.fillInStackTrace();
        }
    }

    public static BluetoothSocket getSocket() {
        return mbtSocket;
    }

    private void flushData() {
        try {
            if (mbtSocket != null) {
                mbtSocket.close();
                mbtSocket = null;
            }

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
            }

            if (btDevices != null) {
                btDevices.clear();
                btDevices = null;
            }

            if (listArrayAdapter != null) {
                listArrayAdapter.clear();
                listArrayAdapter.notifyDataSetChanged();
                listArrayAdapter.notifyDataSetInvalidated();
                listArrayAdapter = null;
            }

            finalize();

        } catch (Exception ex) {
        } catch (Throwable e) {
        }

    }

    private int initDevicesList() {

        flushData();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth not supported !!", Toast.LENGTH_LONG).show();
            return -1;
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        listArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1);

        setListAdapter(listArrayAdapter);
        Set<BluetoothDevice> btDeviceList = mBluetoothAdapter
                .getBondedDevices();
        try {
            if (btDeviceList.size() > 0) {
                for (BluetoothDevice device : btDeviceList) {
                    if (!isArrayAdapterHaveDevice(device)) {
                        btDevices.add(device);
                        listArrayAdapter.add(device.getName() + "\n"
                                + device.getAddress());
                        listArrayAdapter.notifyDataSetInvalidated();
                    }
                }
            }
        } catch (Exception ex) {
        }
//        Intent enableBtIntent = new Intent(
//                BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        try {
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        } catch (Exception ex) {
//            return -2;
//        }

        Toast.makeText(getApplicationContext(),
                "Getting all available Bluetooth Devices ( Paired )", Toast.LENGTH_SHORT)
             .show();

        return 0;

    }

//    @Override
//    protected void onActivityResult(int reqCode, int resultCode, Intent intent) {
//        super.onActivityResult(reqCode, resultCode, intent);
//
//        switch (reqCode) {
//            case REQUEST_ENABLE_BT:
//
//                if (resultCode == RESULT_OK) {
//                    Set<BluetoothDevice> btDeviceList = mBluetoothAdapter
//                            .getBondedDevices();
//                    try {
//                        if (btDeviceList.size() > 0) {
//
//                            for (BluetoothDevice device : btDeviceList) {
//                                if (!isArrayAdapterHaveDevice(device)) {
//
//                                    btDevices.add(device);
//
//                                    listArrayAdapter.add(device.getName() + "\n"
//                                            + device.getAddress());
//                                    listArrayAdapter.notifyDataSetInvalidated();
//                                }
//                            }
//                        }
//                    } catch (Exception ex) {
//                    }
//                }
//
//                break;
//        }
//    }

    private boolean isArrayAdapterHaveDevice(BluetoothDevice device) {
        if (listArrayAdapter == null)
            return false;
        for (int i = 0; i < listArrayAdapter.getCount(); i++) {
            if (listArrayAdapter.getItem(i).contains(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position,
                                   long id) {
        final int index = position - 1;
        super.onListItemClick(l, v, index, id);

        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        Toast.makeText(
                getApplicationContext(),
                "Connecting to " + btDevices.getItem(index).getName() + ","
                        + btDevices.getItem(index).getAddress(),
                Toast.LENGTH_SHORT).show();

        Thread connectThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final BluetoothDevice device = btDevices.getItem(index);
                    boolean gotuuid = device.fetchUuidsWithSdp();
                    UUID uuid = device.getUuids()[0].getUuid();
                    activeDevice = device;
                    mbtSocket = device.createRfcommSocketToServiceRecord(uuid);
                    mbtSocket.connect();
                } catch (Throwable ex) {
                    runOnUiThread(socketErrorRunnable);
                    try {
                        mbtSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mbtSocket = null;
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        });

        connectThread.start();
    }

    private Runnable socketErrorRunnable = new Runnable() {

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(),
                    "Cannot establish connection", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.startDiscovery();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, Menu.FIRST, Menu.NONE, "Refresh Scanning");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case Menu.FIRST:
                initDevicesList();
                break;
        }

        return true;
    }
}