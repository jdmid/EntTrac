package com.enttrac.backend.client;

import com.enttrac.backend.model.result.MangaSearchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("mangaDexClient")
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
                .uri(uriBuilder -> uriBuilder
                        .path("/manga")
                        .queryParam("title", query)
                        .queryParam("limit", 25)
                        .queryParam("includes[]", "cover_art", "author", "artist")
                        .queryParam("contentRating[]", "safe", "suggestive")
                        .build())
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
                .uri(uriBuilder -> uriBuilder
                        .path("/manga/{id}")
                        .queryParam("includes[]", "cover_art", "author", "artist")
                        .queryParam("contentRating[]", "safe", "suggestive")
                        .build(id))
                .retrieve()
                .body(JsonNode.class);

        if (response != null && response.has("data")) {
            MangaSearchResult result = mapToSearchResult(response.get("data"));

            String mangaStatus = response.get("data")
                    .path("attributes")
                    .path("status")
                    .asText("");

            if ("completed".equalsIgnoreCase(mangaStatus)) {
                // lastChapter from manga object is source of truth for completed series
                // only fall back to feed if it came back null or 0
                if (result.getLatestChapter() == null || result.getLatestChapter() == 0) {
                    Integer feedChapter = fetchLatestChapterFromFeed(id);
                    if (feedChapter != null) result.setLatestChapter(feedChapter);
                }
            } else {
                // ongoing, hiatus — feed is source of truth
                Integer feedChapter = fetchLatestChapterFromFeed(id);
                if (feedChapter != null) result.setLatestChapter(feedChapter);
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

        // Get author, authorId, artist, artistId
        String author = null;
        String artist = null;
        String authorId = null;
        String artistId = null;
        if (relationships != null) {
            for (JsonNode rel : relationships) {
                String relType = rel.get("type").asText();
                if ("author".equals(relType) && rel.has("attributes")) {
                    author = rel.get("attributes").get("name").asText();
                    authorId = rel.get("id").asText();
                }
                if ("artist".equals(relType) && rel.has("attributes")) {
                    artist = rel.get("attributes").get("name").asText();
                    artistId = rel.get("id").asText();
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
                .artistId(artistId)
                .authorId(authorId)
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

    public List<MangaSearchResult> getWorksByCreator(String creatorId) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/author/{id}")
                        .queryParam("includes[]", "manga")
                        .build(creatorId))
                .retrieve()
                .body(JsonNode.class);

        List<String> mangaIds = new ArrayList<>();

        if (response != null && response.has("data")) {
            JsonNode data = response.get("data");
            if (data.has("relationships")) {
                for (JsonNode rel : data.get("relationships")) {
                    if ("manga".equals(rel.path("type").asText())) {
                        mangaIds.add(rel.get("id").asText());
                    }
                }
            }
        }

        if (mangaIds.isEmpty()) return List.of();

        JsonNode batchResponse = restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/manga")
                            .queryParam("limit", 100)
                            .queryParam("includes[]", "cover_art", "author", "artist")
                            .queryParam("contentRating[]", "safe", "suggestive");
                    for (String id : mangaIds) {
                        builder = builder.queryParam("ids[]", id);
                    }
                    return builder.build();
                })
                .retrieve()
                .body(JsonNode.class);

        List<MangaSearchResult> results = new ArrayList<>();

        if (batchResponse != null && batchResponse.has("data")) {
            for (JsonNode manga : batchResponse.get("data")) {
                results.add(mapToSearchResult(manga));
            }
        }

        return results;
    }

    private MangaSearchResult mapAuthorWorkToSearchResult(JsonNode manga) {
        String id = manga.get("id").asText();

        JsonNode titles = manga.path("attributes").path("title");
        String title = null;
        if (titles.has("en")) {
            title = titles.get("en").asText();
        } else if (titles.fields().hasNext()) {
            title = titles.fields().next().getValue().asText();
        }
        if (title == null) title = "Unknown title";

        String status = manga.path("attributes").path("status").asText(null);

        String coverUrl = null;
        if (manga.has("relationships")) {
            for (JsonNode rel : manga.get("relationships")) {
                if ("cover_art".equals(rel.path("type").asText()) && rel.has("attributes")) {
                    String fileName = rel.get("attributes").path("fileName").asText();
                    coverUrl = "https://uploads.mangadex.org/covers/" + id + "/" + fileName;
                    break;
                }
            }
        }

        return MangaSearchResult.builder()
                .id(id)
                .title(title)
                .status(status)
                .coverUrl(coverUrl)
                .build();
    }

    @Override
    public List<Map<String, String>> searchCreators(String name) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/author")
                        .queryParam("name", name)
                        .queryParam("limit", 10)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        List<Map<String, String>> results = new ArrayList<>();

        if (response != null && response.has("data")) {
            for (JsonNode author : response.get("data")) {
                String id = author.get("id").asText();
                String authorName = author.path("attributes").path("name").asText();
                if (!id.isBlank() && !authorName.isBlank()) {
                    results.add(Map.of("id", id, "name", authorName));
                }
            }
        }

        return results;
    }
}