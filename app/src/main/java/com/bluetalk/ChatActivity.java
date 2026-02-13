package com.bluetalk;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluetalk.adapter.MessageAdapter;
import com.bluetalk.manager.ProfileManager;
import com.bluetalk.model.ChatMessage;
import com.bluetalk.service.BluetoothService;
import com.bluetalk.service.WifiDirectService;
import com.bluetalk.util.CryptoUtils;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private final List<ChatMessage> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private BluetoothService bluetoothService;
    private WifiDirectService wifiDirectService;
    private MediaRecorder recorder;
    private String lastAudioFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RecyclerView recyclerView = findViewById(R.id.messageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);

        EditText inputMessage = findViewById(R.id.messageInput);
        MaterialButton sendButton = findViewById(R.id.sendButton);
        MaterialButton audioButton = findViewById(R.id.audioButton);

        ProfileManager profileManager = new ProfileManager(this);

        bluetoothService = new BluetoothService(this, null);
        wifiDirectService = new WifiDirectService(this, peerName -> { });

        sendButton.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                return;
            }
            sendEncryptedText(profileManager.getNickname(), text);
            inputMessage.setText("");
        });

        audioButton.setEnabled(BuildConfig.FEATURE_AUDIO);
        audioButton.setOnTouchListener((v, event) -> {
            if (!BuildConfig.FEATURE_AUDIO) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startRecording();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                stopRecordingAndSend();
                return true;
            }
            return false;
        });
    }

    private void sendEncryptedText(String sender, String message) {
        try {
            String encrypted = CryptoUtils.encrypt(message);
            if (BuildConfig.FEATURE_WIFI_DIRECT && wifiDirectService.isWifiDirectAvailable()) {
                wifiDirectService.sendMessage(encrypted);
            } else {
                bluetoothService.sendMessage(encrypted);
            }

            messages.add(new ChatMessage(sender, message, System.currentTimeMillis(), ChatMessage.Type.TEXT, true));
            messageAdapter.notifyItemInserted(messages.size() - 1);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao criptografar/enviar mensagem", Toast.LENGTH_SHORT).show();
        }
    }

    public void onIncomingEncryptedMessage(String sender, String encryptedMessage) {
        try {
            String decrypted = CryptoUtils.decrypt(encryptedMessage);
            messages.add(new ChatMessage(sender, decrypted, System.currentTimeMillis(), ChatMessage.Type.TEXT, false));
            runOnUiThread(() -> messageAdapter.notifyItemInserted(messages.size() - 1));
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Erro ao descriptografar mensagem", Toast.LENGTH_SHORT).show());
        }
    }

    private void startRecording() {
        if (!BuildConfig.FEATURE_AUDIO) return;
        try {
            File audioFile = new File(getCacheDir(), "walkie_" + System.currentTimeMillis() + ".3gp");
            lastAudioFilePath = audioFile.getAbsolutePath();
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(lastAudioFilePath);
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            Toast.makeText(this, "Falha ao iniciar gravação", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndSend() {
        if (recorder == null) return;
        try {
            recorder.stop();
            recorder.release();
            recorder = null;

            String payload = "[AUDIO]" + lastAudioFilePath;
            if (BuildConfig.FEATURE_WIFI_DIRECT && wifiDirectService.isWifiDirectAvailable()) {
                wifiDirectService.sendMessage(payload);
            } else {
                bluetoothService.sendMessage(payload);
            }

            messages.add(new ChatMessage("Você", "🎙 Áudio enviado", System.currentTimeMillis(), ChatMessage.Type.AUDIO, true));
            messageAdapter.notifyItemInserted(messages.size() - 1);
        } catch (RuntimeException e) {
            Toast.makeText(this, "Falha ao finalizar gravação", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio(String audioPath) {
        if (audioPath == null) return;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(audioPath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao reproduzir áudio", Toast.LENGTH_SHORT).show();
        }
    }
}
