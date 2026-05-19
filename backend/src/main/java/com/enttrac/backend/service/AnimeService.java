package com.enttrac.backend.service;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.AnimeItem;
import com.enttrac.backend.model.result.AnimeSearchResult;
import com.enttrac.backend.repository.AnimeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// TODO: refactor into shared MediaService when adding third medium
@Service
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final MediaMetadataClient<AnimeSearchResult> animeMetadataClient;

    public AnimeService(AnimeRepository animeRepository,
                        @Qualifier("jikanClient") MediaMetadataClient<AnimeSearchResult> animeMetadataClient) {
        this.animeRepository = animeRepository;
        this.animeMetadataClient = animeMetadataClient;
    }

    public List<AnimeSearchResult> search(String query) {
        return animeMetadataClient.search(query);
    }

    public AnimeSearchResult getDetails(String id) {
        return animeMetadataClient.getDetails(id);
    }

    public List<AnimeItem> getLibrary() {
        return animeRepository.findAll();
    }

    public AnimeItem getAnime(String animeId) {
        return animeRepository.findById(animeId);
    }

    public AnimeItem addToLibrary(AnimeItem item) {
        AnimeItem existing = animeRepository.findById(item.getAnimeId());
        if (existing != null) {
            return existing;
        }
        item.setPk("USER#default");
        item.setSk("ANIME#JIKAN#" + item.getAnimeId());
        String now = Instant.now().toString();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        animeRepository.save(item);
        return item;
    }

    public AnimeItem updateProgress(String animeId, int episodesWatched) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        item.setEpisodesWatched(episodesWatched);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem updateScore(String animeId, int score) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem updateStatus(String animeId, String status) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem refreshLatestEpisode(String animeId) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        AnimeSearchResult details = animeMetadataClient.getDetails(animeId);
        if (details != null && details.getTotalEpisodes() != null) {
            item.setTotalEpisodes(details.getTotalEpisodes());
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
            animeRepository.save(item);
        }
        return item;
    }

    public Double getCommunityRating(String animeId) {
        return animeMetadataClient.getCommunityRating(animeId);
    }

    public void removeFromLibrary(String animeId) {
        animeRepository.delete(animeId);
    }

    public AnimeItem updateNotes(String animeId, String notes) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new NotFoundException("Anime not found: " + animeId);
        }
        item.setNotes(notes);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public List<AnimeItem> refreshAll() {
        List<AnimeItem> library = animeRepository.findAll();
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
                    if (details.getStatus() != null &&
                            !details.getStatus().equals(item.getSeriesStatus())) {
                        item.setSeriesStatus(details.getStatus());
                        changed = true;
                    }

                    item.setLastRefreshed(Instant.now().toString());
                    if (changed) item.setUpdatedAt(Instant.now().toString());
                    animeRepository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                updated.add(item);
            }
        }
        return updated;
    }

    public List<AnimeItem> refreshOngoing() {
        List<AnimeItem> library = animeRepository.findAll();
        List<AnimeItem> updated = new ArrayList<>();

        for (AnimeItem item : library) {
            try {
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

                    item.setLastRefreshed(Instant.now().toString());
                    if (changed) item.setUpdatedAt(Instant.now().toString());
                    animeRepository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                updated.add(item);
            }
        }
        return updated;
    }
}
