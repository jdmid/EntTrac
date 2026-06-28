package com.enttrac.backend.model.result;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AnimeSearchResult extends MediaSearchResult {
    private Integer totalEpisodes;
    private Integer latestEpisode;
    private String studio;
    private String season;
    private String studioId;
    private Double anilistRating;
    private Integer nextAiringEpisode;
    private Long nextAiringAt;


    @lombok.Builder
    public AnimeSearchResult(String id, String title, String description,
                             String coverUrl, String status, Double anilistRating,
                             Integer totalEpisodes, Integer latestEpisode,
                             String studio, String season, String studioId,
                             Integer nextAiringEpisode, Long nextAiringAt) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        this.totalEpisodes = totalEpisodes;
        this.latestEpisode = latestEpisode;
        this.studio = studio;
        this.season = season;
        this.studioId = studioId;
        this.anilistRating = anilistRating;
        this.nextAiringAt = nextAiringAt;
        this.nextAiringEpisode = nextAiringEpisode;
    }
}