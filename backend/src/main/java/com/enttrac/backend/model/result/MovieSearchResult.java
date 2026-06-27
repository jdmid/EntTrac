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
public class MovieSearchResult extends MediaSearchResult {

    private String releaseYear;
    private String runtime;
    private String genres;
    private String director;
    private String directorId;
    private Double tmdbRating;

    // Supplementary scores populated by OmdbClient
    private Double imdbRating;
    private String rottenTomatoesRating;
    private String metacriticRating;
    private String imdbId;

    @lombok.Builder
    public MovieSearchResult(String id, String title, String description,
                             String coverUrl, String status, Double tmdbRating,
                             String releaseYear, String runtime, String genres,
                             String director, String directorId, Double imdbRating,
                             String rottenTomatoesRating, String metacriticRating,
                             String imdbId) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        setTmdbRating(tmdbRating);
        this.releaseYear = releaseYear;
        this.runtime = runtime;
        this.genres = genres;
        this.director = director;
        this.directorId = directorId;
        this.imdbRating = imdbRating;
        this.rottenTomatoesRating = rottenTomatoesRating;
        this.metacriticRating = metacriticRating;
        this.imdbId = imdbId;
    }
}
