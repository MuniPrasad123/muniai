package com.muniai.conversation.api;

import com.muniai.conversation.domain.MessageRole;
import com.muniai.conversation.domain.ChatMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ConversationDtos {
    private ConversationDtos() {}

    public record RenameRequest(@NotBlank @Size(max = 200) String title) {}
    public record SendMessageRequest(@NotBlank String message, ChatMode mode, List<UUID> documentIds,
                                     @Min(1) @Max(20) Integer topK,
                                     @DecimalMin("0.0") @DecimalMax("1.0") Double similarityThreshold) {
        public ChatMode effectiveMode(){return mode==null?ChatMode.NORMAL:mode;}
        public java.util.Set<UUID> selectedDocuments(){
            return documentIds==null?java.util.Set.of():new java.util.LinkedHashSet<>(documentIds);
        }
    }
    public record CitationResponse(UUID citationId,int citationIndex,UUID documentId,String originalFileName,
            UUID chunkId,int chunkIndex,Integer pageNumber,double similarityScore,String contentPreview){}
    public record MessageResponse(UUID id, UUID conversationId, MessageRole role, String content,
                                  String model, ChatMode mode, List<CitationResponse> citations, Instant createdAt) {}
    public record ConversationResponse(UUID id, String title, Instant createdAt, Instant updatedAt,
                                       List<MessageResponse> messages) {}
    public record SendMessageResponse(UUID conversationId, MessageResponse userMessage,
                                      MessageResponse assistantMessage, String provider, String correlationId,
                                      boolean noRelevantContext) {}
}
