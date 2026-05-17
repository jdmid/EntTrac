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

    @lombok.Builder
    public AnimeSearchResult(String id, String title, String description,
                             String coverUrl, String status, Double communityRating,
                             Integer totalEpisodes, Integer latestEpisode,
                             String studio, String season) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        setCommunityRating(communityRating);
        this.totalEpisodes = totalEpisodes;
        this.latestEpisode = latestEpisode;
        this.studio = studio;
        this.season = season;
    }
}