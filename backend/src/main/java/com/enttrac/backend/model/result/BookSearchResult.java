package com.enttrac.backend.model.result;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BookSearchResult extends MediaSearchResult {

    private List<Map<String, String>> authors;
    private String firstPublishYear;
    private String genres;

    @lombok.Builder
    public BookSearchResult(String id, String title, String description,
                            String coverUrl, Double communityRating,
                            List<Map<String, String>> authors,
                            String firstPublishYear, String genres) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setCommunityRating(communityRating);
        this.authors = authors;
        this.firstPublishYear = firstPublishYear;
        this.genres = genres;
    }
}
