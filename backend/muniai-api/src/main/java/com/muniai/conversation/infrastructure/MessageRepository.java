package com.muniai.conversation.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    List<MessageEntity> findByConversation_IdOrderByCreatedAtAscIdAsc(UUID conversationId);
    long countByConversation_Id(UUID conversationId);
}
