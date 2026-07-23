package com.muniai.document.application;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class DocumentExtractor {
    public Extraction extract(Path path, String contentType) throws IOException {
        if ("application/pdf".equals(contentType)) {
            try (PDDocument pdf = Loader.loadPDF(path.toFile())) {
                PDFTextStripper stripper = new PDFTextStripper();
                StringBuilder text = new StringBuilder();
                for (int page=1; page<=pdf.getNumberOfPages(); page++) {
                    stripper.setStartPage(page); stripper.setEndPage(page);
                    String pageText = stripper.getText(pdf).strip();
                    if (!pageText.isEmpty()) {
                        if (!text.isEmpty()) text.append(System.lineSeparator()).append(System.lineSeparator());
                        text.append(pageText);
                    }
                }
                if (text.toString().isBlank()) throw new IOException("PDF has no extractable native text; OCR is not supported.");
                return new Extraction(text.toString(), pdf.getNumberOfPages());
            }
        }
        byte[] bytes = Files.readAllBytes(path);
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            return new Extraction(decoder.decode(ByteBuffer.wrap(bytes)).toString(), null);
        } catch (CharacterCodingException exception) {
            throw new IOException("The file is not valid UTF-8 text.", exception);
        }
    }
    public record Extraction(String text, Integer pageCount) {}
}
