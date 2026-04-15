package com.smartlab.erp.repository;

import com.smartlab.erp.entity.InternalMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalMessageRepository extends JpaRepository<InternalMessage, Long> {
    List<InternalMessage> findByRecipientUserIdOrderByCreatedAtDesc(String recipientUserId);

    long countByRecipientUserIdAndReadFalse(String recipientUserId);
}
