package com.enttrac.backend.service;

import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.MediaItem;
import com.enttrac.backend.model.result.MediaSearchResult;
import com.enttrac.backend.repository.MediaRepository;

import java.time.Instant;
import java.util.List;

public abstract class MediaService<T extends MediaItem, R extends MediaSearchResult> {

    protected final MediaRepository<T> repository;

    protected MediaService(MediaRepository<T> repository) {
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

    public List<T> getLibrary() {
        return repository.findAll();
    }

    public void removeFromLibrary(String id) {
        repository.delete(id);
    }

    public T addToLibrary(T item) {
        T existing = repository.findById(getEntityId(item));
        if (existing != null) {
            return existing;
        }
        item.setPk("USER#default");
        item.setSk(buildSortKey(item));
        String now = Instant.now().toString();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        beforeSave(item);
        repository.save(item);
        return item;
    }

    public T updateScore(String id, int score) {
        T item = repository.findById(id);
        if (item == null) throw new NotFoundException(getNotFoundMessage(id));
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public T updateStatus(String id, String status) {
        T item = repository.findById(id);
        if (item == null) throw new NotFoundException(getNotFoundMessage(id));
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public T updateNotes(String id, String notes) {
        T item = repository.findById(id);
        if (item == null) throw new NotFoundException(getNotFoundMessage(id));
        item.setNotes(notes);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }
}
