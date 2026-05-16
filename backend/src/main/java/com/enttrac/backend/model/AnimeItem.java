package com.enttrac.backend.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class AnimeItem extends MediaItem {

    @NotBlank(message = "Anime ID is required")
    private String animeId;

    private String studio;
    private String season;

    @Min(value = 0, message = "Episodes watched cannot be negative")
    private int episodesWatched;

    private Integer totalEpisodes;
    private Integer latestEpisode;
}
