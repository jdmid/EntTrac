package com.enttrac.backend.service;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.repository.TvRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TvService {

    private final TvRepository tvRepository;
    private final MediaMetadataClient<TvSearchResult> tvMetadataClient;

    public TvService(TvRepository tvRepository,
                     @Qualifier("tmdbClient") MediaMetadataClient<TvSearchResult> tvMetadataClient) {
        this.tvRepository = tvRepository;
        this.tvMetadataClient = tvMetadataClient;
    }

    public List<TvSearchResult> search(String query) {
        return tvMetadataClient.search(query);
    }

    public TvSearchResult getDetails(String id) {
        return tvMetadataClient.getDetails(id);
    }

    public List<TvItem> getLibrary() {
        return tvRepository.findAll();
    }

    public TvItem getTvShow(String tvId) {
        return tvRepository.findById(tvId);
    }

    public TvItem addToLibrary(TvItem item) {
        TvItem existing = tvRepository.findById(item.getTvId());
        if (existing != null) {
            return existing;
        }
        item.setPk("USER#default");
        item.setSk("TV#TMDB#" + item.getTvId());
        item.setUpdatedAt(Instant.now().toString());
        // Guard against null values in seasonEpisodes list
        if (item.getSeasonEpisodes() != null) {
            item.getSeasonEpisodes().removeIf(ep -> ep == null);
        }
        tvRepository.save(item);
        return item;
    }

    public TvItem updateProgress(String tvId, int episodesWatched, int currentSeason) {
        TvItem item = tvRepository.findById(tvId);
        if (item == null) {
            throw new NotFoundException("TV show not found: " + tvId);
        }
        item.setEpisodesWatched(episodesWatched);
        item.setCurrentSeason(currentSeason);
        item.setUpdatedAt(Instant.now().toString());
        tvRepository.save(item);
        return item;
    }
    private String normalizeSeriesStatus(String rawStatus) {
        if (rawStatus == null) return null;
        String status = rawStatus.toLowerCase().trim();
        if (status.equals("returning series")) return "ongoing";
        if (status.equals("in production")) return "in production";
        if (status.equals("pilot")) return "upcoming";
        if (status.equals("planned")) return "upcoming";
        if (status.equals("ended")) return "finished";
        if (status.equals("canceled")) return "cancelled";
        return null;
    }

    public TvItem updateScore(String tvId, int score) {
        TvItem item = tvRepository.findById(tvId);
        if (item == null) {
            throw new NotFoundException("TV show not found: " + tvId);
        }
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        tvRepository.save(item);
        return item;
    }

    public TvItem updateStatus(String tvId, String status) {
        TvItem item = tvRepository.findById(tvId);
        if (item == null) {
            throw new NotFoundException("TV show not found: " + tvId);
        }
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        tvRepository.save(item);
        return item;
    }

    public TvItem updateNotes(String tvId, String notes) {
        TvItem item = tvRepository.findById(tvId);
        if (item == null) {
            throw new NotFoundException("TV show not found: " + tvId);
        }
        item.setNotes(notes);
        item.setUpdatedAt(Instant.now().toString());
        tvRepository.save(item);
        return item;
    }

    public TvItem refreshLatestEpisodes(String tvId) {
        TvItem item = tvRepository.findById(tvId);
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
            tvRepository.save(item);
        }
        return item;
    }

    public Double getCommunityRating(String tvId) {
        return tvMetadataClient.getCommunityRating(tvId);
    }

    public void removeFromLibrary(String tvId) {
        tvRepository.delete(tvId);
    }

    public List<TvItem> refreshAll() {
        List<TvItem> library = tvRepository.findAll();
        List<TvItem> updated = new ArrayList<>();

        for (TvItem item : library) {
            try {
                TvSearchResult details = tvMetadataClient.getDetails(item.getTvId());
                if (details != null) {
                    if (details.getSeasonEpisodes() != null) {
                        List<Integer> cleaned = new ArrayList<>(details.getSeasonEpisodes());
                        cleaned.removeIf(Objects::isNull);
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
                    tvRepository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                updated.add(item);
            }
        }

        return updated;
    }
}