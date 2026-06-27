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
    private String authorId;
    private String artistId;
    private Double mangadexRating;
    private Double anilistRating;

    @lombok.Builder
    public MangaSearchResult(String id, String title, String description,
                             String coverUrl, String status, Double mangadexRating,
                             Integer latestChapter, String author, String artist,
                             String authorId, String artistId, Double anilistRating) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        setStatus(status);
        this.latestChapter = latestChapter;
        this.author = author;
        this.artist = artist;
        this.authorId = authorId;
        this.artistId = artistId;
        this.mangadexRating = mangadexRating;
        this.anilistRating = anilistRating;
    }
}