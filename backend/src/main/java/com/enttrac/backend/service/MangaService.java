package com.enttrac.backend.service;

import com.enttrac.backend.client.MangaMetadataClient;
import com.enttrac.backend.model.MangaItem;
import com.enttrac.backend.model.MangaSearchResult;
import com.enttrac.backend.repository.MangaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MangaService {

    private final MangaRepository mangaRepository;
    private final MangaMetadataClient mangaMetadataClient;

    public MangaService(MangaRepository mangaRepository, MangaMetadataClient mangaMetadataClient) {
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
        item.setSk("MANGA#" + item.getMangaId());
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public MangaItem updateProgress(String mangaId, int chaptersRead) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new RuntimeException("Manga not found: " + mangaId);
        }
        item.setChaptersRead(chaptersRead);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public MangaItem refreshLatestChapter(String mangaId) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new RuntimeException("Manga not found: " + mangaId);
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
            throw new RuntimeException("Manga not found: " + mangaId);
        }
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public MangaItem updateStatus(String mangaId, String status) {
        MangaItem item = mangaRepository.findById(mangaId);
        if (item == null) {
            throw new RuntimeException("Manga not found: " + mangaId);
        }
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        mangaRepository.save(item);
        return item;
    }

    public void removeFromLibrary(String mangaId) {
        mangaRepository.delete(mangaId);
    }
}