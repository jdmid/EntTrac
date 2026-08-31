package com.enttrac.backend.client;

import com.enttrac.backend.model.result.MediaSearchResult;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface MediaMetadataClient<T extends MediaSearchResult> {
    List<T> search(String query);
    T getDetails(String id);

    default List<T> getWorksByCreator(String creatorId) {
        return List.of();
    }

    default List<Map<String, String>> searchCreators(String name) {
        return List.of();
    }

    default List<T> getWorksByStudio(String studioId) {
        return List.of();
    }

    default List<Map<String, String>> searchStudios(String name) {
        return List.of();
    }

    default ResponseEntity<byte[]> getCoverImage(String id, String fileName) {
        return ResponseEntity.notFound().build();
    }
}
