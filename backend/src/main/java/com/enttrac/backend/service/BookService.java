package com.enttrac.backend.service;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.BookItem;
import com.enttrac.backend.model.result.BookSearchResult;
import com.enttrac.backend.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookService extends MediaService<BookItem, BookSearchResult> {

    private final MediaMetadataClient<BookSearchResult> bookMetadataClient;

    public BookService(BookRepository bookRepository,
                       @Qualifier("openLibraryClient") MediaMetadataClient<BookSearchResult> bookMetadataClient) {
        super(bookRepository);
        this.bookMetadataClient = bookMetadataClient;
    }

    @Override
    protected String getEntityId(BookItem item) { return item.getBookId(); }

    @Override
    protected String buildSortKey(BookItem item) { return "BOOK#OPENLIBRARY#" + item.getBookId(); }

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
}