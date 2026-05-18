package com.enttrac.backend.controller;

import com.enttrac.backend.model.item.MangaItem;
import com.enttrac.backend.model.result.MangaSearchResult;
import com.enttrac.backend.service.MangaService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

@RestController
@RequestMapping("/api/manga")
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<List<MangaItem>> getLibrary() {
        return ResponseEntity.ok(mangaService.getLibrary());
    }

    // Get single manga from library
    @GetMapping("/library/{mangaId}")
    public ResponseEntity<MangaItem> getManga(@PathVariable String mangaId) {
        MangaItem item = mangaService.getManga(mangaId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    // Add manga to library
    @PostMapping("/library")
    public ResponseEntity<MangaItem> addToLibrary(@Valid @RequestBody MangaItem item) {
        return ResponseEntity.ok(mangaService.addToLibrary(item));
    }

    // Update reading progress
    @PatchMapping("/library/{mangaId}/progress")
    public ResponseEntity<MangaItem> updateProgress(
            @PathVariable String mangaId,
            @RequestParam int chaptersRead) {
        return ResponseEntity.ok(mangaService.updateProgress(mangaId, chaptersRead));
    }

    // Update status from user
    @PatchMapping("/library/{mangaId}/status")
    public ResponseEntity<MangaItem> updateStatus(
            @PathVariable String mangaId,
            @RequestParam @Pattern(regexp = "CONSUMING|PLANNED|FINISHED|DROPPED") String status) {
        return ResponseEntity.ok(mangaService.updateStatus(mangaId, status));
    }

    // Refresh latest chapter from API
    @PostMapping("/library/{mangaId}/refresh")
    public ResponseEntity<MangaItem> refresh(@PathVariable String mangaId) {
        return ResponseEntity.ok(mangaService.refreshLatestChapter(mangaId));
    }

    // Update score from user
    @PatchMapping("/library/{mangaId}/score")
    public ResponseEntity<MangaItem> updateScore(
            @PathVariable String mangaId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(mangaService.updateScore(mangaId, score));
    }

    // Remove from library
    @DeleteMapping("/library/{mangaId}")
    public ResponseEntity<Void> removeFromLibrary(@PathVariable String mangaId) {
        mangaService.removeFromLibrary(mangaId);
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

    // Get community ratings from API
    @GetMapping("/library/{mangaId}/rating")
    public ResponseEntity<Double> getCommunityRating(@PathVariable String mangaId) {
        Double rating = mangaService.getCommunityRating(mangaId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }

    // Update notes on manga DB entry
    @PatchMapping("/library/{mangaId}/notes")
    public ResponseEntity<MangaItem> updateNotes(
            @PathVariable String mangaId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(mangaService.updateNotes(mangaId, notes != null ? notes : ""));
    }

    @PostMapping("/library/refresh-all")
    public ResponseEntity<List<MangaItem>> refreshAll() {
        return ResponseEntity.ok(mangaService.refreshAll());
    }

    // Refresh all ongoing series from API
    @PostMapping("/library/refresh-ongoing")
    public ResponseEntity<List<MangaItem>> refreshOngoing() {
        return ResponseEntity.ok(mangaService.refreshOngoing());
    }
}