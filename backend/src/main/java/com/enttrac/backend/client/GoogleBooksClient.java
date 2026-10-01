package com.enttrac.backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GoogleBooksClient {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1";

    private final String apiKey;
    private final RestClient restClient;

    public GoogleBooksClient(@Value("${google.books.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    // --- Enrichment: called at save time for Open Library entries ---

    public BookEnrichmentData enrich(String title, String authorLastName) {
        if (title == null || title.isBlank()) return null;

        String queryString = "intitle:" + title;
        if (authorLastName != null && !authorLastName.isBlank()) {
            queryString += "+inauthor:" + authorLastName;
        }

        final String query = queryString;
        log.info("Enriching book via Google Books: title={}, author={}", title, authorLastName);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", query)
                            .queryParam("maxResults", 1)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("items")
                    || response.get("items").isEmpty()) {
                log.debug("No Google Books enrichment result for: {}", title);
                return null;
            }

            JsonNode item = response.get("items").get(0);
            JsonNode volumeInfo = item.path("volumeInfo");

            // Fuzzy title validation — reject if result title shares no words with query title
            String resultTitle = volumeInfo.path("title").asText("");
            if (!titlesMatch(title, resultTitle)) {
                log.debug("Google Books enrichment title mismatch: expected '{}', got '{}'",
                        title, resultTitle);
                return null;
            }

            String googleBooksId = item.path("id").asText(null);
            String isbn = extractIsbn(volumeInfo);
            String publishedDate = volumeInfo.path("publishedDate").asText(null);
            String seriesName = null;
            String seriesPosition = null;

            if (volumeInfo.has("seriesInfo")) {
                JsonNode seriesInfo = volumeInfo.get("seriesInfo");
                seriesName = seriesInfo.path("shortSeriesBookTitle").asText(null);
                seriesPosition = seriesInfo.path("bookDisplayNumber").asText(null);
            }

            log.info("Successfully enriched book '{}' with Google Books data", title);
            return new BookEnrichmentData(googleBooksId, isbn, publishedDate,
                    seriesName, seriesPosition);

        } catch (Exception e) {
            log.error("Failed to enrich book '{}' via Google Books: {}", title, e.getMessage());
            return null;
        }
    }

    // --- Private helpers ---

    private String extractIsbn(JsonNode volumeInfo) {
        if (!volumeInfo.has("industryIdentifiers")) return null;
        for (JsonNode identifier : volumeInfo.get("industryIdentifiers")) {
            if ("ISBN_13".equals(identifier.path("type").asText())) {
                return identifier.path("identifier").asText(null);
            }
        }
        // Fall back to ISBN_10 if no ISBN_13
        for (JsonNode identifier : volumeInfo.get("industryIdentifiers")) {
            if ("ISBN_10".equals(identifier.path("type").asText())) {
                return identifier.path("identifier").asText(null);
            }
        }
        return null;
    }

    private boolean titlesMatch(String expected, String actual) {
        if (expected == null || actual == null) return false;
        String normalizedExpected = expected.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        String normalizedActual = actual.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        // Check if any word from expected title appears in actual title
        for (String word : normalizedExpected.split(" ")) {
            if (word.length() > 3 && normalizedActual.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
