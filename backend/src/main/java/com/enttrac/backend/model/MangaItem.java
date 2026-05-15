package com.enttrac.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class MangaItem {

    @Getter(AccessLevel.NONE)
    private String pk;

    @Getter(AccessLevel.NONE)
    private String sk;

    @NotBlank(message = "Manga ID is required")
    private String mangaId;

    @NotBlank(message = "Title is required")
    private String title;

    @Pattern(
            regexp = "CONSUMING|PLANNED|FINISHED|DROPPED",
            message = "Status must be CONSUMING, PLANNED, FINISHED, or DROPPED"
    )
    private String status;

    @Min(value = 0, message = "Chapters read cannot be negative")
    private int chaptersRead;
    private Integer latestChapter;
    private Integer totalChapters;
    private String author;
    private String artist;
    private String coverUrl;

    @Min(value = 1, message = "Score must be between 1 and 10")
    @Max(value = 10, message = "Score must be between 1 and 10")
    private Integer score; // nullable — unscored until user rates it
    private String lastRefreshed;
    private String updatedAt;
    private String description;
    private String seriesStatus;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() { return pk; }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() { return sk; }
}