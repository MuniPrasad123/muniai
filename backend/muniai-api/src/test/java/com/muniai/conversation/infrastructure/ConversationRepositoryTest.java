package com.muniai.conversation.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.muniai.bootstrap.MuniAiApplication;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = MuniAiApplication.class)
class ConversationRepositoryTest {
    @Autowired ConversationRepository conversations;
    @Autowired MessageRepository messages;
    @Autowired JdbcTemplate jdbc;

    @Test
    void ordersByMostRecentlyUpdated() {
        Instant now = Instant.now();
        conversations.save(new ConversationEntity(UUID.randomUUID(), "older", now.minusSeconds(10), now.minusSeconds(10)));
        conversations.save(new ConversationEntity(UUID.randomUUID(), "newer", now, now));
        assertEquals("newer", conversations.findAllByOrderByUpdatedAtDesc().getFirst().getTitle());
    }

    @Test
    void databaseCascadeDeletesMessages() {
        Instant now = Instant.now();
        ConversationEntity conversation = conversations.saveAndFlush(new ConversationEntity(UUID.randomUUID(), "title", now, now));
        messages.saveAndFlush(new MessageEntity(UUID.randomUUID(), conversation,
                com.muniai.conversation.domain.MessageRole.USER, "hello", null, now));
        jdbc.update("delete from conversations where id = ?", conversation.getId());
        assertEquals(0, jdbc.queryForObject("select count(*) from messages", Integer.class));
    }

    @Test
    void flywayMigrationIsApplied() {
        assertEquals(1L, jdbc.queryForObject("select count(*) from \"flyway_schema_history\" where \"version\" = '1' and \"success\" = true", Long.class));
    }
}
