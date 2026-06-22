package com.enttrac.backend.model.item;

import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.validation.ValidStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class MovieItem extends MediaItem {

    @NotBlank(message = "Movie ID is required")
    private String movieId;

    private String releaseYear;
    private String runtime;
    private String genres;
    private String director;
    private String directorId;

    // Scores from multiple sources
    private Double imdbRating;
    private String rottenTomatoesRating;
    private String metacriticRating;
    private Double tmdbRating;

    private List<String> watchProviders;
    private String watchProvidersRefreshedAt;

    @Override
    @ValidStatus(MediaType.MOVIE)
    public String getStatus() {
        return super.getStatus();
    }
}