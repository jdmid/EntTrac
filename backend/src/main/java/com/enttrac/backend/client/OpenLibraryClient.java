package com.enttrac.backend.client;

import com.enttrac.backend.model.result.BookSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("openLibraryClient")
public class OpenLibraryClient implements MediaMetadataClient<BookSearchResult> {

    private static final String BASE_URL = "https://openlibrary.org";
    private static final String COVER_BASE = "https://covers.openlibrary.org/b/id/";

    private final RestClient restClient;

    public OpenLibraryClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public List<BookSearchResult> search(String query) {
        log.info("Searching Open Library for: {}", query);

        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("q", query)
                        .queryParam("fields",
                                "key,title,author_name,author_key,cover_i," +
                                        "first_publish_year,subject")
                        .queryParam("limit", 25)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        List<BookSearchResult> results = new ArrayList<>();

        if (response != null && response.has("docs")) {
            for (JsonNode doc : response.get("docs")) {
                results.add(mapToSearchResult(doc));
            }
        }

        log.info("Search returned {} results for query: {}", results.size(), query);
        return results;
    }

    @Override
    public BookSearchResult getDetails(String id) {
        log.info("Fetching book details from Open Library for id: {}", id);

        try {
            JsonNode response = restClient.get()
                    .uri("/works/{id}.json", id)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                log.warn("No details returned from Open Library for id: {}", id);
                return null;
            }

            // Description can be a plain string or an object with a "value" key
            String description = null;
            if (response.has("description")) {
                JsonNode desc = response.get("description");
                if (desc.isTextual()) {
                    description = desc.asText();
                } else if (desc.has("value")) {
                    description = desc.get("value").asText();
                }
            }

            log.info("Successfully fetched details for book id: {}", id);
            return BookSearchResult.builder()
                    .id(id)
                    .description(description)
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch book details for id: {}", id, e);
            return null;
        }
    }

    @Override
    public List<BookSearchResult> getWorksByCreator(String authorId) {
        log.info("Fetching works by author id: {}", authorId);

        try {
            JsonNode response = restClient.get()
                    .uri("/authors/{authorId}/works.json?limit=50", authorId)
                    .retrieve()
                    .body(JsonNode.class);

            List<BookSearchResult> results = new ArrayList<>();

            if (response != null && response.has("entries")) {
                for (JsonNode entry : response.get("entries")) {
                    results.add(mapWorkEntryToSearchResult(entry, authorId));
                }
            }

            log.info("Found {} works for author id: {}", results.size(), authorId);
            return results;

        } catch (Exception e) {
            log.error("Failed to fetch works for author id: {}", authorId, e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, String>> searchCreators(String name) {
        log.info("Searching Open Library authors for: {}", name);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/authors.json")
                            .queryParam("q", name)
                            .queryParam("limit", 10)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            List<Map<String, String>> results = new ArrayList<>();

            if (response != null && response.has("docs")) {
                for (JsonNode author : response.get("docs")) {
                    String id = author.path("key").asText();
                    String authorName = author.path("name").asText();
                    if (!id.isBlank() && !authorName.isBlank()) {
                        results.add(Map.of("id", id, "name", authorName));
                    }
                }
            }

            log.info("Author search returned {} results for: {}", results.size(), name);
            return results;

        } catch (Exception e) {
            log.error("Failed to search authors for: {}", name, e);
            return List.of();
        }
    }

    private BookSearchResult mapToSearchResult(JsonNode doc) {
        // Work ID — strip "/works/" prefix
        String id = null;
        if (doc.has("key")) {
            id = doc.get("key").asText().replace("/works/", "");
        }

        String title = doc.path("title").asText(null);

        // Cover URL
        String coverUrl = null;
        if (doc.has("cover_i") && !doc.get("cover_i").isNull()) {
            coverUrl = COVER_BASE + doc.get("cover_i").asText() + "-L.jpg";
        }

        // First publish year
        String firstPublishYear = null;
        if (doc.has("first_publish_year") && !doc.get("first_publish_year").isNull()) {
            firstPublishYear = doc.get("first_publish_year").asText();
        }

        // Authors — zip author_name and author_key arrays together
        List<Map<String, String>> authors = new ArrayList<>();
        JsonNode nameNodes = doc.path("author_name");
        JsonNode keyNodes = doc.path("author_key");

        if (nameNodes.isArray()) {
            for (int i = 0; i < nameNodes.size(); i++) {
                String authorName = nameNodes.get(i).asText();
                // author_key may have fewer entries than author_name — guard against index OOB
                String authorId = (keyNodes.isArray() && i < keyNodes.size())
                        ? keyNodes.get(i).asText().replace("/authors/", "")
                        : "";
                if (!authorName.isBlank()) {
                    Map<String, String> author = new HashMap<>();
                    author.put("id", authorId);
                    author.put("name", authorName);
                    authors.add(author);
                }
            }
        }

        // Genres — take first 3 subjects, very noisy otherwise
        String genres = null;
        if (doc.has("subject") && doc.get("subject").isArray()) {
            List<String> subjectList = new ArrayList<>();
            for (JsonNode subject : doc.get("subject")) {
                subjectList.add(subject.asText());
                if (subjectList.size() == 3) break;
            }
            if (!subjectList.isEmpty()) {
                genres = String.join(", ", subjectList);
            }
        }

        return BookSearchResult.builder()
                .id(id)
                .title(title)
                .coverUrl(coverUrl)
                .firstPublishYear(firstPublishYear)
                .authors(authors)
                .genres(genres)
                .build();
    }

    private BookSearchResult mapWorkEntryToSearchResult(JsonNode entry, String authorId) {
        // Works endpoint returns a different shape than search
        String id = entry.path("key").asText("").replace("/works/", "");
        String title = entry.path("title").asText(null);

        String coverUrl = null;
        if (entry.has("covers") && entry.get("covers").isArray()
                && entry.get("covers").size() > 0) {
            coverUrl = COVER_BASE + entry.get("covers").get(0).asText() + "-L.jpg";
        }

        return BookSearchResult.builder()
                .id(id)
                .title(title)
                .coverUrl(coverUrl)
                .build();
    }
}
