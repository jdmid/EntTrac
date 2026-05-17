package com.enttrac.backend.service;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.MangaItem;
import com.enttrac.backend.model.result.MangaSearchResult;
import com.enttrac.backend.repository.MangaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// TODO: refactor into shared MediaService when adding third medium
@Service
public class MangaService {

    private final MangaRepository mangaRepository;
    private final MediaMetadataClient<MangaSearchResult> mangaMetadataClient;

    public MangaService(MangaRepository mangaRepository,
                        @Qualifier("mangaDexClient") MediaMetadataClient<MangaSearchResult> mangaMetadataClient) {
        this.mangaRepository = mangaRepository;
        this.mangaMetadataClient = mangaMetadataClient;
    }

    public List<MangaSearchResult> search(String query) {
        return mangaMetadataClient.search(query);
    }

    public MangaSearchResult getDetails(String id) {
        return mangaMetadataClient.getDetails(id);
    }

    public List<MangaItem> getLibrary() {
        return mangaRepository.findAll();
    }

    public MangaItem getManga(String mangaId) {
        return mangaRepository.findById(mangaId);
    }

    public MangaItem addToLibrary(MangaItem item) {
        // Check if already exists
        MangaItem existing = mangaRepository.findById(item.getMangaId());
        if (existing != null) {
            return existing; // already in library, don't overwrite
        }
        item.setPk("USER#default");
        item.setSk("MANGA#MANGADEX#" + item.getMangaId());
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public MangaItem updateProgress(String mangaId, int chaptersRead) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        item.setChaptersRead(chaptersRead);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public MangaItem refreshLatestChapter(String mangaId) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        MangaSearchResult details = mangaMetadataClient.getDetails(mangaId);
        if (details != null && details.getLatestChapter() != null) {
            item.setLatestChapter(details.getLatestChapter());
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
            mangaRepository.save(item);
        }
        return item;
    }

    public MangaItem updateScore(String mangaId, int score) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public MangaItem updateStatus(String mangaId, String status) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public Double getCommunityRating(String mangaId) {
        return mangaMetadataClient.getCommunityRating(mangaId);
    }

    public MangaItem updateNotes(String mangaId, String notes) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        item.setNotes(notes);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }
    public List<MangaItem> refreshAll() {
        List<MangaItem> library = mangaRepository.findAll();
        List<MangaItem> updated = new ArrayList<>();

        for (MangaItem item : library) {
            try {
                MangaSearchResult details = mangaMetadataClient.getDetails(item.getMangaId());
                if (details != null && details.getLatestChapter() != null) {
                    item.setLatestChapter(details.getLatestChapter());
                    item.setLastRefreshed(Instant.now().toString());
                    item.setUpdatedAt(Instant.now().toString());
                    mangaRepository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                // skip this item if it fails, continue with rest
                updated.add(item);
            }
        }
        return updated;
    }

    public void removeFromLibrary(String mangaId) {
        mangaRepository.delete(mangaId);
    }
}