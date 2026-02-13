package com.bluetalk.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothService {

    public interface DeviceDiscoveryListener {
        void onDeviceFound(BluetoothDevice device);
        void onDiscoveryError(String reason);
    }

    private static final String TAG = "BluetoothService";
    private static final UUID CHAT_UUID = UUID.fromString("b7f5d6c7-3f2a-4d40-90de-8a5956cdfd6d");

    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final DeviceDiscoveryListener discoveryListener;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && discoveryListener != null) {
                    discoveryListener.onDeviceFound(device);
                }
            }
        }
    };

    public BluetoothService(Context context, DeviceDiscoveryListener discoveryListener) {
        this.context = context.getApplicationContext();
        this.discoveryListener = discoveryListener;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.context.registerReceiver(discoveryReceiver, filter);
    }

    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        if (bluetoothAdapter == null) {
            if (discoveryListener != null) discoveryListener.onDiscoveryError("Bluetooth não suportado no dispositivo");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            if (discoveryListener != null) discoveryListener.onDiscoveryError("Bluetooth está desligado");
            return;
        }
        bluetoothAdapter.startDiscovery();
    }

    public void sendMessage(String encryptedPayload) {
        ioExecutor.execute(() -> {
            // Implementação base de thread de envio.
            // A conexão com BluetoothSocket pode ser criada aqui com CHAT_UUID.
            Log.d(TAG, "Mensagem enviada via Bluetooth (simulação): " + encryptedPayload.getBytes(StandardCharsets.UTF_8).length + " bytes");
        });
    }

    public void stop() {
        try {
            context.unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        ioExecutor.shutdownNow();
    }
}
