package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.validation.ValidStatus;
import com.enttrac.backend.service.TvService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tv")
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
    public ResponseEntity<List<TvItem>> getLibrary(@CurrentUserId String userId) {
        return ResponseEntity.ok(tvService.getLibrary(userId));
    }

    @GetMapping("/library/{tvId}")
    public ResponseEntity<TvItem> getTvShow(@CurrentUserId String userId, @PathVariable String tvId) {
        TvItem item = tvService.getTvShow(userId, tvId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<TvItem> addToLibrary(@CurrentUserId String userId, @Valid @RequestBody TvItem item) {
        return ResponseEntity.ok(tvService.addToLibrary(userId, item));
    }

    @PatchMapping("/library/{tvId}/progress")
    public ResponseEntity<TvItem> updateProgress(
            @CurrentUserId String userId,
            @PathVariable String tvId,
            @RequestParam int episodesWatched,
            @RequestParam int currentSeason) {
        return ResponseEntity.ok(tvService.updateProgress(userId, tvId, episodesWatched, currentSeason));
    }

    @PatchMapping("/library/{tvId}/score")
    public ResponseEntity<TvItem> updateScore(
            @CurrentUserId String userId,
            @PathVariable String tvId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(tvService.updateScore(userId, tvId, score));
    }

    @PatchMapping("/library/{tvId}/status")
    public ResponseEntity<TvItem> updateStatus(
            @CurrentUserId String userId,
            @PathVariable String tvId,
            @RequestParam @ValidStatus(MediaType.TV) String status) {
        return ResponseEntity.ok(tvService.updateStatus(userId, tvId, status));
    }

    @PatchMapping("/library/{tvId}/notes")
    public ResponseEntity<TvItem> updateNotes(
            @CurrentUserId String userId,
            @PathVariable String tvId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(tvService.updateNotes(userId, tvId, notes != null ? notes : ""));
    }

    @PostMapping("/library/{tvId}/refresh")
    public ResponseEntity<TvItem> refresh(@CurrentUserId String userId, @PathVariable String tvId) {
        return ResponseEntity.ok(tvService.refreshLatestEpisodes(userId, tvId));
    }

    @GetMapping("/details/{tvId}")
    public ResponseEntity<TvSearchResult> getDetails(@PathVariable String tvId) {
        TvSearchResult result = tvService.getDetails(tvId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/library/{tvId}")
    public ResponseEntity<Void> removeFromLibrary(@CurrentUserId String userId, @PathVariable String tvId) {
        tvService.removeFromLibrary(userId, tvId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/library/refresh-all")
    public ResponseEntity<List<TvItem>> refreshAll(@CurrentUserId String userId) {
        return ResponseEntity.ok(tvService.refreshAll(userId));
    }

    @PostMapping("/library/refresh-ongoing")
    public ResponseEntity<List<TvItem>> refreshOngoing(@CurrentUserId String userId) {
        return ResponseEntity.ok(tvService.refreshOngoing(userId));
    }

    @PostMapping("/library/{id}/enrich")
    public ResponseEntity<TvItem> enrich(@CurrentUserId String userId, @PathVariable String id) {
        return ResponseEntity.ok(tvService.enrichTmdbRating(userId, id));
    }

    @PostMapping("/library/{id}/watch-providers")
    public ResponseEntity<TvItem> enrichWatchProviders(
            @CurrentUserId String userId,
            @PathVariable String id,
            @RequestParam(defaultValue = "US") String region) {
        return ResponseEntity.ok(tvService.enrichWatchProviders(userId, id, region));
    }

    @GetMapping("/creator/{personId}")
    public ResponseEntity<List<TvSearchResult>> getWorksByCreator(
            @PathVariable String personId) {
        return ResponseEntity.ok(tvService.getWorksByCreator(personId));
    }

    @GetMapping("/person-search")
    public ResponseEntity<List<Map<String, String>>> searchPeople(@RequestParam String name) {
        return ResponseEntity.ok(tvService.searchPeople(name));
    }
}
