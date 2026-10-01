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
    private String source;
    private String publishedDate;
    private String isbn;
    private String seriesName;
    private String seriesPosition;

    @lombok.Builder
    public BookSearchResult(String id, String title, String description,
                            String coverUrl, List<Map<String, String>> authors,
                            String firstPublishYear, String genres, String source, String publishedDate, String isbn,
                            String seriesName, String seriesPosition) {
        super();
        setId(id);
        setTitle(title);
        setDescription(description);
        setCoverUrl(coverUrl);
        this.authors = authors;
        this.firstPublishYear = firstPublishYear;
        this.genres = genres;
        this.source = source;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.seriesName = seriesName;
        this.seriesPosition = seriesPosition;
    }
}
