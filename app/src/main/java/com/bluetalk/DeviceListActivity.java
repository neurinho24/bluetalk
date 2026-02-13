package com.bluetalk;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluetalk.adapter.DeviceAdapter;
import com.bluetalk.manager.NotificationHelper;
import com.bluetalk.service.BluetoothService;
import com.bluetalk.service.WifiDirectService;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {

    private final List<String> devices = new ArrayList<>();
    private DeviceAdapter adapter;
    private BluetoothService bluetoothService;
    private WifiDirectService wifiDirectService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        RecyclerView recyclerView = findViewById(R.id.deviceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(devices, device -> Toast.makeText(this, "Selecionado: " + device, Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);

        NotificationHelper notificationHelper = new NotificationHelper(this);
        bluetoothService = new BluetoothService(this, new BluetoothService.DeviceDiscoveryListener() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                String displayName = "🔵 " + (device.getName() == null ? "Dispositivo sem nome" : device.getName());
                if (!devices.contains(displayName)) {
                    devices.add(displayName);
                    runOnUiThread(() -> {
                        adapter.notifyItemInserted(devices.size() - 1);
                        notificationHelper.notifyNearbyDevice(displayName);
                    });
                }
            }

            @Override
            public void onDiscoveryError(String reason) {
                runOnUiThread(() -> Toast.makeText(DeviceListActivity.this, reason, Toast.LENGTH_SHORT).show());
            }
        });

        wifiDirectService = new WifiDirectService(this, peerName -> {
            String displayName = "🔵 " + peerName;
            if (!devices.contains(displayName)) {
                devices.add(displayName);
                runOnUiThread(() -> {
                    adapter.notifyItemInserted(devices.size() - 1);
                    notificationHelper.notifyNearbyDevice(displayName);
                });
            }
        });

        MaterialButton scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            if (BuildConfig.FEATURE_WIFI_DIRECT) {
                wifiDirectService.discoverPeers();
            }
            bluetoothService.startDiscovery();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothService.stop();
        wifiDirectService.stop();
    }
}
