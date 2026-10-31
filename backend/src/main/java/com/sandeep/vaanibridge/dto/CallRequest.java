package com.sandeep.vaanibridge.dto;

import lombok.Data;

@Data
public class CallRequest {
    private String to;
    private String callerId;
    private String callerLanguage;
    private String receiverLanguage;
}
