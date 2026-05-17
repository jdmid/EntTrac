package com.enttrac.backend.model.result;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TvSearchResult extends MediaSearchResult {
    private String network;
    private String genres;
    private String firstAirYear;
    private String seriesType;
    private Integer totalEpisodes;
    private List<Integer> seasonEpisodes;
    private String nextEpisodeDate;
    private Integer numberOfSeasons;

    @lombok.Builder
    public TvSearchResult(String id, String title, String description,
                          String coverUrl, String status, Double communityRating,
                          String network, String genres, String firstAirYear,
                          String seriesType, Integer totalEpisodes,
                          List<Integer> seasonEpisodes, String nextEpisodeDate, Integer numberOfSeasons) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        setCommunityRating(communityRating);
        this.network = network;
        this.genres = genres;
        this.firstAirYear = firstAirYear;
        this.seriesType = seriesType;
        this.totalEpisodes = totalEpisodes;
        this.seasonEpisodes = seasonEpisodes;
        this.nextEpisodeDate = nextEpisodeDate;
        this.numberOfSeasons = numberOfSeasons;
    }
}