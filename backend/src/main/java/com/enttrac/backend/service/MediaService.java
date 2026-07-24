package com.enttrac.backend.service;

import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.MediaItem;
import com.enttrac.backend.model.result.MediaSearchResult;
import com.enttrac.backend.repository.BaseMediaRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
public abstract class MediaService<T extends MediaItem, R extends MediaSearchResult> {

    protected final BaseMediaRepository<T> repository;

    protected MediaService(BaseMediaRepository<T> repository) {
        this.repository = repository;
    }

    // --- Abstract methods subclasses must implement ---

    protected abstract String getEntityId(T item);
    protected abstract String buildSortKey(T item);
    protected abstract String getNotFoundMessage(String id);
    protected void beforeSave(T item) {
        // default no-op — override in subclasses for pre-save logic
    }

    // --- Shared implementations ---

    private String typeLabel(T item) {
        return item.getClass().getSimpleName().replace("Item", "");
    }

    public List<T> getLibrary(String userId) {
        return repository.findAll(userId);
    }

    public void removeFromLibrary(String userId, String id) {
        log.info("Removing from library: {}", id);
        repository.delete(userId, id);
    }

    public T addToLibrary(String userId, T item) {
        T existing = repository.findById(userId, getEntityId(item));
        if (existing != null) {
            log.info("{} already in library, skipping add: {}", typeLabel(item), getEntityId(item));
            return existing;
        }
        item.setPk(userId);
        item.setSk(buildSortKey(item));
        String now = Instant.now().toString();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        beforeSave(item);
        repository.save(item);
        log.info("Added {} to library: {}", typeLabel(item), getEntityId(item));
        return item;
    }

    public T updateScore(String userId, String id, int score) {
        T item = repository.findById(userId, id);
        if (item == null) throw new NotFoundException(getNotFoundMessage(id));
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        log.info("Updated {} score: {} -> {}", typeLabel(item), id, score);
        return item;
    }

    public T updateStatus(String userId, String id, String status) {
        T item = repository.findById(userId, id);
        if (item == null) throw new NotFoundException(getNotFoundMessage(id));
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        log.info("Updated {} status: {} -> {}", typeLabel(item), id, status);
        return item;
    }

    public T updateNotes(String userId, String id, String notes) {
        T item = repository.findById(userId, id);
        if (item == null) throw new NotFoundException(getNotFoundMessage(id));
        item.setNotes(notes);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        log.info("Updated {} notes: {}", typeLabel(item), id);
        return item;
    }
}
