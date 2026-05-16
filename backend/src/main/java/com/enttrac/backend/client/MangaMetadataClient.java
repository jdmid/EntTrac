package com.enttrac.backend.client;

import com.enttrac.backend.model.MangaSearchResult;

import java.util.List;

public interface MangaMetadataClient {
    List<MangaSearchResult> search(String query);
    MangaSearchResult getDetails(String id);

    // Optional — clients that don't have ratings just skip this
    default Double getCommunityRating(String id) {
        return null;
    }
}
