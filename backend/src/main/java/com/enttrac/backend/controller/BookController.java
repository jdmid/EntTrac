package com.enttrac.backend.controller;

import com.enttrac.backend.model.item.BookItem;
import com.enttrac.backend.model.result.BookSearchResult;
import com.enttrac.backend.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookSearchResult>> search(@RequestParam String q) {
        return ResponseEntity.ok(bookService.search(q));
    }

    @GetMapping("/library")
    public ResponseEntity<List<BookItem>> getLibrary() {
        return ResponseEntity.ok(bookService.getLibrary());
    }

    @GetMapping("/library/{bookId}")
    public ResponseEntity<BookItem> getBook(@PathVariable String bookId) {
        BookItem item = bookService.getBook(bookId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<BookItem> addToLibrary(@Valid @RequestBody BookItem item) {
        return ResponseEntity.ok(bookService.addToLibrary(item));
    }

    @PatchMapping("/library/{bookId}/progress")
    public ResponseEntity<BookItem> updateProgress(
            @PathVariable String bookId,
            @RequestParam(required = false) Integer currentChapter,
            @RequestParam(required = false) Integer currentPage) {
        return ResponseEntity.ok(bookService.updateProgress(bookId, currentChapter, currentPage));
    }

    @PatchMapping("/library/{bookId}/score")
    public ResponseEntity<BookItem> updateScore(
            @PathVariable String bookId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(bookService.updateScore(bookId, score));
    }

    @PatchMapping("/library/{bookId}/status")
    public ResponseEntity<BookItem> updateStatus(
            @PathVariable String bookId,
            @RequestParam @Pattern(regexp = "CONSUMING|PLANNED|FINISHED|DROPPED") String status) {
        return ResponseEntity.ok(bookService.updateStatus(bookId, status));
    }

    @PatchMapping("/library/{bookId}/notes")
    public ResponseEntity<BookItem> updateNotes(
            @PathVariable String bookId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(bookService.updateNotes(bookId, notes != null ? notes : ""));
    }

    @DeleteMapping("/library/{bookId}")
    public ResponseEntity<Void> removeFromLibrary(@PathVariable String bookId) {
        bookService.removeFromLibrary(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/details/{bookId}")
    public ResponseEntity<BookSearchResult> getDetails(@PathVariable String bookId) {
        BookSearchResult result = bookService.getDetails(bookId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/creator/{authorId}")
    public ResponseEntity<List<BookSearchResult>> getWorksByAuthor(
            @PathVariable String authorId) {
        return ResponseEntity.ok(bookService.getWorksByAuthor(authorId));
    }

    @GetMapping("/author-search")
    public ResponseEntity<List<Map<String, String>>> searchAuthors(@RequestParam String name) {
        return ResponseEntity.ok(bookService.searchAuthors(name));
    }

    @DeleteMapping("/library/{bookId}/progress")
    public ResponseEntity<BookItem> resetProgress(@PathVariable String bookId) {
        return ResponseEntity.ok(bookService.resetProgress(bookId));
    }
}
