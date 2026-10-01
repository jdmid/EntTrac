package com.enttrac.backend.service;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.client.GoogleBooksClient;
import com.enttrac.backend.client.BookEnrichmentData;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.BookItem;
import com.enttrac.backend.model.result.BookSearchResult;
import com.enttrac.backend.repository.BookRepository;
import com.enttrac.backend.config.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookService extends MediaService<BookItem, BookSearchResult> {

    private final BookRepository bookRepository;
    private final MediaMetadataClient<BookSearchResult> bookMetadataClient;
    private final GoogleBooksClient googleBooksClient;

    public BookService(BookRepository bookRepository,
                       @Qualifier("openLibraryClient") MediaMetadataClient<BookSearchResult> bookMetadataClient,
                       GoogleBooksClient googleBooksClient) {
        super(bookRepository);
        this.bookRepository = bookRepository;
        this.bookMetadataClient = bookMetadataClient;
        this.googleBooksClient = googleBooksClient;
    }

    @Override
    protected String getEntityId(BookItem item) { return item.getBookId(); }

    @Override
    protected String buildSortKey(BookItem item) {
                String source = item.getSource() != null ? item.getSource() : "OPENLIBRARY";
                return "BOOK#" + source + "#" + item.getBookId();
    }

    @Override
    protected void beforeSave(BookItem item) {
        // Validate GOOGLEBOOKS entries are PLANNED only
        if ("GOOGLEBOOKS".equals(item.getSource()) && !"PLANNED".equals(item.getStatus())) {
            throw new ValidationException("Upcoming books can only be saved with status PLANNED");
        }

        // Set seriesStatus for upcoming entries
        if ("GOOGLEBOOKS".equals(item.getSource())) {
            item.setSeriesStatus("upcoming");
        }

        // Enrich OPENLIBRARY entries with Google Books data
        if ("OPENLIBRARY".equals(item.getSource())) {
            String authorLastName = extractAuthorLastName(item.getAuthors());
            BookEnrichmentData enrichment = googleBooksClient.enrich(item.getTitle(), authorLastName);
            if (enrichment != null) {
                item.setGoogleBooksId(enrichment.googleBooksId());
                item.setIsbn(enrichment.isbn());
                item.setPublishedDate(enrichment.publishedDate());
                item.setSeriesName(enrichment.seriesName());
                item.setSeriesPosition(enrichment.seriesPosition());
            }
        }
    }

    @Override
    protected String getNotFoundMessage(String id) { return "Book not found: " + id; }

    public List<BookSearchResult> search(String query) {
        log.info("Searching for books with query: {}", query);
        return bookMetadataClient.search(query);
    }

    public BookSearchResult getDetails(String id) {
        log.info("Fetching book details for id: {}", id);
        return bookMetadataClient.getDetails(id);
    }

    public BookItem getBook(String userId,String bookId) {
        log.info("Fetching book from library: {}", bookId);
        return repository.findById(userId, bookId);
    }

    public BookItem updateProgress(String userId, String bookId, Integer currentChapter, Integer currentPage) {
        log.info("Updating progress for book: {} chapter={} page={}", bookId, currentChapter, currentPage);
        BookItem item = repository.findById(userId, bookId);
        if (item == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        if (currentChapter != null) item.setCurrentChapter(currentChapter);
        if (currentPage != null) item.setCurrentPage(currentPage);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public List<BookSearchResult> getWorksByAuthor(String authorId) {
        log.info("Fetching works by author id: {}", authorId);
        return bookMetadataClient.getWorksByCreator(authorId);
    }

    public List<Map<String, String>> searchAuthors(String name) {
        log.info("Searching authors for: {}", name);
        return bookMetadataClient.searchCreators(name);
    }

    public BookItem resetProgress(String userId, String bookId) {
        log.info("Resetting progress for book: {}", bookId);
        BookItem item = repository.findById(userId, bookId);
        if (item == null) throw new NotFoundException("Book not found: " + bookId);
        item.setCurrentChapter(null);
        item.setCurrentPage(null);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    private String extractAuthorLastName(List<Map<String, String>> authors) {
        if (authors == null || authors.isEmpty()) return null;
        String fullName = authors.get(0).get("name");
        if (fullName == null || fullName.isBlank()) return null;
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    private boolean isPublished(String publishedDate) {
        if (publishedDate == null || publishedDate.isBlank()) return false;
        try {
            LocalDate date;
            if (publishedDate.length() == 4) {
                date = LocalDate.of(Integer.parseInt(publishedDate), 1, 1);
            } else if (publishedDate.length() == 7) {
                date = LocalDate.parse(publishedDate + "-01",
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                date = LocalDate.parse(publishedDate,
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return !date.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}