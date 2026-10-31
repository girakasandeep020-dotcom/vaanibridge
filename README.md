# VaaniBridge: Real-Time AI Phone Translation Bridge

**VaaniBridge** is an enterprise-grade, full-stack communication platform that enables real-time, bidirectional speech translation between Telugu (te-IN) and Hindi (hi-IN) callers using ordinary PSTN mobile networks.

Built with a focus on low-latency processing and robust security, VaaniBridge allows a smartphone user to communicate with any ordinary mobile user—who requires no internet or app—by bridging digital audio streams with cellular circuits.

---

## 🏗️ System Architecture

The system is partitioned into three core layers:

### 1. Android Client (The Controller)
*   **Speech Recognition Engine:** Continuous loop implementation of Android's `SpeechRecognizer` for real-time Telugu STT.
*   **Audio Capture Manager:** Low-level `AudioRecord` implementation capturing 16kHz Mono 16-bit PCM chunks for binary streaming.
*   **TTS Engine:** Context-aware `TextToSpeech` with dynamic locale switching for Hindi and Telugu playback.
*   **Networking:** Hybrid architecture using **OkHttp** for RESTful commands and **WebSockets** for real-time binary audio telemetry.

### 2. Spring Boot Backend (The Bridge)
*   **Telecom Gateway:** Integration with **Exotel API** to initiate and manage PSTN calls to ordinary Indian mobile numbers.
*   **WebSocket Server:** High-concurrency binary bridge for receiving and processing live PCM audio chunks.
*   **Translation Proxy:** Modular interface for live cloud translation (Google/Azure/Self-hosted) with API-key masking.
*   **Persistence:** Analytics engine tracking session metadata, translation counts, and latency benchmarks via JPA/H2.

### 3. Security Hardening
*   **Authentication:** Stateless **JWT (JSON Web Token)** implementation.
*   **Password Security:** **BCrypt** hashing for user credentials.
*   **Route Protection:** Interceptor-based security for all REST and WebSocket endpoints.
*   **Client Security:** `EncryptedSharedPreferences` for token storage and automatic 401 session expiration handling.

---

## 🛠️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Mobile** | Java (Android API 26+), ViewBinding, Material Components 3 |
| **Backend** | Spring Boot 3.x, Spring Security, Maven |
| **Telecom** | Exotel PSTN Gateway |
| **STT/TTS** | Google Speech/Android Native Engines |
| **Networking** | OkHttp 4, WebSockets, REST |
| **Security** | JJWT (io.jsonwebtoken), BCrypt |
| **Database** | JPA, H2 (Prototype) / MySQL (Production) |

---

## 📊 Analytics & Telemetry
VaaniBridge measures every conversation turn to ensure a natural experience. Built-in metrics include:
*   **STT Duration:** Time taken for local speech-to-text processing.
*   **Translation Latency:** Network round-trip for text conversion.
*   **Total RT Latency:** Target benchmark of <1500ms for seamless dialogue.

---

## 🚀 Setup & Installation

### Backend
1. Clone the repository and navigate to `/backend`.
2. Configure your `application.yml` with Exotel credentials:
   ```yaml
   exotel:
     sid: ${EXOTEL_SID}
     api-key: ${EXOTEL_API_KEY}
     api-token: ${EXOTEL_API_TOKEN}
   ```
3. Run `mvn spring-boot:run`.

### Android
1. Open the project in Android Studio.
2. Update `BASE_URL` and `WS_URL` in `MainActivity.java` with your server's IP.
3. Build and deploy the APK to a physical device (e.g., Samsung Galaxy F34).
4. Register a user account and begin a secure session.

---

## 🛡️ License
Distributed under the MIT License. See `LICENSE` for more information.

Developed by [Sandeep]
