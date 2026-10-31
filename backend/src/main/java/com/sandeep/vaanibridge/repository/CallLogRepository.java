package com.sandeep.vaanibridge.repository;

import com.sandeep.vaanibridge.model.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
    List<CallLog> findByCallerPhoneNumberOrderByCallStartTimeDesc(String callerPhoneNumber);
}
