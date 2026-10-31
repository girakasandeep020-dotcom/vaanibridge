package com.sandeep.vaanibridge.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String phoneNumber;
}
