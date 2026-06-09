package com.enttrac.backend.model.item;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class MovieItem extends MediaItem {

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
}