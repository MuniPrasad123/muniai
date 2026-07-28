package com.muniai.document.infrastructure;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunkEntity,UUID> {
    List<DocumentChunkEntity> findByDocumentIdOrderByChunkIndex(UUID documentId);
    long countByDocumentId(UUID documentId);
    @Transactional
    void deleteByDocumentId(UUID documentId);
}
