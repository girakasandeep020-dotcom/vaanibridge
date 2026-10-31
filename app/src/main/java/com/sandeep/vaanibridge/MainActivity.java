package com.sandeep.vaanibridge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sandeep.vaanibridge.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * VaaniBridge Final Production Client
 * Optimized for Cloud connectivity (Render) with WSS and JWT.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "VaaniBridge_Main";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private ActivityMainBinding binding;
    private SpeechRecognizer speechRecognizer;
    private TtsManager ttsManager;
    private TranslationManager translationManager;
    private AudioCaptureManager audioCaptureManager;
    private SessionManager sessionManager;

    private boolean isCallActive = false;
    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(4);

    // Centralized Network Client with Logging and High Timeouts
    private final OkHttpClient httpClient = NetworkClient.getInstance();

    private WebSocket webSocket;

    // Metrics
    private long totalStartTime;
    private LocalDateTime callStartDateTime;
    private int translationCount = 0;
    private long totalLatencySum = 0;
    private String currentReceiverNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("Secured Session: " + sessionManager.getUsername());
        }

        checkPermissions();
        initializeManagers();
        initializeSpeechRecognizer();
        setupUI();
    }

    private void setupUI() {
        binding.btnSpeakTelugu.setOnClickListener(v -> {
            if (!isCallActive) {
                initiateTelecomCall();
            } else {
                stopRealTimeCall();
            }
        });

        binding.btnSimulateHindi.setOnClickListener(v -> simulateHindiResponse());
        binding.btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        
        binding.tvStatus.setOnLongClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        });
    }

    private void initializeManagers() {
        translationManager = new TranslationManager(Config.BASE_URL, sessionManager);
        ttsManager = new TtsManager(this, new TtsManager.TtsInitListener() {
            @Override
            public void onInitSuccess() { appendToLog("System: TTS Ready"); }
            @Override
            public void onInitError(String message) { appendToLog("System Error: TTS initialization failed"); }
        });

        audioCaptureManager = new AudioCaptureManager(new AudioCaptureManager.AudioChunkListener() {
            @Override
            public void onChunkCaptured(byte[] chunk) {
                if (webSocket != null && isCallActive) {
                    webSocket.send(ByteString.of(chunk));
                }
            }
            @Override
            public void onError(String error) { runOnUiThread(() -> appendToLog("Audio Error: " + error)); }
        });
    }

    private void initiateTelecomCall() {
        currentReceiverNumber = binding.etReceiverNumber.getText().toString().trim();
        if (currentReceiverNumber.isEmpty()) {
            Toast.makeText(this, "Enter mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        updateStatus("WAKING SERVER...");
        appendToLog("System: Initiating secure cloud handshake...");

        JSONObject json = new JSONObject();
        try {
            json.put("to", currentReceiverNumber);
            json.put("callerLanguage", "te-IN");
            json.put("receiverLanguage", "hi-IN");
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(Config.BASE_URL + "/api/calls/initiate")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    appendToLog("Handshake Failed: Server may be sleeping. Retrying...");
                    updateStatus("IDLE");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> startRealTimeCall());
                } else if (response.code() == 401) {
                    runOnUiThread(() -> { sessionManager.logoutUser(); finish(); });
                } else {
                    runOnUiThread(() -> { appendToLog("Server Refused: " + response.code()); updateStatus("IDLE"); });
                }
            }
        });
    }

    private void startRealTimeCall() {
        isCallActive = true;
        callStartDateTime = LocalDateTime.now();
        translationCount = 0;
        totalLatencySum = 0;

        binding.btnSpeakTelugu.setText("End Call");
        updateStatus("BRIDGE ACTIVE");
        appendToLog("System: Bridge Established (WSS).");

        connectWebSocket();
        audioCaptureManager.startCapture();
        restartListening();
    }

    private void connectWebSocket() {
        Request request = new Request.Builder()
                .url(Config.WS_URL)
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .build();

        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                runOnUiThread(() -> appendToLog("System: Binary Audio Stream Active"));
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                runOnUiThread(() -> appendToLog("Stream Error: Check 4G/5G Signal"));
            }
        });
    }

    private void stopRealTimeCall() {
        if (!isCallActive) return;
        isCallActive = false;
        LocalDateTime end = LocalDateTime.now();
        audioCaptureManager.stopCapture();
        if (webSocket != null) { webSocket.close(1000, "End"); webSocket = null; }
        if (speechRecognizer != null) speechRecognizer.stopListening();
        binding.btnSpeakTelugu.setText("1. Start Call (Continuous)");
        updateStatus("IDLE");
        saveCallLogToBackend(end);
    }

    private void handleOutgoingTelugu(String teluguText, long sttDuration) {
        appendToLog("Caller (TE): " + teluguText);
        updateStatus("TRANSLATING");
        translationManager.translateTeluguToHindi(teluguText, new TranslationManager.TranslationListener() {
            @Override
            public void onTranslationSuccess(String orig, String trans) {
                long duration = SystemClock.elapsedRealtime() - totalStartTime;
                runOnUiThread(() -> {
                    translationCount++;
                    totalLatencySum += duration;
                    appendToLog("Receiver (HI): " + trans + " [" + duration + "ms]");
                    updateStatus("STREAMING");
                    ttsManager.speak(trans, new Locale("hi", "IN"));
                });
            }
            @Override
            public void onTranslationError(String error) { runOnUiThread(() -> appendToLog("Error: " + error)); }
        });
    }

    private void saveCallLogToBackend(LocalDateTime end) {
        long avg = translationCount > 0 ? totalLatencySum / translationCount : 0;
        JSONObject log = new JSONObject();
        try {
            log.put("callerPhoneNumber", sessionManager.getUsername());
            log.put("receiverPhoneNumber", currentReceiverNumber);
            log.put("callerLanguage", "te-IN");
            log.put("receiverLanguage", "hi-IN");
            log.put("callStartTime", callStartDateTime.toString());
            log.put("callEndTime", end.toString());
            log.put("totalTranslationsCount", translationCount);
            log.put("callStatus", "SUCCESS");
            log.put("averageLatencyMs", avg);
        } catch (Exception e) {}

        RequestBody body = RequestBody.create(log.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(Config.BASE_URL + "/api/logs/save")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .post(body).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {}
            @Override public void onResponse(@NonNull Call call, @NonNull Response res) { 
                if (res.isSuccessful()) runOnUiThread(() -> appendToLog("Analytics: Logged."));
            }
        });
    }

    private void updateStatus(String status) { runOnUiThread(() -> binding.tvStatus.setText("Status: " + status)); }
    private void appendToLog(String msg) {
        runOnUiThread(() -> {
            String current = binding.tvLiveLog.getText().toString();
            if (current.contains("---")) current = "";
            binding.tvLiveLog.setText(current + (current.isEmpty() ? "" : "\n") + msg);
            binding.scrollLog.post(() -> binding.scrollLog.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { updateStatus("LISTENING"); }
                @Override public void onBeginningOfSpeech() { totalStartTime = SystemClock.elapsedRealtime(); }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (data != null && !data.isEmpty()) handleOutgoingTelugu(data.get(0), 0);
                    if (isCallActive) restartListening();
                }
                @Override public void onError(int error) { if (isCallActive) restartListening(); }
                @Override public void onEndOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void restartListening() {
        if (!isCallActive) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN");
        speechRecognizer.startListening(intent);
    }

    private void simulateHindiResponse() {
        String resp = "नमस्ते, मैं आपकी सहायता कैसे कर सकता हूँ?";
        appendToLog("Receiver (HI): " + resp);
        updateStatus("TRANSLATING");
        translationManager.translateHindiToTelugu(resp, new TranslationManager.TranslationListener() {
            @Override public void onTranslationSuccess(String orig, String trans) {
                runOnUiThread(() -> { appendToLog("Caller (TE): " + trans); ttsManager.speak(trans, new Locale("te", "IN")); });
            }
            @Override public void onTranslationError(String error) {}
        });
    }

    private String getErrorText(int error) { return "STT Error " + error; }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopRealTimeCall();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (ttsManager != null) ttsManager.shutDown();
        processingExecutor.shutdown();
    }
}