package com.enttrac.backend.client;

import com.enttrac.backend.model.result.BookSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GoogleBooksClient {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1";
    private static final int NEW_RELEASE_DAYS = 90;

    private final String apiKey;
    private final RestClient restClient;

    public GoogleBooksClient(@Value("${google.books.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    // --- Upcoming search: returns titles published within last 90 days or in the future ---

    public List<BookSearchResult> searchUpcoming(String query) {
        log.info("Searching Google Books upcoming for: {}", query);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", query)
                            .queryParam("orderBy", "newest")
                            .queryParam("printType", "books")
                            .queryParam("maxResults", 25)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            List<BookSearchResult> results = new ArrayList<>();

            if (response != null && response.has("items")) {
                for (JsonNode item : response.get("items")) {
                    BookSearchResult result = mapToSearchResult(item);
                    if (result != null && isNewOrUpcoming(result.getPublishedDate())) {
                        results.add(result);
                    }
                }
            }

            log.info("Upcoming search returned {} results for: {}", results.size(), query);
            return results;

        } catch (Exception e) {
            log.error("Failed to search Google Books upcoming for: {}", query, e);
            return List.of();
        }
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

    private BookSearchResult mapToSearchResult(JsonNode item) {
        try {
            JsonNode volumeInfo = item.path("volumeInfo");

            String id = item.path("id").asText(null);
            String title = volumeInfo.path("title").asText(null);
            if (id == null || title == null) return null;

            String description = volumeInfo.path("description").asText(null);
            String publishedDate = volumeInfo.path("publishedDate").asText(null);
            String isbn = extractIsbn(volumeInfo);

            // Cover
            String coverUrl = null;
            JsonNode imageLinks = volumeInfo.path("imageLinks");
            if (!imageLinks.isMissingNode()) {
                coverUrl = imageLinks.path("thumbnail").asText(null);
                // Upgrade to https — Google returns http thumbnails
                if (coverUrl != null) {
                    coverUrl = coverUrl.replace("http://", "https://");
                }
            }

            // Authors — Google Books returns a flat string array
            List<Map<String, String>> authors = new ArrayList<>();
            if (volumeInfo.has("authors") && volumeInfo.get("authors").isArray()) {
                for (JsonNode author : volumeInfo.get("authors")) {
                    Map<String, String> authorMap = new HashMap<>();
                    authorMap.put("name", author.asText());
                    authorMap.put("id", "");
                    authors.add(authorMap);
                }
            }

            // Series
            String seriesName = null;
            String seriesPosition = null;
            if (volumeInfo.has("seriesInfo")) {
                JsonNode seriesInfo = volumeInfo.get("seriesInfo");
                seriesName = seriesInfo.path("shortSeriesBookTitle").asText(null);
                seriesPosition = seriesInfo.path("bookDisplayNumber").asText(null);
            }

            return BookSearchResult.builder()
                    .id(id)
                    .title(title)
                    .description(description)
                    .coverUrl(coverUrl)
                    .authors(authors)
                    .publishedDate(publishedDate)
                    .isbn(isbn)
                    .seriesName(seriesName)
                    .seriesPosition(seriesPosition)
                    .source("GOOGLEBOOKS")
                    .build();

        } catch (Exception e) {
            log.debug("Failed to map Google Books item to search result: {}", e.getMessage());
            return null;
        }
    }

    private boolean isNewOrUpcoming(String publishedDate) {
        if (publishedDate == null || publishedDate.isBlank()) return false;
        try {
            LocalDate date = parseFlexibleDate(publishedDate);
            LocalDate cutoff = LocalDate.now().minusDays(NEW_RELEASE_DAYS);
            return !date.isBefore(cutoff);
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDate parseFlexibleDate(String dateStr) {
        // Google Books returns variable precision: "2025", "2025-11", "2025-11-04"
        if (dateStr.length() == 4) {
            return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
        } else if (dateStr.length() == 7) {
            return LocalDate.parse(dateStr + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

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
