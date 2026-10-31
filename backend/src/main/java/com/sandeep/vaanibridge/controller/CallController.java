package com.sandeep.vaanibridge.controller;

import com.sandeep.vaanibridge.dto.CallRequest;
import com.sandeep.vaanibridge.service.ExotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*")
public class CallController {

    @Autowired
    private ExotelService exotelService;

    @PostMapping("/initiate")
    public ResponseEntity<String> initiateCall(@RequestBody CallRequest request) {
        try {
            String response = exotelService.initiateCall(request.getTo());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error initiating call: " + e.getMessage());
        }
    }

    @PostMapping("/telecom/webhook/call-status")
    public ResponseEntity<Void> handleCallStatus(@RequestParam("CallSid") String callSid,
                                                @RequestParam("Status") String status) {
        System.out.println("Call SID: " + callSid + ", Status: " + status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telecom/webhook/flow")
    public String handleCallFlow() {
        // Basic Exotel XML flow to bridge audio (simplified)
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Response>\n" +
               "    <Say>Connecting your Vaani Bridge call.</Say>\n" +
               "    <Record action=\"http://your-server/api/telecom/webhook/audio\" method=\"POST\" finishOnKey=\"#\" />\n" +
               "</Response>";
    }
}
