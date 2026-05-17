package com.enttrac.backend.client;

import com.enttrac.backend.model.MangaSearchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
public class MangaDexClient implements MediaMetadataClient<MangaSearchResult> {

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
                .uri("/manga?title={query}&limit=25&includes[]=cover_art&includes[]=author&includes[]=artist", query)                .retrieve()
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
                .uri("/manga/{id}?includes[]=cover_art&includes[]=author&includes[]=artist", id)
                .retrieve()
                .body(JsonNode.class);

        if (response != null && response.has("data")) {
            MangaSearchResult result = mapToSearchResult(response.get("data"));
            // Override with accurate chapter count from aggregate
            Integer accurateChapter = fetchLatestChapterFromFeed(id);
            if (accurateChapter != null) {
                result.setLatestChapter(accurateChapter);
            }
            return result;
        }
        return null;
    }

    @Override
    public Double getCommunityRating(String id) {
        try {
            JsonNode response = restClient.get()
                    .uri("/statistics/manga/{id}", id)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("statistics")) {
                JsonNode stats = response.get("statistics").get(id);
                if (stats != null && stats.has("rating")) {
                    double bayesian = stats.get("rating").get("bayesian").asDouble();
                    return Math.round(bayesian * 10.0) / 10.0;
                }
            }
        } catch (Exception e) {
            // rating unavailable
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
        Integer latestChapter = null;
        if (manga.get("attributes").has("lastChapter")
                && !manga.get("attributes").get("lastChapter").isNull()
                && !manga.get("attributes").get("lastChapter").asText().isEmpty()) {
            try {
                latestChapter = (int) Double.parseDouble(
                        manga.get("attributes").get("lastChapter").asText());
            } catch (NumberFormatException e) {
                // not a clean number, leave null
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

        // Get author and artist
        String author = null;
        String artist = null;
        if (relationships != null) {
            for (JsonNode rel : relationships) {
                String relType = rel.get("type").asText();
                if ("author".equals(relType) && rel.has("attributes")) {
                    author = rel.get("attributes").get("name").asText();
                }
                if ("artist".equals(relType) && rel.has("attributes")) {
                    artist = rel.get("attributes").get("name").asText();
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
                .author(author)
                .artist(artist)
                .build();
    }

    private Integer fetchLatestChapterFromFeed(String mangaId) {
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/manga/{id}/aggregate")
                            .queryParam("translatedLanguage[]", "en")
                            .build(mangaId))
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("volumes")) {
                double maxChapter = -1;
                for (JsonNode volume : response.get("volumes")) {
                    if (volume.has("chapters")) {
                        for (JsonNode chapter : volume.get("chapters")) {
                            if (chapter.has("chapter")) {
                                try {
                                    double num = Double.parseDouble(
                                            chapter.get("chapter").asText());
                                    if (num > maxChapter) maxChapter = num;
                                } catch (NumberFormatException e) {
                                    // skip non-numeric entries
                                }
                            }
                        }
                    }
                }
                if (maxChapter >= 0) return (int) maxChapter;
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }
}