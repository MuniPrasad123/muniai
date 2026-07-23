package com.muniai.document.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muniai.bootstrap.MuniAiApplication;
import com.muniai.document.infrastructure.DocumentRepository;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = MuniAiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired DocumentRepository documents;
    @Autowired ObjectMapper mapper;
    private final Path root = Path.of(System.getProperty("java.io.tmpdir"), "muniai-document-tests");

    @BeforeEach void clean() throws Exception {
        documents.deleteAllInBatch();
        Files.createDirectories(root);
        try (var paths = Files.list(root)) {
            for (Path path : paths.toList()) Files.deleteIfExists(path);
        }
    }

    @Test void uploadsListsFetchesReadsAndDeletesUtf8Text() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "../private notes.txt", "text/plain",
                "Synthetic local document.".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String body = mvc.perform(multipart("/api/v1/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFileName").value("private notes.txt"))
                .andExpect(jsonPath("$.extractionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.fileAvailable").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = mapper.readTree(body).get("id").asText();
        String stored = documents.findById(java.util.UUID.fromString(id)).orElseThrow().getStoredFileName();
        Assertions.assertFalse(stored.contains("private"));
        Assertions.assertTrue(Files.isRegularFile(root.resolve(stored)));

        mvc.perform(get("/api/v1/documents")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/v1/documents/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/documents/{id}/text", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Synthetic local document."));
        mvc.perform(delete("/api/v1/documents/{id}", id)).andExpect(status().isNoContent());
        Assertions.assertFalse(Files.exists(root.resolve(stored)));
        mvc.perform(get("/api/v1/documents/{id}", id)).andExpect(status().isNotFound());
    }

    @Test void uploadsMarkdown() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "readme.md", "text/markdown", "# Safe fixture".getBytes());
        mvc.perform(multipart("/api/v1/documents").file(file)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("text/markdown"))
                .andExpect(jsonPath("$.extractionStatus").value("COMPLETED"));
    }

    @Test void listsNewestDocumentFirst() throws Exception {
        String first = mapper.readTree(mvc.perform(multipart("/api/v1/documents")
                .file(new MockMultipartFile("file","first.txt","text/plain","first".getBytes())))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        Thread.sleep(5);
        String second = mapper.readTree(mvc.perform(multipart("/api/v1/documents")
                .file(new MockMultipartFile("file","second.txt","text/plain","second".getBytes())))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        mvc.perform(get("/api/v1/documents")).andExpect(jsonPath("$[0].id").value(second))
                .andExpect(jsonPath("$[1].id").value(first));
    }

    @Test void uploadsPdfAndCapturesPageCount() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fixture.pdf", "application/pdf", pdf("PDF fixture text"));
        String body = mvc.perform(multipart("/api/v1/documents").file(file)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.pageCount").value(1)).andReturn().getResponse().getContentAsString();
        String id = mapper.readTree(body).get("id").asText();
        mvc.perform(get("/api/v1/documents/{id}/text", id)).andExpect(jsonPath("$.text").value("PDF fixture text"));
    }

    @Test void rejectsEmptyUnsupportedOversizedAndFakePdf() throws Exception {
        mvc.perform(multipart("/api/v1/documents").file(new MockMultipartFile("file","empty.txt","text/plain",new byte[0])))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("EMPTY_DOCUMENT"));
        mvc.perform(multipart("/api/v1/documents").file(new MockMultipartFile("file","image.png","image/png",new byte[]{1})))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNSUPPORTED_DOCUMENT_TYPE"));
        mvc.perform(multipart("/api/v1/documents").file(new MockMultipartFile("file","large.txt","text/plain",new byte[1025])))
                .andExpect(status().isPayloadTooLarge()).andExpect(jsonPath("$.code").value("DOCUMENT_TOO_LARGE"));
        mvc.perform(multipart("/api/v1/documents").file(new MockMultipartFile("file","fake.pdf","application/pdf","not pdf".getBytes())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DOCUMENT_SIGNATURE"));
    }

    @Test void keepsFailedExtractionAndAllowsDeletionWhenPhysicalFileIsMissing() throws Exception {
        byte[] invalidUtf8 = {(byte)0xC3, (byte)0x28};
        String body = mvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file","invalid.txt","text/plain",invalidUtf8)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.extractionStatus").value("FAILED"))
                .andExpect(jsonPath("$.extractionError").value("The file is not valid UTF-8 text."))
                .andReturn().getResponse().getContentAsString();
        var entity = documents.findById(java.util.UUID.fromString(mapper.readTree(body).get("id").asText())).orElseThrow();
        Assertions.assertNull(entity.getExtractedText());
        Files.delete(root.resolve(entity.getStoredFileName()));
        mvc.perform(get("/api/v1/documents/{id}", entity.getId())).andExpect(jsonPath("$.fileAvailable").value(false));
        mvc.perform(delete("/api/v1/documents/{id}", entity.getId())).andExpect(status().isNoContent());
    }

    private byte[] pdf(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(); document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText(); stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(72, 720); stream.showText(text); stream.endText();
            }
            document.save(output); return output.toByteArray();
        }
    }
}
