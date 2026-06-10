package com.enttrac.backend.service;

import com.enttrac.backend.client.MangaDexClient;
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
import java.util.Map;

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
        return mangaMetadataClient.search(query);
    }

    public MangaSearchResult getDetails(String id) {
        return mangaMetadataClient.getDetails(id);
    }

    public MangaItem getManga(String mangaId) {
        return repository.findById(mangaId);
    }

    public MangaItem updateProgress(String mangaId, int chaptersRead) {
        MangaItem item = repository.findById(mangaId);
        if (item == null) {
            throw new NotFoundException("Manga not found: " + mangaId);
        }
        item.setChaptersRead(chaptersRead);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public MangaItem refreshLatestChapter(String mangaId) {
        MangaItem item = repository.findById(mangaId);
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
            Double rating = mangaMetadataClient.getCommunityRating(mangaId);
            if (rating != null) item.setMangadexRating(rating);
        }

        repository.save(item);

        return item;
    }

    public List<MangaItem> refreshAll() {
        List<MangaItem> library = repository.findAll();
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
                updated.add(item);
            }
        }
        return updated;
    }

    public List<MangaItem> refreshOngoing() {
        List<MangaItem> library = repository.findAll();
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
                updated.add(item);
            }
        }
        return updated;
    }

    public MangaItem enrichMangadexRating(String mangaId) {
        MangaItem item = repository.findById(mangaId);
        if (item == null) throw new NotFoundException("Manga not found: " + mangaId);

        if (item.getMangadexRating() == null) {
            Double rating = mangaMetadataClient.getCommunityRating(mangaId);
            if (rating != null) {
                item.setMangadexRating(rating);
                item.setUpdatedAt(Instant.now().toString());
                repository.save(item);
            }
        }
        return item;
    }

    public List<MangaSearchResult> getWorksByAuthor(String authorId) {
        return mangaMetadataClient.getWorksByCreator(authorId);
    }

    public List<Map<String, String>> searchAuthors(String name) {
        return mangaMetadataClient.searchCreators(name);
    }
}