package com.enttrac.backend.service;

import com.enttrac.backend.client.MangaDexClient;
import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.MangaItem;
import com.enttrac.backend.model.result.MangaSearchResult;
import com.enttrac.backend.repository.MangaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MangaService extends MediaService<MangaItem, MangaSearchResult> {

    private final MediaMetadataClient<MangaSearchResult> mangaMetadataClient;

    public MangaService(MangaRepository mangaRepository,
                        @Qualifier("mangaDexClient") MediaMetadataClient<MangaSearchResult> mangaMetadataClient) {
        super(mangaRepository);
        this.mangaMetadataClient = mangaMetadataClient;
    }

    @Override
    protected String getEntityId(MangaItem item) { return item.getMangaId(); }

    @Override
    protected String buildSortKey(MangaItem item) { return "MANGA#MANGADEX#" + item.getMangaId(); }

    @Override
    protected String getNotFoundMessage(String id) { return "Manga not found: " + id; }

    public List<MangaSearchResult> search(String query) {
        log.info("Searching manga: {}", query);
        return mangaMetadataClient.search(query);
    }

    public MangaSearchResult getDetails(String id) {
        log.info("Fetching manga details: {}", id);
        return mangaMetadataClient.getDetails(id);
    }

    public MangaItem getManga(String userId, String mangaId) {
        return repository.findById(userId, mangaId);
    }

    public MangaItem updateProgress(String userId, String mangaId, int chaptersRead) {
        MangaItem item = repository.findById(userId, mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        item.setChaptersRead(chaptersRead);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        log.info("Updated manga progress: {} -> {} chapters read", mangaId, chaptersRead);
        return item;
    }

    public MangaItem refreshLatestChapter(String userId, String mangaId) {
        MangaItem item = repository.findById(userId, mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }

        MangaSearchResult details = mangaMetadataClient.getDetails(mangaId);
        if (details != null && details.getLatestChapter() != null) {
            item.setLatestChapter(details.getLatestChapter());
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
        }

        if (item.getMangadexRating() == null) {
            Double rating = ((MangaDexClient) mangaMetadataClient).getMangadexRating(mangaId);
            if (rating != null) item.setMangadexRating(rating);
        }

        repository.save(item);
        log.info("Refreshed manga: {}", mangaId);

        return item;
    }

    public List<MangaItem> refreshAll(String userId) {
        log.info("Refreshing all manga in library");
        List<MangaItem> library = repository.findAll(userId);
        List<MangaItem> updated = new ArrayList<>();

        for (MangaItem item : library) {
            try {
                MangaSearchResult details = mangaMetadataClient.getDetails(item.getMangaId());
                if (details != null && details.getLatestChapter() != null) {
                    item.setLastRefreshed(Instant.now().toString());
                    if (!details.getLatestChapter().equals(item.getLatestChapter())) {
                        item.setLatestChapter(details.getLatestChapter());
                        item.setUpdatedAt(Instant.now().toString());
                    }
                    repository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                // skip this item if it fails, continue with rest
                log.debug("Failed to refresh manga {} during refreshAll: {}", item.getMangaId(), e.getMessage());
                updated.add(item);
            }
        }
        log.info("Finished refreshing all manga: {} items processed", updated.size());
        return updated;
    }

    public List<MangaItem> refreshOngoing(String userId) {
        log.info("Refreshing ongoing manga in library");
        List<MangaItem> library = repository.findAll(userId);
        List<MangaItem> updated = new ArrayList<>();

        for (MangaItem item : library) {
            try {
                // Items with null or unknown seriesStatus are included intentionally —
                // refreshing them may populate their status from the API
                if ("completed".equals(item.getSeriesStatus()) ||
                        "cancelled".equals(item.getSeriesStatus())) {
                    updated.add(item);
                    continue;
                }
                MangaSearchResult details = mangaMetadataClient.getDetails(item.getMangaId());
                if (details != null && details.getLatestChapter() != null) {
                    item.setLastRefreshed(Instant.now().toString());
                    if (!details.getLatestChapter().equals(item.getLatestChapter())) {
                        item.setLatestChapter(details.getLatestChapter());
                        item.setUpdatedAt(Instant.now().toString());
                    }
                    repository.save(item);
                }
                updated.add(item);
            } catch (Exception e) {
                log.debug("Failed to refresh manga {} during refreshOngoing: {}", item.getMangaId(), e.getMessage());
                updated.add(item);
            }
        }
        log.info("Finished refreshing ongoing manga: {} items processed", updated.size());
        return updated;
    }

    public MangaItem enrichMangadexRating(String userId, String mangaId) {
        MangaItem item = repository.findById(userId, mangaId);
        if (item == null) throw new NotFoundException("Manga not found: " + mangaId);

        if (item.getMangadexRating() == null) {
            Double rating = ((MangaDexClient) mangaMetadataClient).getMangadexRating(mangaId);
            if (rating != null) {
                item.setMangadexRating(rating);
                item.setUpdatedAt(Instant.now().toString());
                repository.save(item);
                log.info("Enriched manga {} with MangaDex rating: {}", mangaId, rating);
            }
        }
        return item;
    }

    public ResponseEntity<byte[]> getCoverImage(String mangaId, String fileName) {
        return mangaMetadataClient.getCoverImage(mangaId, fileName);
    }

    public List<MangaSearchResult> getWorksByAuthor(String authorId) {
        return mangaMetadataClient.getWorksByCreator(authorId);
    }

    public List<Map<String, String>> searchAuthors(String name) {
        return mangaMetadataClient.searchCreators(name);
    }
}