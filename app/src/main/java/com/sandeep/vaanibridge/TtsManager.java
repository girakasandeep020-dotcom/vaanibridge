package com.sandeep.vaanibridge;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TtsManager {
    private static final String TAG = "VaaniBridge_TTS";
    private TextToSpeech tts;
    private boolean isInitialized = false;

    public interface TtsInitListener {
        void onInitSuccess();
        void onInitError(String message);
    }

    public TtsManager(Context context, TtsInitListener listener) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Default to Hindi for the initial check
                int result = tts.setLanguage(new Locale("hi", "IN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Hindi language is not supported or missing data");
                    // We don't fail entirely as Telugu might be available, but it's a warning
                }
                isInitialized = true;
                listener.onInitSuccess();
            } else {
                Log.e(TAG, "TTS Initialization failed");
                listener.onInitError("TTS Initialization failed");
            }
        });
    }

    public void setLanguage(Locale locale) {
        if (isInitialized && tts != null) {
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + locale.getLanguage());
            }
        }
    }

    public void speak(String text, Locale locale) {
        if (isInitialized && text != null && !text.isEmpty()) {
            setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VaaniBridgeTTS");
        }
    }

    public void shutDown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}