package com.muniai.memory.application;

import com.muniai.memory.domain.*;
import com.muniai.memory.infrastructure.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemoryApplicationService {
    public static final Set<String> SINGULAR_KEYS=Set.of("preferred_name","target_role","preferred_language","response_style","experience_level","preferred_explanation_style","preferred_ai_runtime");
    private final UserMemoryRepository repository;
    private final MemorySensitiveDataPolicy sensitive;
    private final MemoryConfigurationProperties properties;
    public MemoryApplicationService(UserMemoryRepository repository,MemorySensitiveDataPolicy sensitive,
            MemoryConfigurationProperties properties){this.repository=repository;this.sensitive=sensitive;this.properties=properties;}

    @Transactional
    public UserMemory propose(MemoryCategory category,String key,String value,String label,MemorySourceType source,
            UUID conversationId,UUID messageId,Double confidence,Instant expiresAt){
        requireEnabled(); validate(category,key,value,label,source); sensitive.validate(key,value);
        if(repository.countByStatus(MemoryStatus.PROPOSED)>=properties.maxProposed())
            throw new MemoryException("MEMORY_PROPOSED_LIMIT","The maximum number of proposed memories has been reached.");
        Instant now=Instant.now();
        return map(repository.save(new UserMemoryEntity(UUID.randomUUID(),category,normalizeKey(key),value.trim(),
                normalizeValue(value),label.trim(),MemoryStatus.PROPOSED,source,conversationId,messageId,confidence,expiresAt,now)));
    }
    @Transactional(readOnly=true) public List<UserMemory> list(){return repository.findAllByOrderByUpdatedAtDesc().stream().map(this::map).toList();}
    @Transactional(readOnly=true) public List<UserMemory> list(MemoryStatus status){return repository.findByStatusOrderByUpdatedAtDesc(status).stream().map(this::map).toList();}
    @Transactional(readOnly=true) public UserMemory get(UUID id){return map(required(id));}

    @Transactional
    public UserMemory edit(UUID id,MemoryCategory category,String key,String value,String label,Instant expiresAt){
        UserMemoryEntity entity=required(id);
        if(entity.getStatus()==MemoryStatus.DELETED||entity.getStatus()==MemoryStatus.SUPERSEDED||entity.getStatus()==MemoryStatus.REJECTED)
            throw transition(entity,"edited");
        validate(category,key,value,label,entity.getSourceType()); sensitive.validate(key,value);
        entity.edit(category,normalizeKey(key),value.trim(),normalizeValue(value),label.trim(),expiresAt,Instant.now());
        return map(entity);
    }
    @Transactional
    public UserMemory approve(UUID id){
        UserMemoryEntity entity=required(id); requireStatus(entity,MemoryStatus.PROPOSED,"approved");
        if(repository.countByStatus(MemoryStatus.ACTIVE)>=properties.maxActive())
            throw new MemoryException("MEMORY_ACTIVE_LIMIT","The maximum number of active memories has been reached.");
        List<UserMemoryEntity> conflicts=activeConflicts(entity);
        if(!conflicts.isEmpty()) throw new MemoryException("MEMORY_CONFLICT",
                "An active value already exists for this singular memory key; explicit replacement is required.");
        entity.activate(Instant.now()); return map(entity);
    }
    @Transactional public UserMemory reject(UUID id){UserMemoryEntity e=required(id);requireStatus(e,MemoryStatus.PROPOSED,"rejected");e.reject(Instant.now());return map(e);}
    @Transactional public UserMemory disable(UUID id){UserMemoryEntity e=required(id);requireStatus(e,MemoryStatus.ACTIVE,"disabled");e.disable(Instant.now());return map(e);}
    @Transactional public UserMemory enable(UUID id){
        UserMemoryEntity e=required(id);requireStatus(e,MemoryStatus.DISABLED,"enabled");
        if(!activeConflicts(e).isEmpty()) throw new MemoryException("MEMORY_CONFLICT","Another active value exists for this singular memory key.");
        e.activate(Instant.now());return map(e);
    }
    @Transactional
    public UserMemory supersede(UUID oldId,UUID replacementId){
        if(oldId.equals(replacementId)) throw new MemoryException("INVALID_MEMORY_REPLACEMENT","A memory cannot replace itself.");
        UserMemoryEntity old=required(oldId),replacement=required(replacementId);
        requireStatus(old,MemoryStatus.ACTIVE,"superseded"); requireStatus(replacement,MemoryStatus.PROPOSED,"activated");
        if(!old.getMemoryKey().equals(replacement.getMemoryKey())||!SINGULAR_KEYS.contains(old.getMemoryKey()))
            throw new MemoryException("INVALID_MEMORY_REPLACEMENT","Replacement requires the same singular memory key.");
        Instant now=Instant.now();old.supersede(replacement.getId(),now);replacement.activate(now);return map(replacement);
    }
    @Transactional public void delete(UUID id){UserMemoryEntity e=required(id);if(e.getStatus()!=MemoryStatus.DELETED)e.delete(Instant.now());}
    @Transactional public void deleteAll(){Instant now=Instant.now();repository.findAll().forEach(e->{if(e.getStatus()!=MemoryStatus.DELETED)e.delete(now);});}

    @Transactional(readOnly=true)
    public List<UserMemory> active(){
        Instant now=Instant.now();
        return repository.findByStatusOrderByUpdatedAtDesc(MemoryStatus.ACTIVE).stream()
                .filter(e->e.getExpiresAt()==null||e.getExpiresAt().isAfter(now)).map(this::map).toList();
    }
    @Transactional
    public void recordUsage(Collection<UsedMemory> used){
        if(!properties.usageMetadataEnabled()||used.isEmpty())return;
        Instant now=Instant.now();
        for(UsedMemory value:used)repository.findById(value.memoryId()).filter(e->e.getStatus()==MemoryStatus.ACTIVE).ifPresent(e->e.used(now));
    }
    private List<UserMemoryEntity> activeConflicts(UserMemoryEntity value){
        if(!SINGULAR_KEYS.contains(value.getMemoryKey()))return List.of();
        return repository.findByStatusAndMemoryKeyOrderByUpdatedAtDesc(MemoryStatus.ACTIVE,value.getMemoryKey()).stream()
                .filter(e->!e.getId().equals(value.getId())).toList();
    }
    private void validate(MemoryCategory category,String key,String value,String label,MemorySourceType source){
        if(category==null||source==null)throw new MemoryException("INVALID_MEMORY","Memory category and source type are required.");
        if(key==null||!key.trim().matches("[A-Za-z][A-Za-z0-9_-]{0,99}"))throw new MemoryException("INVALID_MEMORY_KEY","Memory key is invalid.");
        if(value==null||value.isBlank())throw new MemoryException("INVALID_MEMORY_VALUE","Memory value is required.");
        if(value.length()>properties.maxValueLength())throw new MemoryException("MEMORY_VALUE_TOO_LONG","Memory value exceeds the configured maximum length.");
        if(label==null||label.isBlank()||label.length()>150)throw new MemoryException("INVALID_MEMORY_LABEL","Memory display label is invalid.");
    }
    private void requireEnabled(){if(!properties.enabled())throw new MemoryException("MEMORY_DISABLED","Long-term memory is disabled.");}
    private UserMemoryEntity required(UUID id){return repository.findById(id).orElseThrow(()->new MemoryException("MEMORY_NOT_FOUND","The memory was not found."));}
    private void requireStatus(UserMemoryEntity e,MemoryStatus expected,String action){if(e.getStatus()!=expected)throw transition(e,action);}
    private MemoryException transition(UserMemoryEntity e,String action){return new MemoryException("INVALID_MEMORY_TRANSITION","A "+e.getStatus()+" memory cannot be "+action+".");}
    static String normalizeKey(String value){return value.trim().toLowerCase(Locale.ROOT).replaceAll("[\\s-]+","_");}
    static String normalizeValue(String value){return value.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
    UserMemory map(UserMemoryEntity e){return new UserMemory(e.getId(),e.getCategory(),e.getMemoryKey(),e.getMemoryValue(),e.getDisplayLabel(),e.getStatus(),e.getSourceType(),e.getSourceConversationId(),e.getSourceMessageId(),e.getConfidence(),e.getApprovedAt(),e.getDisabledAt(),e.getRejectedAt(),e.getDeletedAt(),e.getExpiresAt(),e.getSupersededByMemoryId(),e.getLastUsedAt(),e.getUsageCount(),e.getCreatedAt(),e.getUpdatedAt());}
}
