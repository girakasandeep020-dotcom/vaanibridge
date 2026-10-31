package com.sandeep.vaanibridge.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "call_logs")
public class CallLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callerPhoneNumber;
    private String receiverPhoneNumber;
    private String callerLanguage;
    private String receiverLanguage;

    private LocalDateTime callStartTime;
    private LocalDateTime callEndTime;
    
    private Integer totalTranslationsCount;
    
    @Enumerated(EnumType.STRING)
    private CallStatus callStatus;

    private Long averageLatencyMs;

    public enum CallStatus {
        SUCCESS, FAILED, BUSY, DROPPED
    }
}
