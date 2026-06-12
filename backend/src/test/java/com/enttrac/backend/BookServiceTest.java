package com.enttrac.backend;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.model.item.BookItem;
import com.enttrac.backend.model.result.BookSearchResult;
import com.enttrac.backend.repository.BookRepository;
import com.enttrac.backend.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MediaMetadataClient<BookSearchResult> bookMetadataClient;

    @InjectMocks
    private BookService bookService;

    private BookItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new BookItem();
        testItem.setBookId("OL27448W");
        testItem.setTitle("The Lord of the Rings");
        testItem.setStatus("CONSUMING");
        testItem.setAuthors(List.of(Map.of("id", "OL26320A", "name", "J.R.R. Tolkien")));
        testItem.setCurrentChapter(5);
        testItem.setCurrentPage(120);
    }

    @Test
    void search_ShouldDelegateToClient() {
        BookSearchResult result = BookSearchResult.builder()
                .id("OL27448W").title("The Lord of the Rings").build();
        when(bookMetadataClient.search("tolkien")).thenReturn(List.of(result));

        List<BookSearchResult> results = bookService.search("tolkien");

        assertEquals(1, results.size());
        verify(bookMetadataClient, times(1)).search("tolkien");
    }

    @Test
    void getDetails_ShouldDelegateToClient() {
        BookSearchResult result = BookSearchResult.builder()
                .id("OL27448W").description("A fantasy epic.").build();
        when(bookMetadataClient.getDetails("OL27448W")).thenReturn(result);

        BookSearchResult details = bookService.getDetails("OL27448W");

        assertEquals("A fantasy epic.", details.getDescription());
        verify(bookMetadataClient, times(1)).getDetails("OL27448W");
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(bookRepository.findAll()).thenReturn(List.of(testItem));

        List<BookItem> result = bookService.getLibrary();

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getBook_ShouldReturnItemWhenFound() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.getBook("OL27448W");

        assertEquals("The Lord of the Rings", result.getTitle());
        verify(bookRepository, times(1)).findById("OL27448W");
    }

    @Test
    void getBook_ShouldReturnNullWhenNotFound() {
        when(bookRepository.findById("notreal")).thenReturn(null);

        BookItem result = bookService.getBook("notreal");

        assertNull(result);
    }

    @Test
    void addToLibrary_ShouldSetPkAndSk() {
        when(bookRepository.findById("OL27448W")).thenReturn(null);

        BookItem result = bookService.addToLibrary(testItem);

        verify(bookRepository, times(1)).save(testItem);
        assertEquals("USER#default", result.getPk());
        assertEquals("BOOK#OPENLIBRARY#OL27448W", result.getSk());
        assertEquals("OL26320A", result.getAuthors().get(0).get("id"));
    }

    @Test
    void addToLibrary_ShouldReturnExistingWhenAlreadyInLibrary() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.addToLibrary(testItem);

        assertEquals(testItem, result);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateProgress_ShouldUpdateBothFields() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.updateProgress("OL27448W", 10, 250);

        assertEquals(10, result.getCurrentChapter());
        assertEquals(250, result.getCurrentPage());
        verify(bookRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldOnlyUpdateChapterWhenPageIsNull() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.updateProgress("OL27448W", 10, null);

        assertEquals(10, result.getCurrentChapter());
        assertEquals(120, result.getCurrentPage());
        verify(bookRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldOnlyUpdatePageWhenChapterIsNull() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.updateProgress("OL27448W", null, 250);

        assertEquals(5, result.getCurrentChapter());
        assertEquals(250, result.getCurrentPage());
        verify(bookRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldThrowWhenNotFound() {
        when(bookRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookService.updateProgress("notreal", 10, 250));

        assertEquals("Book not found: notreal", ex.getMessage());
    }

    @Test
    void updateScore_ShouldUpdateScore() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.updateScore("OL27448W", 9);

        assertEquals(9, result.getScore());
        verify(bookRepository, times(1)).save(testItem);
    }

    @Test
    void updateScore_ShouldThrowWhenNotFound() {
        when(bookRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookService.updateScore("notreal", 9));

        assertEquals("Book not found: notreal", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.updateStatus("OL27448W", "FINISHED");

        assertEquals("FINISHED", result.getStatus());
        verify(bookRepository, times(1)).save(testItem);
    }

    @Test
    void updateStatus_ShouldThrowWhenNotFound() {
        when(bookRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookService.updateStatus("notreal", "FINISHED"));

        assertEquals("Book not found: notreal", ex.getMessage());
    }

    @Test
    void updateNotes_ShouldUpdateNotes() {
        when(bookRepository.findById("OL27448W")).thenReturn(testItem);

        BookItem result = bookService.updateNotes("OL27448W", "Great book");

        assertEquals("Great book", result.getNotes());
        verify(bookRepository, times(1)).save(testItem);
    }

    @Test
    void updateNotes_ShouldThrowWhenNotFound() {
        when(bookRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookService.updateNotes("notreal", "some notes"));

        assertEquals("Book not found: notreal", ex.getMessage());
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        bookService.removeFromLibrary("OL27448W");

        verify(bookRepository, times(1)).delete("OL27448W");
    }

    @Test
    void getWorksByAuthor_ShouldDelegateToClient() {
        BookSearchResult result = BookSearchResult.builder()
                .id("OL45804W").title("The Hobbit").build();
        when(bookMetadataClient.getWorksByCreator("OL26320A"))
                .thenReturn(List.of(result));

        List<BookSearchResult> results = bookService.getWorksByAuthor("OL26320A");

        assertEquals(1, results.size());
        assertEquals("The Hobbit", results.get(0).getTitle());
        verify(bookMetadataClient, times(1)).getWorksByCreator("OL26320A");
    }

    @Test
    void searchAuthors_ShouldDelegateToClient() {
        when(bookMetadataClient.searchCreators("tolkien"))
                .thenReturn(List.of(Map.of("id", "OL26320A", "name", "J.R.R. Tolkien")));

        List<Map<String, String>> results = bookService.searchAuthors("tolkien");

        assertEquals(1, results.size());
        assertEquals("J.R.R. Tolkien", results.get(0).get("name"));
        verify(bookMetadataClient, times(1)).searchCreators("tolkien");
    }
}