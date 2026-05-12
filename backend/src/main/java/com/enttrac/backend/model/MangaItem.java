package com.enttrac.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class MangaItem {

    private String pk;                // "USER#default"
    private String sk;                // "MANGA#<mangadex-id>"
    private String mangaId;           // MangaDex ID
    private String title;
    private String status;            // READING, COMPLETED, PLAN_TO_READ, DROPPED
    private int chaptersRead;
    private Integer latestChapter;    // refreshed from MangaDex
    private Integer totalChapters;    // null if ongoing
    private String coverUrl;
    private String lastRefreshed;     // ISO timestamp
    private String updatedAt;         // ISO timestamp

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }
}