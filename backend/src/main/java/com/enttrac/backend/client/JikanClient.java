package com.enttrac.backend.client;

import com.enttrac.backend.model.AnimeSearchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
public class JikanClient implements AnimeMetadataClient {

    private static final String BASE_URL = "https://api.jikan.moe/v4";
    private final RestClient restClient;

    public JikanClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public List<AnimeSearchResult> search(String query) {
        JsonNode response = restClient.get()
                .uri("/anime?q={query}&limit=25&sfw", query)
                .retrieve()
                .body(JsonNode.class);

        List<AnimeSearchResult> results = new ArrayList<>();

        if (response != null && response.has("data")) {
            for (JsonNode anime : response.get("data")) {
                results.add(mapToSearchResult(anime));
            }
        }

        return results;
    }

    @Override
    public AnimeSearchResult getDetails(String id) {
        JsonNode response = restClient.get()
                .uri("/anime/{id}", id)
                .retrieve()
                .body(JsonNode.class);

        if (response != null && response.has("data")) {
            return mapToSearchResult(response.get("data"));
        }

        return null;
    }

    @Override
    public Double getCommunityRating(String id) {
        try {
            JsonNode response = restClient.get()
                    .uri("/anime/{id}", id)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                if (data.has("score") && !data.get("score").isNull()) {
                    double score = data.get("score").asDouble();
                    return Math.round(score * 10.0) / 10.0;
                }
            }
        } catch (Exception e) {
            // rating unavailable
        }
        return null;
    }

    private AnimeSearchResult mapToSearchResult(JsonNode anime) {
        String id = anime.get("mal_id").asText();

        // Title — prefer English, fall back to romaji
        String title = null;
        if (anime.has("title_english") && !anime.get("title_english").isNull()) {
            title = anime.get("title_english").asText();
        }
        if (title == null || title.isEmpty()) {
            title = anime.get("title").asText();
        }

        // Description
        String description = null;
        if (anime.has("synopsis") && !anime.get("synopsis").isNull()) {
            description = anime.get("synopsis").asText();
        }

        // Cover image
        String coverUrl = null;
        if (anime.has("images")) {
            JsonNode images = anime.get("images");
            if (images.has("jpg") && images.get("jpg").has("large_image_url")) {
                coverUrl = images.get("jpg").get("large_image_url").asText();
            }
        }

        // Episodes
        Integer totalEpisodes = null;
        if (anime.has("episodes") && !anime.get("episodes").isNull()) {
            totalEpisodes = anime.get("episodes").asInt();
        }

        // Status
        String status = null;
        if (anime.has("status") && !anime.get("status").isNull()) {
            status = anime.get("status").asText();
        }

        // Studio — first studio in producers list
        String studio = null;
        if (anime.has("studios") && anime.get("studios").isArray()
                && anime.get("studios").size() > 0) {
            studio = anime.get("studios").get(0).get("name").asText();
        }

        // Season
        String season = null;
        if (anime.has("season") && !anime.get("season").isNull()
                && anime.has("year") && !anime.get("year").isNull()) {
            String s = anime.get("season").asText();
            String y = anime.get("year").asText();
            // Capitalize first letter
            season = s.substring(0, 1).toUpperCase() + s.substring(1) + " " + y;
        }

        // Community rating
        Double communityRating = null;
        if (anime.has("score") && !anime.get("score").isNull()) {
            communityRating = Math.round(anime.get("score").asDouble() * 10.0) / 10.0;
        }

        return AnimeSearchResult.builder()
                .id(id)
                .title(title)
                .description(description)
                .coverUrl(coverUrl)
                .totalEpisodes(totalEpisodes)
                .status(status)
                .studio(studio)
                .season(season)
                .communityRating(communityRating)
                .build();
    }
}