package com.enttrac.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimeSearchResult {
    private String id;
    private String title;
    private String description;
    private String coverUrl;
    private Integer totalEpisodes;
    private Integer latestEpisode;
    private String status;        // currently_airing, finished_airing, not_yet_aired
    private String studio;
    private String season;        // e.g. "Fall 2023"
    private Double communityRating;
}