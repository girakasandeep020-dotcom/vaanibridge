package com.sandeep.vaanibridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TranslationService {

    @Value("${vaanibridge.translation.api-key:}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Translates text using Google Cloud Translation REST API (v2).
     * Falls back to a simulated translation if no API key is provided.
     */
    public String translate(String text, String sourceLang, String targetLang) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            return "[Simulated " + targetLang + "]: " + text;
        }

        String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;

        RequestBody body = new FormBody.Builder()
                .add("q", text)
                .add("source", sourceLang)
                .add("target", targetLang)
                .add("format", "text")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Translation API error: " + response.code() + " " + response.message());
            }

            JsonNode root = objectMapper.readTree(response.body().string());
            return root.path("data").path("translations").get(0).path("translatedText").asText();
        }
    }
}
