package com.enttrac.backend.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public abstract class MediaSearchResult {
    private String id;
    private String title;
    private String description;
    private String coverUrl;
    private String status;
    private Double communityRating;
}
