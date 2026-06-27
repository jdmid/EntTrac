package com.enttrac.backend.client;

import com.enttrac.backend.model.result.AnimeSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("aniListClient")
public class AniListClient implements MediaMetadataClient<AnimeSearchResult> {

    private static final String BASE_URL = "https://graphql.anilist.co";
    private final RestClient restClient;

    public AniListClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    // --- Shared query helper ---

    private JsonNode postQuery(String query, Map<String, Object> variables) {
        Map<String, Object> body = Map.of("query", query, "variables", variables);
        return restClient.post()
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    // --- MediaMetadataClient implementation ---

    @Override
    public List<AnimeSearchResult> search(String query) {
        String gql = """
            query ($search: String, $page: Int, $perPage: Int) {
                Page(page: $page, perPage: $perPage) {
                    media(search: $search, type: ANIME) {
                        id
                        idMal
                        title { english romaji }
                        description(asHtml: false)
                        coverImage { large }
                        episodes
                        status
                        averageScore
                        season
                        seasonYear
                        studios(isMain: true) {
                            nodes { id name }
                        }
                    }
                }
            }
            """;

        Map<String, Object> variables = Map.of(
                "search", query,
                "page", 1,
                "perPage", 25
        );

        try {
            JsonNode response = postQuery(gql, variables);
            JsonNode media = response.path("data").path("Page").path("media");

            List<AnimeSearchResult> results = new ArrayList<>();
            if (media.isArray()) {
                for (JsonNode node : media) {
                    results.add(mapToSearchResult(node));
                }
            }
            log.info("AniList search for '{}' returned {} results", query, results.size());
            return results;
        } catch (Exception e) {
            log.debug("AniList search failed for '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    @Override
    public AnimeSearchResult getDetails(String id) {
        String gql = """
            query ($id: Int) {
                Media(id: $id, type: ANIME) {
                    id
                    idMal
                    title { english romaji }
                    description(asHtml: false)
                    coverImage { large }
                    episodes
                    status
                    averageScore
                    season
                    seasonYear
                    studios(isMain: true) {
                        nodes { id name }
                    }
                }
            }
            """;

        Map<String, Object> variables = Map.of("id", Integer.parseInt(id));

        try {
            JsonNode response = postQuery(gql, variables);
            JsonNode media = response.path("data").path("Media");

            if (media.isMissingNode() || media.isNull()) {
                log.warn("AniList returned no data for anime id: {}", id);
                return null;
            }

            log.info("Fetched AniList details for anime id: {}", id);
            return mapToSearchResult(media);
        } catch (Exception e) {
            log.debug("AniList getDetails failed for id {}: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public List<AnimeSearchResult> getWorksByCreator(String studioId) {
        String gql = """
            query ($studioId: Int) {
                Studio(id: $studioId) {
                    media(sort: START_DATE_DESC, perPage: 50) {
                        nodes {
                            id
                            idMal
                            title { english romaji }
                            description(asHtml: false)
                            coverImage { large }
                            episodes
                            status
                            averageScore
                            season
                            seasonYear
                        }
                    }
                }
            }
            """;

        Map<String, Object> variables = Map.of("studioId", Integer.parseInt(studioId));

        try {
            JsonNode response = postQuery(gql, variables);
            JsonNode media = response.path("data").path("Studio")
                    .path("media").path("nodes");

            List<AnimeSearchResult> results = new ArrayList<>();
            if (media.isArray()) {
                for (JsonNode node : media) {
                    results.add(mapToSearchResult(node));
                }
            }
            log.info("AniList fetched {} works for studio: {}", results.size(), studioId);
            return results;
        } catch (Exception e) {
            log.debug("AniList getWorksByCreator failed for studio {}: {}",
                    studioId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Map<String, String>> searchCreators(String name) {
        String gql = """
            query ($search: String) {
                Page(page: 1, perPage: 10) {
                    studios(search: $search) {
                        nodes {
                            id
                            name
                        }
                    }
                }
            }
            """;

        Map<String, Object> variables = Map.of("search", name);

        try {
            JsonNode response = postQuery(gql, variables);
            JsonNode studios = response.path("data").path("Page")
                    .path("studios").path("nodes");

            List<Map<String, String>> results = new ArrayList<>();
            if (studios.isArray()) {
                for (JsonNode studio : studios) {
                    String id = studio.path("id").asText();
                    String studioName = studio.path("name").asText();
                    if (!id.isBlank() && !studioName.isBlank()) {
                        results.add(Map.of("id", id, "name", studioName));
                    }
                }
            }
            log.info("AniList studio search for '{}' returned {} results",
                    name, results.size());
            return results;
        } catch (Exception e) {
            log.debug("AniList studio search failed for '{}': {}", name, e.getMessage());
            return List.of();
        }
    }

    // --- Supplementary rating methods ---

    public Double getAnilistAnimeRating(String anilistId) {
        String gql = """
            query ($id: Int) {
                Media(id: $id, type: ANIME) {
                    averageScore
                }
            }
            """;

        try {
            JsonNode response = postQuery(gql, Map.of("id", Integer.parseInt(anilistId)));
            JsonNode rating = response.path("data").path("Media").path("averageScore");

            if (!rating.isNull() && !rating.isMissingNode()) {
                double value = rating.asDouble();
                log.info("Fetched AniList anime rating for id {}: {}", anilistId, value);
                return value;
            }
        } catch (Exception e) {
            log.debug("AniList anime rating fetch failed for id {}: {}",
                    anilistId, e.getMessage());
        }
        return null;
    }

    public Double getAnilistMangaRating(String anilistId) {
        String gql = """
            query ($id: Int) {
                Media(id: $id, type: MANGA) {
                    averageScore
                }
            }
            """;

        try {
            JsonNode response = postQuery(gql, Map.of("id", Integer.parseInt(anilistId)));
            JsonNode rating = response.path("data").path("Media").path("averageScore");

            if (!rating.isNull() && !rating.isMissingNode()) {
                double value = rating.asDouble();
                log.info("Fetched AniList manga rating for id {}: {}", anilistId, value);
                return value;
            }
        } catch (Exception e) {
            log.debug("AniList manga rating fetch failed for id {}: {}",
                    anilistId, e.getMessage());
        }
        return null;
    }

    private AnimeSearchResult mapToSearchResult(JsonNode node) {
        String id = node.path("id").asText();

        String malId = node.has("idMal") && !node.get("idMal").isNull()
                ? node.get("idMal").asText() : null;

        // Title — prefer English, fall back to romaji
        String title = null;
        JsonNode titleNode = node.path("title");
        if (titleNode.has("english") && !titleNode.get("english").isNull()) {
            title = titleNode.get("english").asText();
        }
        if (title == null || title.isBlank()) {
            title = titleNode.path("romaji").asText(null);
        }

        // Description — AniList returns HTML-like strings, strip basic tags
        String description = null;
        if (node.has("description") && !node.get("description").isNull()) {
            description = node.get("description").asText()
                    .replaceAll("<[^>]+>", "")
                    .trim();
        }

        // Cover image
        String coverUrl = null;
        if (node.has("coverImage") && !node.get("coverImage").isNull()) {
            coverUrl = node.path("coverImage").path("large").asText(null);
        }

        // Episodes
        Integer totalEpisodes = null;
        if (node.has("episodes") && !node.get("episodes").isNull()) {
            totalEpisodes = node.get("episodes").asInt();
        }

        // Status — AniList enum to normalized string
        String status = null;
        if (node.has("status") && !node.get("status").isNull()) {
            status = normalizeAnilistStatus(node.get("status").asText());
        }

        // Community score — AniList is 0-100
        Double malRating = null;
        Double anilistRating = null;
        if (node.has("averageScore") && !node.get("averageScore").isNull()) {
            anilistRating = node.get("averageScore").asDouble();
        }

        // Season — combine season + seasonYear e.g. "Fall 2024"
        String season = null;
        if (node.has("season") && !node.get("season").isNull()
                && node.has("seasonYear") && !node.get("seasonYear").isNull()) {
            String s = node.get("season").asText();
            String y = node.get("seasonYear").asText();
            season = s.substring(0, 1).toUpperCase()
                    + s.substring(1).toLowerCase()
                    + " " + y;
        }

        // Studio — first main studio node
        String studio = null;
        String studioId = null;
        JsonNode studiosNode = node.path("studios").path("nodes");
        if (studiosNode.isArray() && studiosNode.size() > 0) {
            JsonNode firstStudio = studiosNode.get(0);
            studio = firstStudio.path("name").asText(null);
            studioId = firstStudio.path("id").asText(null);
        }

        return AnimeSearchResult.builder()
                .id(id)
                .malId(malId)
                .title(title)
                .description(description)
                .coverUrl(coverUrl)
                .totalEpisodes(totalEpisodes)
                .status(status)
                .anilistRating(anilistRating)
                .malRating(malRating)
                .studio(studio)
                .studioId(studioId)
                .season(season)
                .build();
    }

    private String normalizeAnilistStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "FINISHED" -> "Finished Airing";
            case "RELEASING" -> "Currently Airing";
            case "NOT_YET_RELEASED" -> "Not yet aired";
            case "CANCELLED" -> "cancelled";
            case "HIATUS" -> "hiatus";
            default -> null;
        };
    }
}
