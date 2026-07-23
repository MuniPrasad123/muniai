package com.muniai.document.application;

import com.muniai.document.infrastructure.*;
import com.muniai.shared.exception.*;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentApplicationService {
    private static final Map<String,String> TYPES = Map.of(
            "pdf", "application/pdf", "txt", "text/plain", "md", "text/markdown");
    private final DocumentRepository repository;
    private final DocumentExtractor extractor;
    private final DocumentConfigurationProperties properties;

    public DocumentApplicationService(DocumentRepository repository, DocumentExtractor extractor,
                                      DocumentConfigurationProperties properties) {
        this.repository=repository; this.extractor=extractor; this.properties=properties;
    }

    public DocumentEntity upload(MultipartFile file) {
        ValidatedUpload validated = validate(file);
        Path root = storageRoot();
        UUID id = UUID.randomUUID();
        String storedName = id + "." + validated.extension();
        Path destination = contained(root, storedName);
        try {
            Files.createDirectories(root);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new DocumentException("DOCUMENT_STORAGE_FAILED", "The document could not be stored.", exception);
        }

        Instant now = Instant.now();
        DocumentEntity document = new DocumentEntity(id, validated.safeName(), storedName,
                validated.contentType(), file.getSize(), storedName, now);
        try {
            repository.saveAndFlush(document);
        } catch (RuntimeException exception) {
            try { Files.deleteIfExists(destination); } catch (IOException ignored) { exception.addSuppressed(ignored); }
            throw exception;
        }
        document.processing(Instant.now());
        repository.saveAndFlush(document);
        try {
            DocumentExtractor.Extraction result = extractor.extract(destination, validated.contentType());
            document.completed(result.text(), result.pageCount(), Instant.now());
        } catch (Exception exception) {
            document.failed(safeExtractionError(exception), Instant.now());
        }
        return repository.saveAndFlush(document);
    }

    public List<DocumentEntity> list() { return repository.findAllByOrderByCreatedAtDescIdDesc(); }
    public DocumentEntity get(UUID id) {
        return repository.findById(id).orElseThrow(DocumentNotFoundException::new);
    }
    public String text(UUID id) {
        DocumentEntity document = get(id);
        if (document.getExtractionStatus() != com.muniai.document.domain.ExtractionStatus.COMPLETED) {
            throw new DocumentException("DOCUMENT_TEXT_UNAVAILABLE", "Extracted text is not available for this document.");
        }
        return document.getExtractedText();
    }
    public boolean fileAvailable(DocumentEntity document) {
        return Files.isRegularFile(contained(storageRoot(), document.getStoragePath()));
    }
    public void delete(UUID id) {
        DocumentEntity document = get(id);
        Path path = contained(storageRoot(), document.getStoragePath());
        try {
            Files.deleteIfExists(path);
        } catch (IOException | SecurityException exception) {
            throw new DocumentException("DOCUMENT_DELETE_FAILED", "The stored document file could not be deleted.", exception);
        }
        repository.delete(document);
    }

    private ValidatedUpload validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new DocumentException("EMPTY_DOCUMENT", "Choose a non-empty document.");
        }
        if (file.getSize() > properties.maxFileSize().toBytes()) {
            throw new DocumentException("DOCUMENT_TOO_LARGE", "The document exceeds the configured maximum size.");
        }
        String safeName = sanitize(file.getOriginalFilename());
        int dot = safeName.lastIndexOf('.');
        String extension = dot < 0 ? "" : safeName.substring(dot + 1).toLowerCase(Locale.ROOT);
        String expectedType = TYPES.get(extension);
        String declaredType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        if (expectedType == null || !mimeMatches(expectedType, declaredType)) {
            throw new DocumentException("UNSUPPORTED_DOCUMENT_TYPE", "Only PDF, UTF-8 TXT, and Markdown files are supported.");
        }
        try {
            byte[] prefix;
            try (InputStream input = file.getInputStream()) { prefix = input.readNBytes(8192); }
            if ("application/pdf".equals(expectedType)) {
                if (prefix.length < 5 || prefix[0]!='%' || prefix[1]!='P' || prefix[2]!='D' || prefix[3]!='F' || prefix[4]!='-') {
                    throw new DocumentException("INVALID_DOCUMENT_SIGNATURE", "The file content is not a valid PDF.");
                }
            } else {
                for (byte value : prefix) if (value == 0) {
                    throw new DocumentException("INVALID_DOCUMENT_SIGNATURE", "The file content is not plain UTF-8 text.");
                }
            }
        } catch (IOException exception) {
            throw new DocumentException("DOCUMENT_VALIDATION_FAILED", "The document could not be validated.", exception);
        }
        return new ValidatedUpload(safeName, extension, expectedType);
    }

    private boolean mimeMatches(String expected, String actual) {
        if (expected.equals(actual)) return true;
        return "text/markdown".equals(expected) && ("text/plain".equals(actual) || "text/x-markdown".equals(actual));
    }
    private String sanitize(String original) {
        String name = Optional.ofNullable(original).orElse("document").replace('\\','/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").strip();
        name = name.replaceAll("[^\\p{L}\\p{N}._ ()-]", "_");
        if (name.isBlank()) name = "document";
        if (name.length() > 255) name = name.substring(name.length() - 255);
        return name;
    }
    private Path storageRoot() {
        return Path.of(properties.uploadDirectory()).toAbsolutePath().normalize();
    }
    private Path contained(Path root, String name) {
        Path resolved = root.resolve(name).normalize();
        if (!resolved.startsWith(root)) throw new DocumentException("INVALID_STORAGE_PATH", "The stored document path is invalid.");
        return resolved;
    }
    private String safeExtractionError(Exception exception) {
        String message = exception.getMessage();
        if (message != null && (message.contains("native text") || message.contains("UTF-8"))) return message;
        return "Readable text could not be extracted from this document.";
    }
    private record ValidatedUpload(String safeName, String extension, String contentType) {}
}
