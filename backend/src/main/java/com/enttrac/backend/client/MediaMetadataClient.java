package com.enttrac.backend.client;

import com.enttrac.backend.model.result.MediaSearchResult;
import java.util.List;
import java.util.Map;

public interface MediaMetadataClient<T extends MediaSearchResult> {
    List<T> search(String query);
    T getDetails(String id);

    default Double getCommunityRating(String id) {
        return null;
    }

    default List<T> getWorksByCreator(String creatorId) {
        return List.of();
    }

    default List<Map<String, String>> searchCreators(String name) {
        return List.of();
    }
}
