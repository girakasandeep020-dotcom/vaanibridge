package com.sandeep.vaanibridge.controller;

import com.sandeep.vaanibridge.model.CallLog;
import com.sandeep.vaanibridge.service.CallHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class CallLogController {

    @Autowired
    private CallHistoryService callHistoryService;

    @PostMapping("/save")
    public ResponseEntity<CallLog> saveLog(@RequestBody CallLog callLog) {
        return ResponseEntity.ok(callHistoryService.saveCallLog(callLog));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<CallLog>> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(callHistoryService.getUserCallHistory(userId));
    }
}
