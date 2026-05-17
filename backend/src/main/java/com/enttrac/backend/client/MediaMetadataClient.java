package com.enttrac.backend.client;

import com.enttrac.backend.model.MediaSearchResult;
import java.util.List;

public interface MediaMetadataClient<T extends MediaSearchResult> {
    List<T> search(String query);
    T getDetails(String id);

    default Double getCommunityRating(String id) {
        return null;
    }
}
