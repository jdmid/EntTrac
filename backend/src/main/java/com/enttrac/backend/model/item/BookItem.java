package com.enttrac.backend.model.item;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class BookItem extends MediaItem {

    @NotBlank(message = "Book ID is required")
    private String bookId;

    private List<Map<String, String>> authors;
    private String firstPublishYear;
    private String genres;
    private Integer currentChapter;
    private Integer currentPage;
    private Double bookRating;
}
