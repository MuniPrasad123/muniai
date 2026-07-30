package com.muniai.conversation.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageCitationRepository extends JpaRepository<MessageCitationEntity, UUID> {
    List<MessageCitationEntity> findByMessage_IdOrderByCitationIndex(UUID messageId);
}
