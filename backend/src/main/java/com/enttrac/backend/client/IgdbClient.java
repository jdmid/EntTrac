package com.enttrac.backend.client;

import com.enttrac.backend.model.result.GameSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component("igdbClient")
public class IgdbClient implements MediaMetadataClient<GameSearchResult> {

    private static final String IGDB_BASE_URL = "https://api.igdb.com/v4";
    private static final String TWITCH_TOKEN_URL = "https://id.twitch.tv/oauth2/token";

    private final String clientId;
    private final String clientSecret;
    private final RestClient igdbClient;
    private final RestClient twitchClient;

    private String accessToken;
    private Instant tokenExpiry;

    public IgdbClient(@Value("${igdb.client.id}") String clientId,
                      @Value("${igdb.client.secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.igdbClient = RestClient.builder()
                .baseUrl(IGDB_BASE_URL)
                .build();
        this.twitchClient = RestClient.builder()
                .baseUrl(TWITCH_TOKEN_URL)
                .build();
    }

    // --- Token management ---

    private synchronized String getToken() {
        if (accessToken == null || Instant.now().isAfter(tokenExpiry)) {
            log.info("IGDB token missing or expired, fetching new token");
            fetchNewToken();
        }
        return accessToken;
    }

    private void fetchNewToken() {
        JsonNode response = twitchClient.post()
                .uri("?client_id={clientId}&client_secret={clientSecret}&grant_type=client_credentials",
                        clientId, clientSecret)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) throw new RuntimeException("Failed to fetch Twitch token");

        this.accessToken = response.get("access_token").asText();
        long expiresIn = response.get("expires_in").asLong();
        // Subtract 60s buffer so we never use an about-to-expire token
        this.tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);
        log.info("Fetched new IGDB token, expires in {}s", expiresIn - 60);
    }

    // --- Shared query helper ---

    private JsonNode postQuery(String endpoint, String body) {
        return igdbClient.post()
                .uri(endpoint)
                .header("Client-ID", clientId)
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "text/plain")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    // --- MediaMetadataClient implementation ---

    @Override
    public List<GameSearchResult> search(String query) {
        String body = """
                fields name,cover.url,genres.name,platforms.name,
                       first_release_date,involved_companies.company.name,
                       involved_companies.developer,rating,aggregated_rating,
                       status,summary, category;
                search "%s";
                limit 25;
                """.formatted(query);

        JsonNode response = postQuery("/games", body);

        List<GameSearchResult> results = new ArrayList<>();
        if (response != null && response.isArray()) {
            for (JsonNode game : response) {
                // Filter out DLC (category 1) on the Java side
                // search keyword doesn't support where filters in IGDB
                int cat = game.has("category") ? game.get("category").asInt(-1) : -1;
                if (cat == 1) continue;
                results.add(mapToSearchResult(game));
            }
        }
        log.info("IGDB search for '{}' returned {} results", query, results.size());
        return results;
    }

    @Override
    public GameSearchResult getDetails(String id) {
        String body = """
            fields name,cover.url,genres.name,platforms.name,
                   first_release_date,involved_companies.company.name,
                   involved_companies.developer,rating,aggregated_rating,
                   status,summary,category;
            where id = %s;
            """.formatted(id);

        JsonNode response = postQuery("/games", body);

        if (response == null || !response.isArray() || response.isEmpty()){
            log.warn("IGDB returned no data for game id: {}", id);
            return null;
        }

        GameSearchResult result = mapToSearchResult(response.get(0));

        // Fetch DLC for this game
        List<GameSearchResult> dlc = fetchDlc(id);
        result.setDlc(dlc.isEmpty() ? null : dlc);
        log.info("Fetched game details for id {}: {} DLC entries", id, dlc.size());

        return result;
    }

    private List<GameSearchResult> fetchDlc(String parentId) {
        try {
            String body = "fields name,cover.url,status,summary,first_release_date,category;where parent_game = %s;limit 25;".formatted(parentId);

            JsonNode response = postQuery("/games", body);

            List<GameSearchResult> results = new ArrayList<>();
            if (response != null && response.isArray()) {
                for (JsonNode dlc : response) {
                    results.add(mapToSearchResult(dlc));
                }
            }
            return results;
        } catch (Exception e) {
            log.debug("Failed to fetch DLC for game {}: {}", parentId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Double getCommunityRating(String id) {
        try {
            String body = """
                    fields rating;
                    where id = %s;
                    """.formatted(id);

            JsonNode response = postQuery("/games", body);

            if (response != null && response.isArray() && response.size() > 0) {
                JsonNode game = response.get(0);
                if (game.has("rating") && !game.get("rating").isNull()) {
                    double rating = game.get("rating").asDouble();
                    return Math.round(rating * 10.0) / 10.0;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch IGDB community rating for game {}: {}", id, e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameSearchResult> getWorksByCreator(String companyId) {
        // Step 1: get game IDs where this company is a developer
        String involvedBody = """
                fields game;
                where company = %s & developer = true;
                limit 50;
                """.formatted(companyId);

        JsonNode involvedResponse = postQuery("/involved_companies", involvedBody);

        List<String> gameIds = new ArrayList<>();
        if (involvedResponse != null && involvedResponse.isArray()) {
            for (JsonNode entry : involvedResponse) {
                if (entry.has("game")) {
                    gameIds.add(entry.get("game").asText());
                }
            }
        }

        if (gameIds.isEmpty()){
            log.info("No games found for company: {}", companyId);
            return List.of();
        }

        log.info("Found {} game IDs for company {}, fetching details", gameIds.size(), companyId);

        // Step 2: fetch those games
        String idsJoined = gameIds.stream().collect(Collectors.joining(",", "(", ")"));
        String gamesBody = """
                fields name,cover.url,genres.name,platforms.name,
                       first_release_date,rating,aggregated_rating,status,category;
                where id = %s;
                limit 50;
                """.formatted(idsJoined);

        JsonNode gamesResponse = postQuery("/games", gamesBody);

        List<GameSearchResult> results = new ArrayList<>();
        if (gamesResponse != null && gamesResponse.isArray()) {
            for (JsonNode game : gamesResponse) {
                results.add(mapToSearchResult(game));
            }
        }
        log.info("Fetched {} games for company: {}", results.size(), companyId);
        return results;
    }

    @Override
    public List<Map<String, String>> searchCreators(String name) {
        String body = """
                fields name;
                search "%s";
                limit 10;
                """.formatted(name);

        JsonNode response = postQuery("/companies", body);

        List<Map<String, String>> results = new ArrayList<>();
        if (response != null && response.isArray()) {
            for (JsonNode company : response) {
                String id = company.path("id").asText();
                String companyName = company.path("name").asText();
                if (!id.isBlank() && !companyName.isBlank()) {
                    results.add(Map.of("id", id, "name", companyName));
                }
            }
        }
        return results;
    }

    // --- Mapping ---

    private GameSearchResult mapToSearchResult(JsonNode game) {
        String id = game.path("id").asText();

        String title = game.path("name").asText(null);

        String description = game.has("summary") && !game.get("summary").isNull()
                ? game.get("summary").asText() : null;

        // Cover URL — prepend https:, swap to t_cover_big
        String coverUrl = null;
        if (game.has("cover") && game.get("cover").has("url")) {
            coverUrl = "https:" + game.get("cover").get("url").asText()
                    .replace("t_thumb", "t_cover_big");
        }

        // Release year from Unix timestamp
        String releaseYear = null;
        if (game.has("first_release_date") && !game.get("first_release_date").isNull()) {
            long epoch = game.get("first_release_date").asLong();
            releaseYear = String.valueOf(
                    java.time.Instant.ofEpochSecond(epoch)
                            .atZone(java.time.ZoneOffset.UTC)
                            .getYear()
            );
        }

        // Genres
        String genres = null;
        if (game.has("genres") && game.get("genres").isArray()) {
            List<String> genreList = StreamSupport
                    .stream(game.get("genres").spliterator(), false)
                    .map(g -> g.path("name").asText())
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            if (!genreList.isEmpty()) genres = String.join(", ", genreList);
        }

        // Platforms
        List<String> platforms = new ArrayList<>();
        if (game.has("platforms") && game.get("platforms").isArray()) {
            for (JsonNode p : game.get("platforms")) {
                String pName = p.path("name").asText();
                if (!pName.isBlank()) platforms.add(pName);
            }
        }

        // Developer — first involved_company where developer = true
        String developer = null;
        String developerId = null;
        if (game.has("involved_companies") && game.get("involved_companies").isArray()) {
            for (JsonNode ic : game.get("involved_companies")) {
                if (ic.path("developer").asBoolean(false)) {
                    developer = ic.path("company").path("name").asText(null);
                    developerId = ic.path("company").path("id").asText(null);
                    break;
                }
            }
        }

        // Status — normalize IGDB integer status to our vocabulary
        String status = null;
        if (game.has("status") && !game.get("status").isNull()
                && !game.get("status").asText().isBlank()) {
            status = normalizeStatus(game.get("status").asInt(-1));
        } else {
            // IGDB omits status field — infer from release date
            if (game.has("first_release_date") && !game.get("first_release_date").isNull()) {
                long epochSeconds = game.get("first_release_date").asLong();
                if (java.time.Instant.ofEpochSecond(epochSeconds).isAfter(java.time.Instant.now())) {
                    status = "upcoming";
                } else {
                    status = "released";
                }
            } else {
                // No status, no release date — default to released
                status = "released";
            }
        }

        // Ratings
        Double igdbRating = null;
        if (game.has("rating") && !game.get("rating").isNull()) {
            igdbRating = Math.round(game.get("rating").asDouble() * 10.0) / 10.0;
        }

        Double igdbCriticRating = null;
        if (game.has("aggregated_rating") && !game.get("aggregated_rating").isNull()) {
            igdbCriticRating = Math.round(
                    game.get("aggregated_rating").asDouble() * 10.0) / 10.0;
        }

        // Category
        String category = null;
        if (game.has("category") && !game.get("category").isNull()) {
            category = normalizeCategory(game.get("category").asInt(-1));
        }

        return GameSearchResult.builder()
                .id(id)
                .title(title)
                .description(description)
                .coverUrl(coverUrl)
                .status(status)
                .releaseYear(releaseYear)
                .genres(genres)
                .platforms(platforms)
                .developer(developer)
                .developerId(developerId)
                .igdbRating(igdbRating)
                .igdbCriticRating(igdbCriticRating)
                .category(category)
                .dlc(null) // populated only in getDetails()
                .build();
    }

    private String normalizeStatus(int igdbStatus) {
        return switch (igdbStatus) {
            case 0 -> "released";
            case 6 -> "cancelled";
            case 7, 8 -> "upcoming";
            default -> null;
        };
    }

    private String normalizeCategory(int igdbCategory) {
        return switch (igdbCategory) {
            case 0 -> "Main Game";
            case 1 -> "DLC";
            case 2 -> "Expansion";
            case 3 -> "Bundle";
            case 4 -> "Standalone Expansion";
            case 8 -> "Remake";
            case 9 -> "Remaster";
            case 10 -> "Expanded Game";
            case 11 -> "Port";
            case 12 -> "Fork";
            default -> null;
        };
    }
}
