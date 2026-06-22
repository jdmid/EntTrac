package com.enttrac.backend.model.item;

import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.validation.ValidStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class TvItem extends MediaItem {

    @NotBlank(message = "TV show ID is required")
    private String tvId;

    private String network;
    private String genres;
    private String firstAirYear;
    private String seriesType;
    private Integer numberOfSeasons;

    @Min(value = 0, message = "Episodes watched cannot be negative")
    private int episodesWatched;

    private Integer totalEpisodes;
    private Integer currentSeason;
    private String nextEpisodeDate;
    private Double tmdbRating;
    private String creatorName;
    private String creatorId;

    private List<Integer> seasonEpisodes;

    private List<String> watchProviders;
    private String watchProvidersRefreshedAt;

    @Override
    @ValidStatus(MediaType.TV)
    public String getStatus() {
        return super.getStatus();
    }


}