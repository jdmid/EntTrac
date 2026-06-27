package com.enttrac.backend.client;

import com.enttrac.backend.model.result.MovieSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("tmdbMovieClient")
public class TmdbMovieClient implements MediaMetadataClient<MovieSearchResult> {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    private final String apiKey;
    private final RestClient restClient;

    public TmdbMovieClient(@Value("${tmdb.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("accept", "application/json")
                .build();
    }

    @Override
    public List<MovieSearchResult> search(String query) {
        JsonNode response = restClient.get()
                .uri("/search/movie?query={query}&language=en-US&page=1&api_key={apiKey}",
                        query, apiKey)
                .retrieve()
                .body(JsonNode.class);

        List<MovieSearchResult> results = new ArrayList<>();

        if (response != null && response.has("results")) {
            for (JsonNode movie : response.get("results")) {
                results.add(mapToSearchResult(movie));
            }
        }

        log.info("TMDB movie search for '{}' returned {} results", query, results.size());
        return results;
    }

    @Override
    public MovieSearchResult getDetails(String id) {
        JsonNode response = restClient.get()
                .uri("/movie/{id}?language=en-US&append_to_response=credits&api_key={apiKey}",
                        id, apiKey)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            log.warn("TMDB returned no data for movie id: {}", id);
            return null;
        }

        MovieSearchResult result = mapToSearchResult(response);

        // Extract director from credits
        if (response.has("credits") && response.get("credits").has("crew")) {
            for (JsonNode member : response.get("credits").get("crew")) {
                if ("Director".equals(member.path("job").asText())) {
                    result.setDirector(member.path("name").asText());
                    result.setDirectorId(member.path("id").asText());
                    break;
                }
            }
        }

        // Runtime comes back as integer minutes from detail endpoint
        if (response.has("runtime") && !response.get("runtime").isNull()) {
            result.setRuntime(response.get("runtime").asInt() + " min");
        }

        // IMDb ID for OMDB chaining
        if (response.has("imdb_id") && !response.get("imdb_id").isNull()) {
            result.setImdbId(response.get("imdb_id").asText());
        }

        return result;
    }

    public Double getTmdbRating(String id) {
        try {
            JsonNode response = restClient.get()
                    .uri("/movie/{id}?language=en-US&api_key={apiKey}", id, apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("vote_average")
                    && !response.get("vote_average").isNull()) {
                double rating = response.get("vote_average").asDouble();
                return Math.round(rating * 10.0) / 10.0;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch TMDB community rating for movie {}: {}", id, e.getMessage());
        }
        return null;
    }

    private MovieSearchResult mapToSearchResult(JsonNode movie) {
        String id = movie.has("id") ? movie.get("id").asText() : null;

        String title = null;
        if (movie.has("title") && !movie.get("title").isNull()) {
            title = movie.get("title").asText();
        }

        String description = null;
        if (movie.has("overview") && !movie.get("overview").isNull()) {
            description = movie.get("overview").asText();
        }

        String coverUrl = null;
        if (movie.has("poster_path") && !movie.get("poster_path").isNull()) {
            coverUrl = IMAGE_BASE + movie.get("poster_path").asText();
        }

        String status = null;
        if (movie.has("status") && !movie.get("status").isNull()) {
            status = movie.get("status").asText();
        }

        String releaseYear = null;
        if (movie.has("release_date") && !movie.get("release_date").isNull()) {
            String date = movie.get("release_date").asText();
            if (date.length() >= 4) {
                releaseYear = date.substring(0, 4);
            }
        }

        String genres = null;
        if (movie.has("genres") && movie.get("genres").isArray()) {
            List<String> genreList = new ArrayList<>();
            for (JsonNode g : movie.get("genres")) {
                genreList.add(g.get("name").asText());
            }
            if (!genreList.isEmpty()) {
                genres = String.join(", ", genreList);
            }
        }

        Double tmdbRating = null;
        if (movie.has("vote_average") && !movie.get("vote_average").isNull()) {
            tmdbRating = Math.round(
                    movie.get("vote_average").asDouble() * 10.0) / 10.0;
        }

        return MovieSearchResult.builder()
                .id(id)
                .title(title)
                .description(description)
                .coverUrl(coverUrl)
                .status(status)
                .releaseYear(releaseYear)
                .genres(genres)
                .tmdbRating(tmdbRating)
                .build();
    }

    @Override
    public List<MovieSearchResult> getWorksByCreator(String creatorId) {        JsonNode response = restClient.get()
                .uri("/person/{id}/movie_credits?language=en-US&api_key={apiKey}",
                        creatorId, apiKey)
                .retrieve()
                .body(JsonNode.class);

        List<MovieSearchResult> results = new ArrayList<>();

        if (response != null && response.has("crew")) {
            for (JsonNode movie : response.get("crew")) {
                if ("Director".equals(movie.path("job").asText())) {
                    results.add(mapToSearchResult(movie));
                }
            }
        }

        log.info("Fetched {} directed movies for person: {}", results.size(), creatorId);
        return results;
    }

    @Override
    public List<Map<String, String>> searchCreators(String name) {
        JsonNode response = restClient.get()
                .uri("/search/person?query={name}&language=en-US&api_key={apiKey}",
                        name, apiKey)
                .retrieve()
                .body(JsonNode.class);

        List<Map<String, String>> results = new ArrayList<>();

        if (response != null && response.has("results")) {
            for (JsonNode person : response.get("results")) {
                String id = person.path("id").asText();
                String personName = person.path("name").asText();
                if (!id.isBlank() && !personName.isBlank()) {
                    results.add(Map.of("id", id, "name", personName));
                }
            }
        }

        return results;
    }

    public List<String> getWatchProviders(String id, String region) {
        try {
            JsonNode response = restClient.get()
                    .uri("/movie/{id}/watch/providers?api_key={apiKey}", id, apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("results")) {
                log.debug("No watch provider results for movie {} in region {}", id, region);
                return List.of();
            }

            JsonNode regionNode = response.get("results").get(region);
            if (regionNode == null) {
                log.debug("No watch providers for movie {} in region {}", id, region);
                return List.of();
            }

            if (!regionNode.has("flatrate")) {
                log.debug("No flatrate providers for movie {} in region {}", id, region);
                return List.of();
            }

            List<String> providers = new ArrayList<>();
            for (JsonNode provider : regionNode.get("flatrate")) {
                String name = provider.path("provider_name").asText();
                if (!name.isBlank()) providers.add(name);
            }

            log.info("Fetched {} flatrate providers for movie {} in region {}",
                    providers.size(), id, region);
            return providers;

        } catch (Exception e) {
            log.debug("Failed to fetch watch providers for movie {} in region {}: {}",
                    id, region, e.getMessage());
            return List.of();
        }
    }
}