package com.muniai.conversation.application;

import com.muniai.ai.application.LanguageModelProvider;
import com.muniai.ai.domain.ChatCompletion;
import com.muniai.ai.domain.ChatCompletionRequest;
import com.muniai.chat.application.ChatConfigurationProperties;
import com.muniai.conversation.domain.*;
import com.muniai.shared.exception.MessageTooLongException;
import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.muniai.memory.application.*;
import com.muniai.memory.domain.*;

@Service
public class ConversationApplicationService {
    private final ConversationPersistenceService persistence;
    private final LanguageModelProvider provider;
    private final ChatConfigurationProperties chatProperties;
    private final RagService rag;
    private final RagConfigurationProperties ragProperties;
    private final MemoryApplicationService memories;
    private final MemorySelectionService selection;
    private final MemoryPromptFormatter memoryPrompts;
    private final MemoryCommandService commands;

    public ConversationApplicationService(ConversationPersistenceService persistence, LanguageModelProvider provider,
                                          ChatConfigurationProperties chatProperties, RagService rag,
                                          RagConfigurationProperties ragProperties,MemoryApplicationService memories,
                                          MemorySelectionService selection,MemoryPromptFormatter memoryPrompts,
                                          MemoryCommandService commands) {
        this.persistence = persistence;
        this.provider = provider;
        this.chatProperties = chatProperties;
        this.rag = rag;
        this.ragProperties = ragProperties;
        this.memories=memories;this.selection=selection;this.memoryPrompts=memoryPrompts;this.commands=commands;
    }

    public Conversation create() { return persistence.create(); }
    public List<Conversation> list() { return persistence.list(); }
    public Conversation get(UUID id) { return persistence.get(id); }
    public Conversation rename(UUID id, String title) { return persistence.rename(id, title); }
    public void delete(UUID id) { persistence.delete(id); }
    public void deleteAll() { persistence.deleteAll(); }

    public SendMessageResult send(UUID conversationId, String content, ChatMode mode, Set<UUID> documentIds,
                                  Integer topK, Double similarityThreshold) {
        if (content.length() > chatProperties.maxMessageLength()) {
            throw new MessageTooLongException(chatProperties.maxMessageLength());
        }
        persistence.get(conversationId);
        Optional<UserMemory> proposal=commands.propose(content,conversationId,null);
        ConversationMessage userMessage = persistence.saveUserMessage(conversationId, content, mode);
        if(proposal.isPresent()){
            ConversationMessage assistant=persistence.saveAssistantMessage(conversationId,
                    "I created a memory proposal. Review and approve it before it can personalize future responses.","memory",mode,List.of());
            return new SendMessageResult(conversationId,userMessage,assistant,"local-memory",false,proposal.get(),List.of(),null);
        }
        if(commands.isRecall(content)){
            List<UserMemory> recalled=memories.active();
            String answer=recalled.isEmpty()?"I do not have any active approved memories.":"I found "+recalled.size()+" active approved "+(recalled.size()==1?"memory.":"memories.");
            ConversationMessage assistant=persistence.saveAssistantMessage(conversationId,answer,"memory",mode,List.of());
            return new SendMessageResult(conversationId,userMessage,assistant,"local-memory",false,null,recalled,null);
        }
        List<UserMemory> forgetMatches=commands.forgetMatches(content);
        if(!forgetMatches.isEmpty()){
            String answer=forgetMatches.size()==1?"I found one active memory. Confirm disabling it from the Memory page to keep forgetting reversible.":"I found "+forgetMatches.size()+" matching active memories. Choose which one to disable from the Memory page.";
            ConversationMessage assistant=persistence.saveAssistantMessage(conversationId,answer,"memory",mode,List.of());
            return new SendMessageResult(conversationId,userMessage,assistant,"local-memory",false,null,forgetMatches,null);
        }
        List<UserMemory> selected;String warning=null;
        try{selected=selection.select(content);}catch(RuntimeException failure){selected=List.of();warning="Memory personalization was unavailable; the response was generated without memory.";}
        List<UsedMemory> used=selected.stream().map(m->new UsedMemory(m.id(),m.category(),m.key(),m.displayLabel())).toList();
        if(mode==ChatMode.NORMAL){
            List<ConversationMessage> history=persistence.recentMessages(conversationId,ragProperties.maxHistoryMessages()+1)
                    .stream().filter(message->!message.id().equals(userMessage.id())).toList();
            String prompt=memoryPrompts.normalPrompt(content,history,selected);
            ChatCompletion completion = provider.complete(new ChatCompletionRequest(prompt));
            ConversationMessage assistantMessage = persistence.saveAssistantMessage(
                    conversationId, completion.answer(), completion.model(), mode, List.of(),used);
            memories.recordUsage(used);
            return new SendMessageResult(conversationId,userMessage,assistantMessage,completion.provider(),false,null,List.of(),warning);
        }
        List<ConversationMessage> history=persistence.recentMessages(conversationId,ragProperties.maxHistoryMessages()+1)
                .stream().filter(message->!message.id().equals(userMessage.id())).toList();
        RagService.RagResult result=rag.answer(content,documentIds,topK,similarityThreshold,history,selected);
        String model=result.noRelevantContext()?"retrieval":result.completion().model();
        String providerName=result.noRelevantContext()?"local-retrieval":result.completion().provider();
        ConversationMessage assistantMessage=persistence.saveAssistantMessage(
                conversationId,result.answer(),model,mode,result.citations(),result.noRelevantContext()?List.of():used);
        if(!result.noRelevantContext())memories.recordUsage(used);
        return new SendMessageResult(conversationId,userMessage,assistantMessage,providerName,result.noRelevantContext(),null,List.of(),warning);
    }

    public record SendMessageResult(UUID conversationId, ConversationMessage userMessage,
                                    ConversationMessage assistantMessage, String provider, boolean noRelevantContext,
                                    UserMemory memoryProposal,List<UserMemory> recalledMemories,String memoryWarning) {}
}
