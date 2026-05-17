package com.enttrac.backend.client;

import com.enttrac.backend.model.result.TvSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("tmdbClient")
public class TmdbClient implements MediaMetadataClient<TvSearchResult> {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    private final RestClient restClient;

    public TmdbClient(@Value("${tmdb.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("accept", "application/json")
                .build();
    }

    @Override
    public List<TvSearchResult> search(String query) {
        JsonNode response = restClient.get()
                .uri("/search/tv?query={query}&language=en-US&page=1", query)
                .retrieve()
                .body(JsonNode.class);

        List<TvSearchResult> results = new ArrayList<>();

        if (response != null && response.has("results")) {
            for (JsonNode show : response.get("results")) {
                results.add(mapToSearchResult(show));
            }
        }

        return results;
    }

    @Override
    public TvSearchResult getDetails(String id) {
        JsonNode response = restClient.get()
                .uri("/tv/{id}?language=en-US&append_to_response=aggregate_credits", id)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) return null;

        TvSearchResult result = mapToSearchResult(response);

        // Build season episode array using only aired episodes
        List<Integer> seasonEpisodes = buildSeasonEpisodeArray(response, id);
        result.setSeasonEpisodes(seasonEpisodes);

        // Total aired episodes across all seasons
        int total = seasonEpisodes.stream().mapToInt(Integer::intValue).sum();
        result.setTotalEpisodes(total);

        // Next episode date
        if (response.has("next_episode_to_air")
                && !response.get("next_episode_to_air").isNull()) {
            JsonNode next = response.get("next_episode_to_air");
            if (next.has("air_date") && !next.get("air_date").isNull()) {
                String episodeName = next.has("name")
                        && !next.get("name").isNull()
                        ? next.get("name").asText() : "";
                int seasonNum = next.has("season_number")
                        ? next.get("season_number").asInt() : 0;
                int epNum = next.has("episode_number")
                        ? next.get("episode_number").asInt() : 0;
                String airDate = next.get("air_date").asText();
                result.setNextEpisodeDate(
                        "S" + seasonNum + " E" + epNum
                                + (episodeName.isEmpty() ? "" : " · \"" + episodeName + "\"")
                                + " · " + airDate
                );
            }
        }

        return result;
    }

    @Override
    public Double getCommunityRating(String id) {
        try {
            JsonNode response = restClient.get()
                    .uri("/tv/{id}?language=en-US", id)
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

    private List<Integer> buildSeasonEpisodeArray(JsonNode showNode, String showId) {
        List<Integer> seasonEpisodes = new ArrayList<>();

        if (!showNode.has("seasons")) return seasonEpisodes;

        // Cutoff — one day in the future to avoid episodes airing later today
        LocalDate cutoff = LocalDate.now().minusDays(1);

        for (JsonNode season : showNode.get("seasons")) {
            // Skip season 0 — that is TMDB's bucket for specials
            int seasonNum = season.has("season_number")
                    ? season.get("season_number").asInt() : 0;
            if (seasonNum == 0) continue;

            // Fetch full season details to get per-episode air dates
            JsonNode seasonDetails = restClient.get()
                    .uri("/tv/{id}/season/{season}?language=en-US", showId, seasonNum)
                    .retrieve()
                    .body(JsonNode.class);

            if (seasonDetails == null || !seasonDetails.has("episodes")) {
                seasonEpisodes.add(0);
                continue;
            }

            int airedCount = 0;
            for (JsonNode episode : seasonDetails.get("episodes")) {
                if (episode.has("air_date") && !episode.get("air_date").isNull()) {
                    String airDateStr = episode.get("air_date").asText();
                    if (!airDateStr.isEmpty()) {
                        try {
                            LocalDate airDate = LocalDate.parse(airDateStr);
                            if (!airDate.isAfter(cutoff)) {
                                airedCount++;
                            }
                        } catch (Exception e) {
                            // skip malformed dates
                        }
                    }
                }
            }
            seasonEpisodes.add(airedCount);
        }

        return seasonEpisodes;
    }

    private TvSearchResult mapToSearchResult(JsonNode show) {
        String id = show.has("id") ? show.get("id").asText() : null;

        String title = null;
        if (show.has("name") && !show.get("name").isNull()) {
            title = show.get("name").asText();
        }

        String description = null;
        if (show.has("overview") && !show.get("overview").isNull()) {
            description = show.get("overview").asText();
        }

        String coverUrl = null;
        if (show.has("poster_path") && !show.get("poster_path").isNull()) {
            coverUrl = IMAGE_BASE + show.get("poster_path").asText();
        }

        String status = null;
        if (show.has("status") && !show.get("status").isNull()) {
            status = show.get("status").asText();
        }

        String network = null;
        if (show.has("networks") && show.get("networks").isArray()
                && show.get("networks").size() > 0) {
            network = show.get("networks").get(0).get("name").asText();
        }

        String genres = null;
        if (show.has("genres") && show.get("genres").isArray()) {
            List<String> genreList = new ArrayList<>();
            for (JsonNode g : show.get("genres")) {
                genreList.add(g.get("name").asText());
            }
            if (!genreList.isEmpty()) {
                genres = String.join(", ", genreList);
            }
        }

        String firstAirYear = null;
        if (show.has("first_air_date") && !show.get("first_air_date").isNull()) {
            String date = show.get("first_air_date").asText();
            if (date.length() >= 4) {
                firstAirYear = date.substring(0, 4);
            }
        }

        String seriesType = null;
        if (show.has("type") && !show.get("type").isNull()) {
            seriesType = show.get("type").asText();
        }

        Integer numberOfSeasons = null;
        if (show.has("number_of_seasons") && !show.get("number_of_seasons").isNull()) {
            numberOfSeasons = show.get("number_of_seasons").asInt();
        }

        Double communityRating = null;
        if (show.has("vote_average") && !show.get("vote_average").isNull()) {
            communityRating = Math.round(
                    show.get("vote_average").asDouble() * 10.0) / 10.0;
        }

        return TvSearchResult.builder()
                .id(id)
                .title(title)
                .description(description)
                .coverUrl(coverUrl)
                .status(status)
                .network(network)
                .genres(genres)
                .firstAirYear(firstAirYear)
                .seriesType(seriesType)
                .communityRating(communityRating)
                .numberOfSeasons(numberOfSeasons)
                .build();
    }
}
