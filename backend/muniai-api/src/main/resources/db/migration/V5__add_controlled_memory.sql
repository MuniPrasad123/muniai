CREATE TABLE user_memory (
    id UUID PRIMARY KEY,
    category VARCHAR(20) NOT NULL,
    memory_key VARCHAR(100) NOT NULL,
    memory_value VARCHAR(1000) NOT NULL,
    normalized_value VARCHAR(1000) NOT NULL,
    display_label VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_conversation_id UUID,
    source_message_id UUID,
    confidence DOUBLE PRECISION,
    approved_at TIMESTAMP WITH TIME ZONE,
    disabled_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    superseded_by_memory_id UUID,
    last_used_at TIMESTAMP WITH TIME ZONE,
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_user_memory_category CHECK (category IN ('PROFILE','PREFERENCE','GOAL','SKILL','PROJECT','CONTEXT','CUSTOM')),
    CONSTRAINT ck_user_memory_status CHECK (status IN ('PROPOSED','ACTIVE','DISABLED','SUPERSEDED','REJECTED','DELETED')),
    CONSTRAINT ck_user_memory_source CHECK (source_type IN ('EXPLICIT_USER_REQUEST','ASSISTANT_SUGGESTION','MANUAL_ENTRY')),
    CONSTRAINT ck_user_memory_value CHECK (CHAR_LENGTH(TRIM(memory_value)) > 0),
    CONSTRAINT ck_user_memory_usage CHECK (usage_count >= 0),
    CONSTRAINT fk_user_memory_conversation FOREIGN KEY (source_conversation_id) REFERENCES conversations(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_memory_message FOREIGN KEY (source_message_id) REFERENCES messages(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_memory_superseded_by FOREIGN KEY (superseded_by_memory_id) REFERENCES user_memory(id) ON DELETE SET NULL
);

CREATE INDEX idx_user_memory_status ON user_memory(status);
CREATE INDEX idx_user_memory_category ON user_memory(category);
CREATE INDEX idx_user_memory_key ON user_memory(memory_key);
CREATE INDEX idx_user_memory_updated_at ON user_memory(updated_at DESC);

CREATE TABLE message_memory_usage (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    memory_id UUID,
    category VARCHAR(20) NOT NULL,
    memory_key VARCHAR(100) NOT NULL,
    display_label VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_message_memory_usage_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_memory_usage_memory FOREIGN KEY (memory_id) REFERENCES user_memory(id) ON DELETE SET NULL,
    CONSTRAINT uq_message_memory_usage UNIQUE (message_id, memory_id)
);

CREATE INDEX idx_message_memory_usage_message ON message_memory_usage(message_id);
