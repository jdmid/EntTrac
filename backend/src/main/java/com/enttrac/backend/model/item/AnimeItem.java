package com.enttrac.backend.model.item;

import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.validation.ValidStatus;
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
    private String studioId;

    @Min(value = 0, message = "Episodes watched cannot be negative")
    private int episodesWatched;

    private Integer totalEpisodes;
    private Integer latestEpisode;
    private Double anilistRating;

    private Integer nextAiringEpisode;
    private Long nextAiringAt;

    @Override
    @ValidStatus(MediaType.ANIME)
    public String getStatus() {
        return super.getStatus();
    }
}
