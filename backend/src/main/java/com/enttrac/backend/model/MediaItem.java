package com.enttrac.backend.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public abstract class MediaItem {

    @Getter(AccessLevel.NONE)
    private String pk;

    @Getter(AccessLevel.NONE)
    private String sk;

    @NotBlank(message = "Title is required")
    private String title;

    @Pattern(
            regexp = "CONSUMING|PLANNED|FINISHED|DROPPED",
            message = "Status must be CONSUMING, PLANNED, FINISHED, or DROPPED"
    )
    private String status;

    private String coverUrl;
    private String description;
    private String seriesStatus;

    @Min(value = 1, message = "Score must be between 1 and 10")
    @Max(value = 10, message = "Score must be between 1 and 10")
    private Integer score;

    private Double communityRating;
    private String lastRefreshed;
    private String updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() { return pk; }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() { return sk; }
}