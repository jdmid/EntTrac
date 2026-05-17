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
public class MangaSearchResult extends MediaSearchResult {
    private Integer latestChapter;
    private String author;
    private String artist;

    @lombok.Builder
    public MangaSearchResult(String id, String title, String description,
                             String coverUrl, String status, Double communityRating,
                             Integer latestChapter, String author, String artist) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        setCommunityRating(communityRating);
        this.latestChapter = latestChapter;
        this.author = author;
        this.artist = artist;
    }
}