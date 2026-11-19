package com.muniai.memory.infrastructure;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MessageMemoryUsageRepository extends JpaRepository<MessageMemoryUsageEntity,UUID>{
    List<MessageMemoryUsageEntity> findByMessage_Id(UUID messageId);
}
