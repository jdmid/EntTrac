package com.enttrac.backend.client;

import com.enttrac.backend.model.MangaSearchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
public class MangaDexClient implements MangaMetadataClient {

    private static final String BASE_URL = "https://api.mangadex.org";
    private final RestClient restClient;

    public MangaDexClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public List<MangaSearchResult> search(String query) {
        JsonNode response = restClient.get()
                .uri("/manga?title={query}&limit=10&includes[]=cover_art", query)
                .retrieve()
                .body(JsonNode.class);

        List<MangaSearchResult> results = new ArrayList<>();

        if (response != null && response.has("data")) {
            for (JsonNode manga : response.get("data")) {
                results.add(mapToSearchResult(manga));
            }
        }

        return results;
    }

    @Override
    public MangaSearchResult getDetails(String id) {
        JsonNode response = restClient.get()
                .uri("/manga/{id}?includes[]=cover_art", id)
                .retrieve()
                .body(JsonNode.class);

        if (response != null && response.has("data")) {
            return mapToSearchResult(response.get("data"));
        }

        return null;
    }

    private MangaSearchResult mapToSearchResult(JsonNode manga) {
        String id = manga.get("id").asText();

        // Get English title, fall back to first available
        JsonNode titles = manga.get("attributes").get("title");
        String title = titles.has("en") ? titles.get("en").asText()
                : titles.fields().next().getValue().asText();

        // Get description
        JsonNode descriptions = manga.get("attributes").get("description");
        String description = descriptions != null && descriptions.has("en")
                ? descriptions.get("en").asText() : "";

        // Get status
        String status = manga.get("attributes").get("status").asText();

        // Get latest chapter
        JsonNode lastChapterNode = manga.get("attributes").get("lastChapter");
        Integer latestChapter = null;
        if (lastChapterNode != null && !lastChapterNode.isNull() && !lastChapterNode.asText().isEmpty()) {
            try {
                latestChapter = Integer.parseInt(lastChapterNode.asText());
            } catch (NumberFormatException e) {
                // some manga have non-numeric chapter numbers, leave null
            }
        }

        // Get cover art URL
        String coverUrl = null;
        JsonNode relationships = manga.get("relationships");
        if (relationships != null) {
            for (JsonNode rel : relationships) {
                if ("cover_art".equals(rel.get("type").asText())) {
                    String fileName = rel.get("attributes").get("fileName").asText();
                    coverUrl = "https://uploads.mangadex.org/covers/" + id + "/" + fileName;
                    break;
                }
            }
        }

        return MangaSearchResult.builder()
                .id(id)
                .title(title)
                .description(description)
                .status(status)
                .latestChapter(latestChapter)
                .coverUrl(coverUrl)
                .build();
    }
}