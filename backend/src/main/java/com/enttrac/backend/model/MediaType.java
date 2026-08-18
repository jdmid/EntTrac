package com.enttrac.backend.model;

import java.util.Set;

public enum MediaType {

    MANGA(Set.of("CONSUMING", "PLANNED", "FINISHED", "DROPPED")),
    TV(Set.of("CONSUMING", "PLANNED", "FINISHED", "DROPPED")),
    MOVIE(Set.of("PLANNED", "FINISHED", "DROPPED")),
    BOOK(Set.of("CONSUMING", "PLANNED", "FINISHED", "DROPPED")),
    GAME(Set.of("CONSUMING", "PLANNED", "FINISHED", "DROPPED"));

    private final Set<String> allowedStatuses;

    MediaType(Set<String> allowedStatuses) {
        this.allowedStatuses = allowedStatuses;
    }

    public Set<String> getAllowedStatuses() {
        return allowedStatuses;
    }
}
