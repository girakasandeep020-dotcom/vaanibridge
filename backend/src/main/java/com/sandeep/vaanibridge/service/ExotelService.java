package com.sandeep.vaanibridge.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExotelService {

    @Value("${exotel.sid}")
    private String sid;

    @Value("${exotel.api-key}")
    private String apiKey;

    @Value("${exotel.api-token}")
    private String apiToken;

    @Value("${exotel.subdomain}")
    private String subdomain;

    @Value("${exotel.caller-id}")
    private String callerId;

    private final OkHttpClient client = new OkHttpClient();

    public String initiateCall(String to) throws IOException {
        String url = String.format("https://%s:%s@%s/v1/Accounts/%s/Calls/connect.json",
                apiKey, apiToken, subdomain, sid);

        RequestBody formBody = new FormBody.Builder()
                .add("From", callerId)
                .add("To", to)
                .add("CallerId", callerId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
}
