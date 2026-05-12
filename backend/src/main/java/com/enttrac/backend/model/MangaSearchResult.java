package com.enttrac.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MangaSearchResult {
    private String id;
    private String title;
    private String description;
    private String coverUrl;
    private Integer latestChapter;
    private String status;        // ongoing, completed, hiatus, cancelled
}
