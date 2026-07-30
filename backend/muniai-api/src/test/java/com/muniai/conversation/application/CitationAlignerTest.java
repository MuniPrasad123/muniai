package com.muniai.conversation.application;

import static org.junit.jupiter.api.Assertions.*;
import com.muniai.document.application.VectorStore;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CitationAlignerTest {
    private final CitationAligner aligner = new CitationAligner();

    @Test void replacesModelLabelsWithTheBestSupportedSources() {
        var sources = List.of(
                hit(0, 0.90, "The Application Load Balancer spans availability zones and distributes traffic."),
                hit(1, 0.80, "Host-based routing uses the host header. Path-based routing uses the URL path."),
                hit(2, 0.70, "The /red and /blue paths forward requests to separate target groups."));

        var result = aligner.align("""
                Host-based routing uses the host header. [Source 3]

                The /red and /blue paths forward requests to separate target groups. [Source 1]
                """, sources);

        assertEquals("""
                Host-based routing uses the host header. [Source 2]

                The /red and /blue paths forward requests to separate target groups. [Source 3]""",
                result.answer());
        assertEquals(java.util.Set.of(2, 3), result.citedSourceIndexes());
    }

    @Test void removesInventedLabelsAndLeavesUnsupportedSentencesUncited() {
        var result = aligner.align("Mars has two moons. [Source 99]",
                List.of(hit(0, 0.90, "The load balancer distributes web traffic.")));

        assertEquals("Mars has two moons.", result.answer());
        assertTrue(result.citedSourceIndexes().isEmpty());
    }

    @Test void retrievalScoreOnlyBreaksAnOtherwiseSupportedTie() {
        var result = aligner.align("Traffic reaches the load balancer.", List.of(
                hit(0, 0.60, "Traffic reaches the load balancer."),
                hit(1, 0.90, "Traffic reaches the load balancer.")));

        assertEquals("Traffic reaches the load balancer. [Source 2]", result.answer());
    }

    private VectorStore.SearchHit hit(int chunkIndex, double score, String content) {
        return new VectorStore.SearchHit(UUID.randomUUID(), UUID.randomUUID(), chunkIndex,
                score, content, "architecture.pdf");
    }
}
