package com.enttrac.backend.controller;

import com.enttrac.backend.model.AnimeItem;
import com.enttrac.backend.model.AnimeSearchResult;
import com.enttrac.backend.service.AnimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<List<AnimeItem>> getLibrary() {
        return ResponseEntity.ok(animeService.getLibrary());
    }

    @GetMapping("/library/{animeId}")
    public ResponseEntity<AnimeItem> getAnime(@PathVariable String animeId) {
        AnimeItem item = animeService.getAnime(animeId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<AnimeItem> addToLibrary(@Valid @RequestBody AnimeItem item) {
        return ResponseEntity.ok(animeService.addToLibrary(item));
    }

    @PatchMapping("/library/{animeId}/progress")
    public ResponseEntity<AnimeItem> updateProgress(
            @PathVariable String animeId,
            @RequestParam int episodesWatched) {
        return ResponseEntity.ok(animeService.updateProgress(animeId, episodesWatched));
    }

    @PatchMapping("/library/{animeId}/score")
    public ResponseEntity<AnimeItem> updateScore(
            @PathVariable String animeId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(animeService.updateScore(animeId, score));
    }

    @PatchMapping("/library/{animeId}/status")
    public ResponseEntity<AnimeItem> updateStatus(
            @PathVariable String animeId,
            @RequestParam @Pattern(regexp = "CONSUMING|PLANNED|FINISHED|DROPPED") String status) {
        return ResponseEntity.ok(animeService.updateStatus(animeId, status));
    }

    @PostMapping("/library/{animeId}/refresh")
    public ResponseEntity<AnimeItem> refresh(@PathVariable String animeId) {
        return ResponseEntity.ok(animeService.refreshLatestEpisode(animeId));
    }

    @GetMapping("/details/{animeId}")
    public ResponseEntity<AnimeSearchResult> getDetails(@PathVariable String animeId) {
        AnimeSearchResult result = animeService.getDetails(animeId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/library/{animeId}/rating")
    public ResponseEntity<Double> getCommunityRating(@PathVariable String animeId) {
        Double rating = animeService.getCommunityRating(animeId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/library/{animeId}")
    public ResponseEntity<Void> removeFromLibrary(@PathVariable String animeId) {
        animeService.removeFromLibrary(animeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/library/{animeId}/notes")
    public ResponseEntity<AnimeItem> updateNotes(
            @PathVariable String animeId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(animeService.updateNotes(animeId, notes != null ? notes : ""));
    }

    @PostMapping("/library/refresh-all")
    public ResponseEntity<List<AnimeItem>> refreshAll() {
        return ResponseEntity.ok(animeService.refreshAll());
    }
}
