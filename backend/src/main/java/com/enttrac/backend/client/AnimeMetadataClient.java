package com.enttrac.backend.client;

import com.enttrac.backend.model.AnimeSearchResult;
import java.util.List;

public interface AnimeMetadataClient {
    List<AnimeSearchResult> search(String query);
    AnimeSearchResult getDetails(String id);

    default Double getCommunityRating(String id) {
        return null;
    }
}