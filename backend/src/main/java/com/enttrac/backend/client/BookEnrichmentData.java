package com.enttrac.backend.client;

public record BookEnrichmentData(
        String googleBooksId,
        String isbn,
        String publishedDate,
        String seriesName,
        String seriesPosition
) {}