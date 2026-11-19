package com.muniai.conversation.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.muniai.memory.domain.UsedMemory;

public record ConversationMessage(UUID id, UUID conversationId, MessageRole role, String content, String model,
                                  ChatMode mode, List<MessageCitation> citations, List<UsedMemory> usedMemories,
                                  Instant createdAt) {
    public ConversationMessage(UUID id,UUID conversationId,MessageRole role,String content,String model,ChatMode mode,
                               List<MessageCitation> citations,Instant createdAt){
        this(id,conversationId,role,content,model,mode,citations,List.of(),createdAt);
    }
}
