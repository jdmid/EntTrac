package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.model.item.MangaItem;
import com.enttrac.backend.model.result.MangaSearchResult;
import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.service.MangaService;
import com.enttrac.backend.validation.ValidStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manga")
@Validated
public class MangaController {

    private final MangaService mangaService;

    public MangaController(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    // Search MangaDex
    @GetMapping("/search")
    public ResponseEntity<List<MangaSearchResult>> search(@RequestParam String q) {
        return ResponseEntity.ok(mangaService.search(q));
    }

    // Get full library
    @GetMapping("/library")
    public ResponseEntity<List<MangaItem>> getLibrary(@CurrentUserId String userId) {
        return ResponseEntity.ok(mangaService.getLibrary(userId));
    }

    // Get single manga from library
    @GetMapping("/library/{mangaId}")
    public ResponseEntity<MangaItem> getManga(@CurrentUserId String userId, @PathVariable String mangaId) {
        MangaItem item = mangaService.getManga(userId, mangaId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    // Add manga to library
    @PostMapping("/library")
    public ResponseEntity<MangaItem> addToLibrary(@CurrentUserId String userId, @Valid @RequestBody MangaItem item) {
        return ResponseEntity.ok(mangaService.addToLibrary(userId, item));
    }

    // Update reading progress
    @PatchMapping("/library/{mangaId}/progress")
    public ResponseEntity<MangaItem> updateProgress(
            @CurrentUserId String userId,
            @PathVariable String mangaId,
            @RequestParam int chaptersRead) {
        return ResponseEntity.ok(mangaService.updateProgress(userId, mangaId, chaptersRead));
    }

    // Update status from user
    @PatchMapping("/library/{mangaId}/status")
    public ResponseEntity<MangaItem> updateStatus(
            @CurrentUserId String userId,
            @PathVariable String mangaId,
            @RequestParam @ValidStatus(MediaType.MANGA) String status) {
        return ResponseEntity.ok(mangaService.updateStatus(userId, mangaId, status));
    }

    // Refresh latest chapter from API
    @PostMapping("/library/{mangaId}/refresh")
    public ResponseEntity<MangaItem> refresh(@CurrentUserId String userId, @PathVariable String mangaId) {
        return ResponseEntity.ok(mangaService.refreshLatestChapter(userId, mangaId));
    }

    // Update score from user
    @PatchMapping("/library/{mangaId}/score")
    public ResponseEntity<MangaItem> updateScore(
            @CurrentUserId String userId,
            @PathVariable String mangaId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(mangaService.updateScore(userId, mangaId, score));
    }

    // Remove from library
    @DeleteMapping("/library/{mangaId}")
    public ResponseEntity<Void> removeFromLibrary(@CurrentUserId String userId, @PathVariable String mangaId) {
        mangaService.removeFromLibrary(userId, mangaId);
        return ResponseEntity.noContent().build();
    }

    // Get manga details from API
    @GetMapping("/details/{mangaId}")
    public ResponseEntity<MangaSearchResult> getDetails(@PathVariable String mangaId) {
        MangaSearchResult result = mangaService.getDetails(mangaId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    // Update notes on manga DB entry
    @PatchMapping("/library/{mangaId}/notes")
    public ResponseEntity<MangaItem> updateNotes(
            @CurrentUserId String userId,
            @PathVariable String mangaId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(mangaService.updateNotes(userId, mangaId, notes != null ? notes : ""));
    }

    @PostMapping("/library/refresh-all")
    public ResponseEntity<List<MangaItem>> refreshAll(@CurrentUserId String userId) {
        return ResponseEntity.ok(mangaService.refreshAll(userId));
    }

    // Refresh all ongoing series from API
    @PostMapping("/library/refresh-ongoing")
    public ResponseEntity<List<MangaItem>> refreshOngoing(@CurrentUserId String userId) {
        return ResponseEntity.ok(mangaService.refreshOngoing(userId));
    }

    @PostMapping("/library/{id}/enrich")
    public ResponseEntity<MangaItem> enrich(@CurrentUserId String userId, @PathVariable String id) {
        return ResponseEntity.ok(mangaService.enrichMangadexRating(userId, id));
    }

    @GetMapping("/creator/{authorId}")
    public ResponseEntity<List<MangaSearchResult>> getWorksByAuthor(
            @PathVariable String authorId) {
        List<MangaSearchResult> results = mangaService.getWorksByAuthor(authorId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/author-search")
    public ResponseEntity<List<Map<String, String>>> searchAuthors(@RequestParam String name) {
        return ResponseEntity.ok(mangaService.searchAuthors(name));
    }
}