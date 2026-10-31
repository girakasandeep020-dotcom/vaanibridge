package com.sandeep.vaanibridge;

/**
 * VaaniBridge Phase 9: Data Model
 * Maps backend JSON fields for call history.
 */
public class CallLogModel {
    private String receiverPhoneNumber;
    private String callStartTime;
    private int totalTranslationsCount;
    private long averageLatencyMs;
    private String callStatus;
    private String callerLanguage;
    private String receiverLanguage;

    public CallLogModel() {}

    // Getters and Setters
    public String getReceiverPhoneNumber() { return receiverPhoneNumber; }
    public void setReceiverPhoneNumber(String receiverPhoneNumber) { this.receiverPhoneNumber = receiverPhoneNumber; }

    public String getCallStartTime() { return callStartTime; }
    public void setCallStartTime(String callStartTime) { this.callStartTime = callStartTime; }

    public int getTotalTranslationsCount() { return totalTranslationsCount; }
    public void setTotalTranslationsCount(int totalTranslationsCount) { this.totalTranslationsCount = totalTranslationsCount; }

    public long getAverageLatencyMs() { return averageLatencyMs; }
    public void setAverageLatencyMs(long averageLatencyMs) { this.averageLatencyMs = averageLatencyMs; }

    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }

    public String getCallerLanguage() { return callerLanguage; }
    public void setCallerLanguage(String callerLanguage) { this.callerLanguage = callerLanguage; }

    public String getReceiverLanguage() { return receiverLanguage; }
    public void setReceiverLanguage(String receiverLanguage) { this.receiverLanguage = receiverLanguage; }
}
