package com.muniai.memory.infrastructure;
import com.muniai.memory.domain.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserMemoryRepository extends JpaRepository<UserMemoryEntity,UUID>{
    List<UserMemoryEntity> findAllByOrderByUpdatedAtDesc();
    List<UserMemoryEntity> findByStatusOrderByUpdatedAtDesc(MemoryStatus status);
    List<UserMemoryEntity> findByStatusAndExpiresAtIsNullOrStatusAndExpiresAtAfterOrderByUpdatedAtDesc(
            MemoryStatus first,MemoryStatus second,Instant now);
    List<UserMemoryEntity> findByStatusAndMemoryKeyOrderByUpdatedAtDesc(MemoryStatus status,String key);
    long countByStatus(MemoryStatus status);
}
