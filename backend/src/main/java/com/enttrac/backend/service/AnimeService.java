package com.enttrac.backend.service;

import com.enttrac.backend.client.JikanClient;
import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.AnimeItem;
import com.enttrac.backend.model.result.AnimeSearchResult;
import com.enttrac.backend.repository.AnimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AnimeService extends MediaService<AnimeItem, AnimeSearchResult> {

    private final MediaMetadataClient<AnimeSearchResult> animeMetadataClient;

    public AnimeService(AnimeRepository animeRepository,
                        @Qualifier("jikanClient") MediaMetadataClient<AnimeSearchResult> animeMetadataClient) {
        super(animeRepository);
        this.animeMetadataClient = animeMetadataClient;
    }

    @Override
    protected String getEntityId(AnimeItem item) { return item.getAnimeId(); }

    @Override
    protected String buildSortKey(AnimeItem item) { return "ANIME#ANILIST#" + item.getAnimeId(); }

    @Override
    protected String getNotFoundMessage(String id) { return "Anime not found: " + id; }

    public List<AnimeSearchResult> search(String query) {
        log.info("Searching anime: {}", query);
        return animeMetadataClient.search(query);
    }

    public AnimeSearchResult getDetails(String id) {
        log.info("Fetching anime details: {}", id);
        return animeMetadataClient.getDetails(id);
    }


    public AnimeItem getAnime(String animeId) {
        return repository.findById(animeId);
    }


    public AnimeItem updateProgress(String animeId, int episodesWatched) {
        AnimeItem item = repository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        item.setEpisodesWatched(episodesWatched);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        log.info("Updated anime progress: {} -> {} episodes watched", animeId, episodesWatched);
        return item;
    }

    public AnimeItem refreshLatestEpisode(String animeId) {
        AnimeItem item = repository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        AnimeSearchResult details = animeMetadataClient.getDetails(animeId);
        if (details != null && details.getTotalEpisodes() != null) {
            item.setTotalEpisodes(details.getTotalEpisodes());
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());

        }

        if (item.getMalRating() == null) {
            Double rating = ((JikanClient) animeMetadataClient).getMalRating(animeId);
            if (rating != null) item.setMalRating(rating);
        }

        repository.save(item);
        log.info("Refreshed anime: {}", animeId);

        return item;
    }

    public List<AnimeItem> refreshAll() {
        log.info("Refreshing all anime in library");
        List<AnimeItem> library = repository.findAll();
        List<AnimeItem> updated = new ArrayList<>();

        for (AnimeItem item : library) {
            try {
                AnimeSearchResult details = animeMetadataClient.getDetails(item.getAnimeId());
                if (details != null) {
                    boolean changed = false;

                    if (details.getLatestEpisode() != null &&
                            !details.getLatestEpisode().equals(item.getLatestEpisode())) {
                        item.setLatestEpisode(details.getLatestEpisode());
                        changed = true;
                    }
                    if (details.getTotalEpisodes() != null &&
                            !details.getTotalEpisodes().equals(item.getTotalEpisodes())) {
                        item.setTotalEpisodes(details.getTotalEpisodes());
                        changed = true;
                    }
                    if (details.getStatus() != null) {
                        String normalized = normalizeAnimeStatus(details.getStatus());
                        if (!Objects.equals(normalized, item.getSeriesStatus())) {
                            item.setSeriesStatus(normalized);
                            changed = true;
                        }
                    }

                    if (changed) {
                        item.setLastRefreshed(Instant.now().toString());
                        item.setUpdatedAt(Instant.now().toString());
                        repository.save(item);
                    }
                }
                updated.add(item);
            } catch (Exception e) {
                log.debug("Failed to refresh anime {} during refreshAll: {}", item.getAnimeId(), e.getMessage());
                updated.add(item);
            }
        }
        log.info("Finished refreshing all anime: {} items processed", updated.size());
        return updated;
    }

    public List<AnimeItem> refreshOngoing() {
        log.info("Refreshing ongoing anime in library");
        List<AnimeItem> library = repository.findAll();
        List<AnimeItem> updated = new ArrayList<>();

        for (AnimeItem item : library) {
            try {
                // Items with null or unknown seriesStatus are included intentionally —
                // refreshing them may populate their status from the API
                if ("completed".equals(item.getSeriesStatus()) ||
                        "cancelled".equals(item.getSeriesStatus())) {
                    updated.add(item);
                    continue;
                }
                AnimeSearchResult details = animeMetadataClient.getDetails(item.getAnimeId());
                if (details != null) {
                    boolean changed = false;

                    if (details.getLatestEpisode() != null &&
                            !details.getLatestEpisode().equals(item.getLatestEpisode())) {
                        item.setLatestEpisode(details.getLatestEpisode());
                        changed = true;
                    }
                    if (details.getTotalEpisodes() != null &&
                            !details.getTotalEpisodes().equals(item.getTotalEpisodes())) {
                        item.setTotalEpisodes(details.getTotalEpisodes());
                        changed = true;
                    }

                    if (changed) {
                        item.setLastRefreshed(Instant.now().toString());
                        item.setUpdatedAt(Instant.now().toString());
                        repository.save(item);
                    }
                }
                updated.add(item);
            } catch (Exception e) {
                log.debug("Failed to refresh anime {} during refreshOngoing: {}", item.getAnimeId(), e.getMessage());
                updated.add(item);
            }
        }
        log.info("Finished refreshing ongoing anime: {} items processed", updated.size());
        return updated;
    }

    private String normalizeAnimeStatus(String rawStatus) {
        if (rawStatus == null) return null;
        String status = rawStatus.toLowerCase().trim();
        if (status.equals("currently airing")) return "ongoing";
        if (status.equals("finished airing")) return "completed";
        if (status.equals("not yet aired")) return "upcoming";
        return null;
    }

    public AnimeItem enrichMalRating(String animeId) {
        AnimeItem item = repository.findById(animeId);
        if (item == null) throw new NotFoundException("Anime not found: " + animeId);

        if (item.getMalRating() == null) {
            Double rating = ((JikanClient) animeMetadataClient).getMalRating(animeId);
            if (rating != null) {
                item.setMalRating(rating);
                item.setUpdatedAt(Instant.now().toString());
                repository.save(item);
                log.info("Enriched anime {} with MAL rating: {}", animeId, rating);
            }
        }
        return item;
    }

    public JikanClient.PagedResult<AnimeSearchResult> getWorksByProducer(
            String producerId, int page, String name) {
        return ((JikanClient) animeMetadataClient).getWorksByProducer(producerId, page, name);
    }

    public List<Map<String, String>> searchProducers(String name) {
        return animeMetadataClient.searchCreators(name);
    }
}
