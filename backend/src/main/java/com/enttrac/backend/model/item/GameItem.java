package com.enttrac.backend.model.item;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class GameItem extends MediaItem {

    @NotBlank(message = "Game ID is required")
    private String gameId;

    private String releaseYear;
    private String genres;
    private String developer;
    private String developerId;
    private List<String> platforms;
    private String userPlatform;

    @Min(value = 0, message = "Hours played cannot be negative")
    private int hoursPlayed;

    private Double igdbRating;
    private Double igdbCriticRating;
    private List<String> ownedDlcIds;
}
