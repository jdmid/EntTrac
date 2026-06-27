package com.enttrac.backend.service;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.client.TmdbTvClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.repository.TvRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Service
public class TvService extends MediaService<TvItem, TvSearchResult> {

    private final MediaMetadataClient<TvSearchResult> tvMetadataClient;

    public TvService(TvRepository tvRepository,
                     @Qualifier("tmdbTvClient") MediaMetadataClient<TvSearchResult> tvMetadataClient) {
        super(tvRepository);
        this.tvMetadataClient = tvMetadataClient;
    }

    @Override
    protected String getEntityId(TvItem item) { return item.getTvId(); }

    @Override
    protected String buildSortKey(TvItem item) { return "TV#TMDB#" + item.getTvId(); }

    @Override
    protected String getNotFoundMessage(String id) { return "TV show not found: " + id; }

    @Override
    protected void beforeSave(TvItem item) {
        if (item.getSeasonEpisodes() != null) {
            item.getSeasonEpisodes().removeIf(ep -> ep == null);
        }
    }

    public List<TvSearchResult> search(String query) {
        log.info("Searching TV shows: {}", query);
        return tvMetadataClient.search(query);
    }

    public TvSearchResult getDetails(String id) {
        log.info("Fetching TV show details: {}", id);
        return tvMetadataClient.getDetails(id);
    }

    public TvItem getTvShow(String tvId) {
        return repository.findById(tvId);
    }

    public TvItem updateProgress(String tvId, int episodesWatched, int currentSeason) {
        TvItem item = repository.findById(tvId);
        if (item == null) {
            throw new NotFoundException("TV show not found: " + tvId);
        }
        item.setEpisodesWatched(episodesWatched);
        item.setCurrentSeason(currentSeason);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        log.info("Updated TV progress: {} -> {} episodes watched, season {}", tvId, episodesWatched, currentSeason);
        return item;
    }

    private String normalizeSeriesStatus(String rawStatus) {
        if (rawStatus == null) return null;
        String status = rawStatus.toLowerCase().trim();
        if (status.equals("returning series")) return "ongoing";
        if (status.equals("in production")) return "in production";
        if (status.equals("pilot")) return "upcoming";
        if (status.equals("planned")) return "upcoming";
        if (status.equals("ended")) return "completed";
        if (status.equals("canceled")) return "cancelled";
        return null;
    }

    public TvItem refreshLatestEpisodes(String tvId) {
        TvItem item = repository.findById(tvId);
        if (item == null) {
            throw new NotFoundException("TV show not found: " + tvId);
        }
        TvSearchResult details = tvMetadataClient.getDetails(tvId);
        if (details != null) {
            if (details.getSeasonEpisodes() != null) {
                List<Integer> cleaned = new ArrayList<>(details.getSeasonEpisodes());
                cleaned.removeIf(ep -> ep == null);
                item.setSeasonEpisodes(cleaned);
            }
            if (details.getTotalEpisodes() != null) {
                item.setTotalEpisodes(details.getTotalEpisodes());
            }
            if (details.getNextEpisodeDate() != null) {
                item.setNextEpisodeDate(details.getNextEpisodeDate());
            }
            if (details.getStatus() != null) {
                item.setSeriesStatus(normalizeSeriesStatus(details.getStatus()));
            }
            if (details.getNumberOfSeasons() != null) {
                item.setNumberOfSeasons(details.getNumberOfSeasons());
            }
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());

        }

        if (item.getTmdbRating() == null) {
            Double rating = ((TmdbTvClient) tvMetadataClient).getTmdbRating(tvId);
            if (rating != null) item.setTmdbRating(rating);
        }

        repository.save(item);
        log.info("Refreshed TV show: {}", tvId);

