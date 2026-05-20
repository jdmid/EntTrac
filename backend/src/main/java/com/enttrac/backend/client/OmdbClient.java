package com.enttrac.backend.client;

import com.enttrac.backend.model.result.MovieSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

@Component("omdbClient")
public class OmdbClient {

    private static final String BASE_URL = "https://www.omdbapi.com";

    private final String apiKey;
    private final RestClient restClient;

    public OmdbClient(@Value("${omdb.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public void enrichWithRatings(MovieSearchResult result, String imdbId) {
        if (imdbId == null || imdbId.isBlank()) return;

        try {
            JsonNode response = restClient.get()
                    .uri("/?i={imdbId}&apikey={apiKey}", imdbId, apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) return;
            if ("False".equals(response.path("Response").asText())) return;

            // IMDb rating
            if (response.has("imdbRating")
                    && !response.get("imdbRating").isNull()
                    && !"N/A".equals(response.get("imdbRating").asText())) {
                try {
                    result.setImdbRating(
                            Double.parseDouble(response.get("imdbRating").asText()));
                } catch (NumberFormatException e) {
                    // leave null
                }
            }

            // Rotten Tomatoes and Metacritic come back inside a Ratings array
            if (response.has("Ratings") && response.get("Ratings").isArray()) {
                for (JsonNode rating : response.get("Ratings")) {
                    String source = rating.path("Source").asText();
                    String value = rating.path("Value").asText();

                    if ("Rotten Tomatoes".equals(source)) {
                        result.setRottenTomatoesRating(value); // e.g. "94%"
                    }
                    if ("Metacritic".equals(source)) {
                        result.setMetacriticRating(value); // e.g. "88/100"
                    }
                }
            }

        } catch (Exception e) {
            // Ratings unavailable — result still valid without them
        }
    }
}
