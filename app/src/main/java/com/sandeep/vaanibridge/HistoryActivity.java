package com.sandeep.vaanibridge;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sandeep.vaanibridge.databinding.ActivityHistoryBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * VaaniBridge Phase 10: Secure History UI
 * Fetches call history using JWT Authentication and the centralized NetworkClient.
 */
public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "VaaniBridge_History";
    private ActivityHistoryBinding binding;
    private CallHistoryAdapter adapter;
    private SessionManager sessionManager;
    
    // Using the centralized NetworkClient (60s timeouts, logging enabled)
    private final OkHttpClient httpClient = NetworkClient.getOkHttpClient();
    
    private static final String BASE_URL = Config.BASE_URL; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupToolbar();
        setupRecyclerView();
        fetchHistory();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CallHistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void fetchHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        // Fetch logs for the authenticated user
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/logs/history/" + sessionManager.getUsername())
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "History fetch failed", e);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(HistoryActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonData);
                        List<CallLogModel> logs = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            CallLogModel log = new CallLogModel();
                            log.setReceiverPhoneNumber(obj.optString("receiverPhoneNumber", "N/A"));
                            log.setCallStatus(obj.optString("callStatus", "SUCCESS"));
                            log.setCallStartTime(obj.optString("callStartTime", ""));
                            log.setTotalTranslationsCount(obj.optInt("totalTranslationsCount", 0));
                            log.setAverageLatencyMs(obj.optLong("averageLatencyMs", 0));
                            log.setCallerLanguage(obj.optString("callerLanguage", "te-IN"));
                            log.setReceiverLanguage(obj.optString("receiverLanguage", "hi-IN"));
                            logs.add(log);
                        }

                        runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            if (logs.isEmpty()) {
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                adapter.setLogs(logs);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "JSON Parsing error", e);
                        runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
                    }
                } else if (response.code() == 401) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(HistoryActivity.this, "Session Expired", Toast.LENGTH_SHORT).show();
                        sessionManager.logoutUser();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(HistoryActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
