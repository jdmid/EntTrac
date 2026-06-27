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
public class MangaItem extends MediaItem {

    @NotBlank(message = "Manga ID is required")
    private String mangaId;

    private String author;
    private String artist;

    @Min(value = 0, message = "Chapters read cannot be negative")
    private int chaptersRead;

    private Integer latestChapter;
    private Integer totalChapters;
    private Double mangadexRating;
    private Double anilistRating;
    private String authorId;
    private String artistId;

    @Override
    @ValidStatus(MediaType.MANGA)
    public String getStatus() {
        return super.getStatus();
    }
}