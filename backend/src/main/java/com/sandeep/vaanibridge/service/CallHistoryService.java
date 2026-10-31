package com.sandeep.vaanibridge.service;

import com.sandeep.vaanibridge.model.CallLog;
import com.sandeep.vaanibridge.repository.CallLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CallHistoryService {

    @Autowired
    private CallLogRepository callLogRepository;

    public CallLog saveCallLog(CallLog callLog) {
        return callLogRepository.save(callLog);
    }

    public List<CallLog> getUserCallHistory(String callerId) {
        return callLogRepository.findByCallerPhoneNumberOrderByCallStartTimeDesc(callerId);
    }
}
