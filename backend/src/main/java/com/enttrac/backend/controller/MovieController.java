package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.model.item.MovieItem;
import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.model.result.MovieSearchResult;
import com.enttrac.backend.service.MovieService;
import com.enttrac.backend.validation.ValidStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@Validated
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResult>> search(@RequestParam String q) {
        return ResponseEntity.ok(movieService.search(q));
    }

    @GetMapping("/library")
    public ResponseEntity<List<MovieItem>> getLibrary(@CurrentUserId String userId) {
        return ResponseEntity.ok(movieService.getLibrary(userId));
    }

    @GetMapping("/library/{movieId}")
    public ResponseEntity<MovieItem> getMovie(@CurrentUserId String userId, @PathVariable String movieId) {
        MovieItem item = movieService.getMovie(userId, movieId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<MovieItem> addToLibrary(@CurrentUserId String userId, @Valid @RequestBody MovieItem item) {
        return ResponseEntity.ok(movieService.addToLibrary(userId, item));
    }

    @PatchMapping("/library/{movieId}/score")
    public ResponseEntity<MovieItem> updateScore(
            @CurrentUserId String userId,
            @PathVariable String movieId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(movieService.updateScore(userId, movieId, score));
    }

    @PatchMapping("/library/{movieId}/status")
    public ResponseEntity<MovieItem> updateStatus(
            @CurrentUserId String userId,
            @PathVariable String movieId,
            @RequestParam @ValidStatus(MediaType.MOVIE) String status) {
        return ResponseEntity.ok(movieService.updateStatus(userId, movieId, status));
    }

    @PatchMapping("/library/{movieId}/notes")
    public ResponseEntity<MovieItem> updateNotes(
            @CurrentUserId String userId,
            @PathVariable String movieId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(movieService.updateNotes(userId, movieId, notes != null ? notes : ""));
    }

    @PostMapping("/library/{movieId}/refresh")
    public ResponseEntity<MovieItem> refresh(@CurrentUserId String userId, @PathVariable String movieId) {
        return ResponseEntity.ok(movieService.refreshRatings(userId, movieId));
    }

    @PostMapping("/library/{movieId}/enrich")
    public ResponseEntity<MovieItem> enrich(@CurrentUserId String userId, @PathVariable String movieId) {
        return ResponseEntity.ok(movieService.enrichFromCache(userId, movieId));
    }

    @PostMapping("/library/{movieId}/watch-providers")
    public ResponseEntity<MovieItem> enrichWatchProviders(
            @CurrentUserId String userId,
            @PathVariable String movieId,
            @RequestParam(defaultValue = "US") String region) {
        return ResponseEntity.ok(movieService.enrichWatchProviders(userId, movieId, region));
    }

    @GetMapping("/details/{movieId}")
    public ResponseEntity<MovieSearchResult> getDetails(@PathVariable String movieId) {
        MovieSearchResult result = movieService.getDetails(movieId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/library/{movieId}")
    public ResponseEntity<Void> removeFromLibrary(@CurrentUserId String userId, @PathVariable String movieId) {
        movieService.removeFromLibrary(userId, movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/creator/{personId}")
    public ResponseEntity<List<MovieSearchResult>> getWorksByPerson(
            @PathVariable String personId) {
        return ResponseEntity.ok(movieService.getWorksByPerson(personId));
    }

    @GetMapping("/person-search")
    public ResponseEntity<List<Map<String, String>>> searchPeople(@RequestParam String name) {
        return ResponseEntity.ok(movieService.searchPeople(name));
    }
}
