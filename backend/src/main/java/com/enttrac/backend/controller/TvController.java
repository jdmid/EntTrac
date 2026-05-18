package com.enttrac.backend.controller;

import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.service.TvService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tv")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class TvController {

    private final TvService tvService;

    public TvController(TvService tvService) {
        this.tvService = tvService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<TvSearchResult>> search(@RequestParam String q) {
        return ResponseEntity.ok(tvService.search(q));
    }

    @GetMapping("/library")
    public ResponseEntity<List<TvItem>> getLibrary() {
        return ResponseEntity.ok(tvService.getLibrary());
    }

    @GetMapping("/library/{tvId}")
    public ResponseEntity<TvItem> getTvShow(@PathVariable String tvId) {
        TvItem item = tvService.getTvShow(tvId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<TvItem> addToLibrary(@Valid @RequestBody TvItem item) {
        return ResponseEntity.ok(tvService.addToLibrary(item));
    }

    @PatchMapping("/library/{tvId}/progress")
    public ResponseEntity<TvItem> updateProgress(
            @PathVariable String tvId,
            @RequestParam int episodesWatched,
            @RequestParam int currentSeason) {
        return ResponseEntity.ok(tvService.updateProgress(tvId, episodesWatched, currentSeason));
    }

    @PatchMapping("/library/{tvId}/score")
    public ResponseEntity<TvItem> updateScore(
            @PathVariable String tvId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(tvService.updateScore(tvId, score));
    }

    @PatchMapping("/library/{tvId}/status")
    public ResponseEntity<TvItem> updateStatus(
            @PathVariable String tvId,
            @RequestParam @Pattern(regexp = "CONSUMING|PLANNED|FINISHED|DROPPED") String status) {
        return ResponseEntity.ok(tvService.updateStatus(tvId, status));
    }

    @PatchMapping("/library/{tvId}/notes")
    public ResponseEntity<TvItem> updateNotes(
            @PathVariable String tvId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(tvService.updateNotes(tvId, notes != null ? notes : ""));
    }

    @PostMapping("/library/{tvId}/refresh")
    public ResponseEntity<TvItem> refresh(@PathVariable String tvId) {
        return ResponseEntity.ok(tvService.refreshLatestEpisodes(tvId));
    }

    @GetMapping("/details/{tvId}")
    public ResponseEntity<TvSearchResult> getDetails(@PathVariable String tvId) {
        TvSearchResult result = tvService.getDetails(tvId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/library/{tvId}/rating")
    public ResponseEntity<Double> getCommunityRating(@PathVariable String tvId) {
        Double rating = tvService.getCommunityRating(tvId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/library/{tvId}")
    public ResponseEntity<Void> removeFromLibrary(@PathVariable String tvId) {
        tvService.removeFromLibrary(tvId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/library/refresh-all")
    public ResponseEntity<List<TvItem>> refreshAll() {
        return ResponseEntity.ok(tvService.refreshAll());
    }

    @PostMapping("/library/refresh-ongoing")
    public ResponseEntity<List<TvItem>> refreshOngoing() {
        return ResponseEntity.ok(tvService.refreshOngoing());
    }
}
