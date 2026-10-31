package com.sandeep.vaanibridge;

/**
 * VaaniBridge Global Configuration
 * Centralized production endpoints for Render deployment.
 */
public class Config {

    // --- PRODUCTION ENDPOINTS (Render) ---
    public static final String BASE_URL = "https://vaanibridge.onrender.com";
    public static final String WS_URL = "wss://vaanibridge.onrender.com/ws/audio";

    // --- TIMEOUTS (Optimized for Cold Starts) ---
    public static final int CONNECT_TIMEOUT_SECONDS = 60;
    public static final int READ_TIMEOUT_SECONDS = 60;

    // Translation Config
    public static final String SOURCE_LANG_CODE = "te";
    public static final String TARGET_LANG_CODE = "hi";
}