        return item;
    }

    public List<TvItem> refreshAll() {
        log.info("Refreshing all TV shows in library");
        List<TvItem> library = repository.findAll();
        List<TvItem> updated = new ArrayList<>();

        for (TvItem item : library) {
            try {
                TvSearchResult details = tvMetadataClient.getDetails(item.getTvId());
                if (details != null) {
                    boolean changed = false;

                    if (details.getSeasonEpisodes() != null) {
                        List<Integer> cleaned = new ArrayList<>(details.getSeasonEpisodes());
                        cleaned.removeIf(Objects::isNull);
                        if (!cleaned.equals(item.getSeasonEpisodes())) {
                            item.setSeasonEpisodes(cleaned);
                            changed = true;
                        }
                    }
                    if (details.getTotalEpisodes() != null &&
                            !details.getTotalEpisodes().equals(item.getTotalEpisodes())) {
                        item.setTotalEpisodes(details.getTotalEpisodes());
                        changed = true;
                    }
                    if (details.getNextEpisodeDate() != null &&
                            !details.getNextEpisodeDate().equals(item.getNextEpisodeDate())) {
                        item.setNextEpisodeDate(details.getNextEpisodeDate());
                        changed = true;
                    }
                    if (details.getStatus() != null) {
                        String normalized = normalizeSeriesStatus(details.getStatus());
                        if (!Objects.equals(normalized, item.getSeriesStatus())) {
                            item.setSeriesStatus(normalized);
                            changed = true;
                        }
                    }
                    if (details.getNumberOfSeasons() != null &&
                            !details.getNumberOfSeasons().equals(item.getNumberOfSeasons())) {
                        item.setNumberOfSeasons(details.getNumberOfSeasons());
                        changed = true;
                    }

                    item.setLastRefreshed(Instant.now().toString());
                    if (changed) item.setUpdatedAt(Instant.now().toString());
                    repository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                log.debug("Failed to refresh TV show {} during refreshAll: {}", item.getTvId(), e.getMessage());
                updated.add(item);
            }
        }

        log.info("Finished refreshing all TV shows: {} items processed", updated.size());
        return updated;
    }

    public List<TvItem> refreshOngoing() {
        log.info("Refreshing ongoing TV shows in library");
        List<TvItem> library = repository.findAll();
        List<TvItem> updated = new ArrayList<>();

        for (TvItem item : library) {
            try {
                // Items with null or unknown seriesStatus are included intentionally —
                // refreshing them may populate their status from the API
                if ("completed".equals(item.getSeriesStatus()) ||
                        "cancelled".equals(item.getSeriesStatus())) {
                    updated.add(item);
                    continue;
                }
                TvSearchResult details = tvMetadataClient.getDetails(item.getTvId());
                if (details != null) {
                    boolean changed = false;

                    if (details.getSeasonEpisodes() != null) {
                        List<Integer> cleaned = new ArrayList<>(details.getSeasonEpisodes());
                        cleaned.removeIf(Objects::isNull);
                        if (!cleaned.equals(item.getSeasonEpisodes())) {
                            item.setSeasonEpisodes(cleaned);
                            changed = true;
                        }
                    }
                    if (details.getTotalEpisodes() != null &&
                            !details.getTotalEpisodes().equals(item.getTotalEpisodes())) {
                        item.setTotalEpisodes(details.getTotalEpisodes());
                        changed = true;
                    }
                    if (details.getNextEpisodeDate() != null &&
                            !details.getNextEpisodeDate().equals(item.getNextEpisodeDate())) {
                        item.setNextEpisodeDate(details.getNextEpisodeDate());
                        changed = true;
                    }
                    if (details.getStatus() != null) {
                        String normalized = normalizeSeriesStatus(details.getStatus());
                        if (!Objects.equals(normalized, item.getSeriesStatus())) {
                            item.setSeriesStatus(normalized);
                            changed = true;
                        }
                    }
                    if (details.getNumberOfSeasons() != null &&
                            !details.getNumberOfSeasons().equals(item.getNumberOfSeasons())) {
                        item.setNumberOfSeasons(details.getNumberOfSeasons());
                        changed = true;
                    }

                    item.setLastRefreshed(Instant.now().toString());
                    if (changed) item.setUpdatedAt(Instant.now().toString());
                    repository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                log.debug("Failed to refresh TV show {} during refreshOngoing: {}", item.getTvId(), e.getMessage());
                updated.add(item);
            }
        }
        log.info("Finished refreshing ongoing TV shows: {} items processed", updated.size());
        return updated;
    }

    public TvItem enrichTmdbRating(String tvId) {
        TvItem item = repository.findById(tvId);
        if (item == null) throw new NotFoundException("TV show not found: " + tvId);

        if (item.getTmdbRating() == null) {
            Double rating = ((TmdbTvClient) tvMetadataClient).getTmdbRating(tvId);
            if (rating != null) {
                item.setTmdbRating(rating);
                item.setUpdatedAt(Instant.now().toString());
                repository.save(item);
                log.info("Enriched TV show {} with TMDB rating: {}", tvId, rating);
            }
        }
        return item;
    }

    public List<TvSearchResult> getWorksByCreator(String personId) {
        return tvMetadataClient.getWorksByCreator(personId);
    }

    public List<Map<String, String>> searchPeople(String name) {
        return tvMetadataClient.searchCreators(name);
    }

    public TvItem enrichWatchProviders(String tvId, String region) {
        TvItem item = repository.findById(tvId);
        if (item == null) throw new NotFoundException("TV show not found: " + tvId);

        boolean needsRefresh = item.getWatchProvidersRefreshedAt() == null
                || Instant.now().isAfter(
                Instant.parse(item.getWatchProvidersRefreshedAt())
                        .plus(Duration.ofDays(7)));

        if (needsRefresh) {
            List<String> providers =
                    // Cast required — getWatchProviders is not on MediaMetadataClient interface
                    ((TmdbTvClient) tvMetadataClient).getWatchProviders(tvId, region);
            item.setWatchProviders(providers);
            item.setWatchProvidersRefreshedAt(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
            repository.save(item);
            log.info("Enriched TV show {} with {} watch providers for region {}",
                    tvId, providers.size(), region);
        }

        return item;
    }
}