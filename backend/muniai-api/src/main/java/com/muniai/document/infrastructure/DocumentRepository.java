package com.muniai.document.infrastructure;

import java.util.List;
import java.util.UUID;
import com.muniai.document.domain.IndexingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    List<DocumentEntity> findAllByOrderByCreatedAtDescIdDesc();
    List<DocumentEntity> findByIndexingStatus(IndexingStatus status);
}
