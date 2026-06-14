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
public class GameSearchResult extends MediaSearchResult {

    private String releaseYear;
    private String genres;
    private String developer;
    private String developerId;
    private List<String> platforms;
    private Double igdbRating;
    private Double igdbCriticRating;
    private String category;
    private List<GameSearchResult> dlc;

    @lombok.Builder
    public GameSearchResult(String id, String title, String description,
                            String coverUrl, String status, Double communityRating,
                            String releaseYear, String genres,
                            String developer, String developerId,
                            List<String> platforms,
                            Double igdbRating, Double igdbCriticRating,
                            String category, List<GameSearchResult> dlc) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        setCommunityRating(communityRating);
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.developer = developer;
        this.developerId = developerId;
        this.platforms = platforms;
        this.igdbRating = igdbRating;
        this.igdbCriticRating = igdbCriticRating;
        this.category = category;
        this.dlc = dlc;
    }
}
