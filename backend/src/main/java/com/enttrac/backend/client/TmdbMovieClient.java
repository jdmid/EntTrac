package com.enttrac.backend.client;

import com.enttrac.backend.model.result.MovieSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

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

        return results;
    }

    @Override
    public MovieSearchResult getDetails(String id) {
        JsonNode response = restClient.get()
                .uri("/movie/{id}?language=en-US&append_to_response=credits&api_key={apiKey}",
                        id, apiKey)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) return null;

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

    @Override
    public Double getCommunityRating(String id) {
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
            // rating unavailable
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

        Double communityRating = null;
        if (movie.has("vote_average") && !movie.get("vote_average").isNull()) {
            communityRating = Math.round(
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
                .communityRating(communityRating)
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

        return results;
    }
}