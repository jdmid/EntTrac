package com.enttrac.backend.service;

import com.enttrac.backend.client.AnimeMetadataClient;
import com.enttrac.backend.model.AnimeItem;
import com.enttrac.backend.model.AnimeSearchResult;
import com.enttrac.backend.repository.AnimeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final AnimeMetadataClient animeMetadataClient;

    public AnimeService(AnimeRepository animeRepository, AnimeMetadataClient animeMetadataClient) {
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
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem updateProgress(String animeId, int episodesWatched) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new RuntimeException("Anime not found: " + animeId);
        }
        item.setEpisodesWatched(episodesWatched);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem updateScore(String animeId, int score) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new RuntimeException("Anime not found: " + animeId);
        }
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem updateStatus(String animeId, String status) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new RuntimeException("Anime not found: " + animeId);
        }
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        animeRepository.save(item);
        return item;
    }

    public AnimeItem refreshLatestEpisode(String animeId) {
        AnimeItem item = animeRepository.findById(animeId);
        if (item == null) {
            throw new RuntimeException("Anime not found: " + animeId);
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
}
