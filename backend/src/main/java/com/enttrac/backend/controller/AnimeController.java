package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.model.item.AnimeItem;
import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.model.result.AnimeSearchResult;
import com.enttrac.backend.service.AnimeService;
import com.enttrac.backend.validation.ValidStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anime")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Validated
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<AnimeSearchResult>> search(@RequestParam String q) {
        return ResponseEntity.ok(animeService.search(q));
    }

    @GetMapping("/library")
    public ResponseEntity<List<AnimeItem>> getLibrary(@CurrentUserId String userId) {
        return ResponseEntity.ok(animeService.getLibrary(userId));
    }

    @GetMapping("/library/{animeId}")
    public ResponseEntity<AnimeItem> getAnime(@CurrentUserId String userId, @PathVariable String animeId) {
        AnimeItem item = animeService.getAnime(userId, animeId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<AnimeItem> addToLibrary(@CurrentUserId String userId, @Valid @RequestBody AnimeItem item) {
        return ResponseEntity.ok(animeService.addToLibrary(userId, item));
    }

    @PatchMapping("/library/{animeId}/progress")
    public ResponseEntity<AnimeItem> updateProgress(
            @CurrentUserId String userId,
            @PathVariable String animeId,
            @RequestParam int episodesWatched) {
        return ResponseEntity.ok(animeService.updateProgress(userId, animeId, episodesWatched));
    }

    @PatchMapping("/library/{animeId}/score")
    public ResponseEntity<AnimeItem> updateScore(
            @CurrentUserId String userId,
            @PathVariable String animeId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(animeService.updateScore(userId, animeId, score));
    }

    @PatchMapping("/library/{animeId}/status")
    public ResponseEntity<AnimeItem> updateStatus(
            @CurrentUserId String userId,
            @PathVariable String animeId,
            @RequestParam @ValidStatus(MediaType.ANIME) String status) {
        return ResponseEntity.ok(animeService.updateStatus(userId, animeId, status));
    }

    @PostMapping("/library/{animeId}/refresh")
    public ResponseEntity<AnimeItem> refresh(@CurrentUserId String userId, @PathVariable String animeId) {
        return ResponseEntity.ok(animeService.refreshLatestEpisode(userId, animeId));
    }

    @GetMapping("/details/{animeId}")
    public ResponseEntity<AnimeSearchResult> getDetails(@PathVariable String animeId) {
        AnimeSearchResult result = animeService.getDetails(animeId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/library/{animeId}")
    public ResponseEntity<Void> removeFromLibrary(@CurrentUserId String userId, @PathVariable String animeId) {
        animeService.removeFromLibrary(userId, animeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/library/{animeId}/notes")
    public ResponseEntity<AnimeItem> updateNotes(
            @CurrentUserId String userId,
            @PathVariable String animeId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(animeService.updateNotes(userId, animeId, notes != null ? notes : ""));
    }

    @PostMapping("/library/refresh-all")
    public ResponseEntity<List<AnimeItem>> refreshAll(@CurrentUserId String userId) {
        return ResponseEntity.ok(animeService.refreshAll(userId));
    }

    @PostMapping("/library/refresh-ongoing")
    public ResponseEntity<List<AnimeItem>> refreshOngoing(@CurrentUserId String userId) {
        return ResponseEntity.ok(animeService.refreshOngoing(userId));
    }

    @PostMapping("/library/{id}/enrich")
    public ResponseEntity<AnimeItem> enrich(@CurrentUserId String userId, @PathVariable String id) {
        return ResponseEntity.ok(animeService.enrichAniListRating(userId, id));
    }

    @GetMapping("/creator/{studioId}")
    public ResponseEntity<Map<String, Object>> getWorksByStudio(
            @PathVariable String studioId,
            @RequestParam(required = false) String name) {
        List<AnimeSearchResult> results = animeService.getWorksByStudio(studioId);
        return ResponseEntity.ok(Map.of(
                "items", results,
                "hasNextPage", false
        ));
    }

    @GetMapping("/producer-search")
    public ResponseEntity<List<Map<String, String>>> searchProducers(@RequestParam String name) {
        return ResponseEntity.ok(animeService.searchProducers(name));
    }
}
