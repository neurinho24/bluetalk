package com.bluetalk.service;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WifiDirectService {

    public interface PeerListener {
        void onPeerFound(String peerName);
    }

    private static final String TAG = "WifiDirectService";

    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final PeerListener peerListener;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public WifiDirectService(Context context, PeerListener peerListener) {
        this.peerListener = peerListener;
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager != null ? manager.initialize(context, context.getMainLooper(), null) : null;
    }

    public boolean isWifiDirectAvailable() {
        return manager != null && channel != null;
    }

    public void discoverPeers() {
        if (!isWifiDirectAvailable()) {
            return;
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.requestPeers(channel, WifiDirectService.this::onPeersAvailable);
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "Falha ao descobrir peers. reason=" + reason);
            }
        });
    }

    private void onPeersAvailable(WifiP2pDeviceList peers) {
        Collection<WifiP2pDevice> list = peers.getDeviceList();
        for (WifiP2pDevice device : list) {
            if (peerListener != null) {
                peerListener.onPeerFound(device.deviceName);
            }
        }
    }

    public void connect(WifiP2pDevice device) {
        if (!isWifiDirectAvailable() || device == null) {
            return;
        }

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Conectado ao peer Wi-Fi Direct");
            }

            @Override
            public void onFailure(int reason) {
                Log.w(TAG, "Falha ao conectar no peer. reason=" + reason);
            }
        });
    }

    public void sendMessage(String encryptedPayload) {
        ioExecutor.execute(() -> Log.d(TAG, "Mensagem enviada via Wi-Fi Direct (simulação): " + encryptedPayload));
    }

    public void stop() {
        ioExecutor.shutdownNow();
    }
}
