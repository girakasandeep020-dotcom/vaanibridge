package com.sandeep.vaanibridge;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages low-latency PCM audio capture using AudioRecord.
 * Prepared for Phase 6 & 7 streaming.
 */
public class AudioCaptureManager {
    private static final String TAG = "VaaniBridge_Audio";

    // Standard low-latency parameters
    public static final int SAMPLE_RATE = 16000; // 16kHz for voice
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private int bufferSize;
    private final ExecutorService captureExecutor = Executors.newSingleThreadExecutor();

    public interface AudioChunkListener {
        void onChunkCaptured(byte[] chunk);
        void onError(String error);
    }

    private AudioChunkListener listener;

    public AudioCaptureManager(AudioChunkListener listener) {
        this.listener = listener;
        this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    @SuppressLint("MissingPermission")
    public void startCapture() {
        if (isRecording) return;

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                listener.onError("Failed to initialize AudioRecord");
                return;
            }

            audioRecord.startRecording();
            isRecording = true;

            captureExecutor.execute(this::captureLoop);
            Log.d(TAG, "Audio capture started");

        } catch (Exception e) {
            Log.e(TAG, "Error starting capture: " + e.getMessage());
            listener.onError(e.getMessage());
        }
    }

    private void captureLoop() {
        byte[] buffer = new byte[bufferSize / 2]; // Smaller chunks for lower latency

        while (isRecording) {
            int readSize = audioRecord.read(buffer, 0, buffer.length);
            if (readSize > 0) {
                // In a real streaming scenario, we'd send this to a WebSocket
                // For now, we notify the listener for Phase 6 metrics/processing
                byte[] chunk = new byte[readSize];
                System.arraycopy(buffer, 0, chunk, 0, readSize);
                if (listener != null) {
                    listener.onChunkCaptured(chunk);
                }
            } else if (readSize < 0) {
                Log.e(TAG, "Read error: " + readSize);
                break;
            }
        }
    }

    public void stopCapture() {
        isRecording = false;
        if (audioRecord != null) {
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }
        Log.d(TAG, "Audio capture stopped");
    }
}