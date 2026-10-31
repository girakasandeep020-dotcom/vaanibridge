package com.sandeep.vaanibridge;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * VaaniBridge Phase 11: Real-Time Translation API Integration
 * Handles live translation requests via the Spring Boot backend proxy.
 */
public class TranslationManager {

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final SessionManager sessionManager;

    public interface TranslationListener {
        void onTranslationSuccess(String originalText, String translatedText);
        void onTranslationError(String error);
    }

    public TranslationManager(String baseUrl, SessionManager sessionManager) {
        // Use the centralized NetworkClient to handle Render cold starts and logging (60s timeouts)
        this.httpClient = NetworkClient.getOkHttpClient();
        this.baseUrl = baseUrl;
        this.sessionManager = sessionManager;
    }

    /**
     * Translates Telugu text to Hindi text.
     */
    public void translateTeluguToHindi(String text, TranslationListener listener) {
        performTranslation(text, "te", "hi", listener);
    }

    /**
     * Translates Hindi text to Telugu text.
     */
    public void translateHindiToTelugu(String text, TranslationListener listener) {
        performTranslation(text, "hi", "te", listener);
    }

    private void performTranslation(String text, String sourceLang, String targetLang, TranslationListener listener) {
        if (text == null || text.isEmpty()) {
            listener.onTranslationError("Empty text");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("sourceLang", sourceLang);
            json.put("targetLang", targetLang);
        } catch (Exception e) {
            listener.onTranslationError("JSON Error: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/translate")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onTranslationError("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String translatedText = response.body().string();
                    listener.onTranslationSuccess(text, translatedText);
                } else {
                    listener.onTranslationError("Server Error: " + response.code());
                }
            }
        });
    }
}