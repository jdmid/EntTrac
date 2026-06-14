package com.enttrac.backend.controller;

import com.enttrac.backend.model.item.GameItem;
import com.enttrac.backend.model.result.GameSearchResult;
import com.enttrac.backend.service.GameService;
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
@RequestMapping("/api/games")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<GameSearchResult>> search(@RequestParam String q) {
        return ResponseEntity.ok(gameService.search(q));
    }

    @GetMapping("/library")
    public ResponseEntity<List<GameItem>> getLibrary() {
        return ResponseEntity.ok(gameService.getLibrary());
    }

    @GetMapping("/library/{gameId}")
    public ResponseEntity<GameItem> getGame(@PathVariable String gameId) {
        GameItem item = gameService.getGame(gameId);
        if (item == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<GameItem> addToLibrary(@Valid @RequestBody GameItem item) {
        return ResponseEntity.ok(gameService.addToLibrary(item));
    }

    @DeleteMapping("/library/{gameId}")
    public ResponseEntity<Void> removeFromLibrary(@PathVariable String gameId) {
        gameService.removeFromLibrary(gameId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/library/{gameId}/progress")
    public ResponseEntity<GameItem> updateProgress(
            @PathVariable String gameId,
            @RequestParam int hoursPlayed) {
        return ResponseEntity.ok(gameService.updateProgress(gameId, hoursPlayed));
    }

    @PatchMapping("/library/{gameId}/score")
    public ResponseEntity<GameItem> updateScore(
            @PathVariable String gameId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(gameService.updateScore(gameId, score));
    }

    @PatchMapping("/library/{gameId}/status")
    public ResponseEntity<GameItem> updateStatus(
            @PathVariable String gameId,
            @RequestParam @Pattern(regexp = "CONSUMING|PLANNED|FINISHED|DROPPED") String status) {
        return ResponseEntity.ok(gameService.updateStatus(gameId, status));
    }

    @PatchMapping("/library/{gameId}/platform")
    public ResponseEntity<GameItem> updateUserPlatform(
            @PathVariable String gameId,
            @RequestParam String userPlatform) {
        return ResponseEntity.ok(gameService.updateUserPlatform(gameId, userPlatform));
    }

    @PatchMapping("/library/{gameId}/dlc")
    public ResponseEntity<GameItem> updateOwnedDlc(
            @PathVariable String gameId,
            @RequestBody List<String> ownedDlcIds) {
        return ResponseEntity.ok(gameService.updateOwnedDlc(gameId, ownedDlcIds));
    }

    @PatchMapping("/library/{gameId}/notes")
    public ResponseEntity<GameItem> updateNotes(
            @PathVariable String gameId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(gameService.updateNotes(gameId, notes != null ? notes : ""));
    }

    @PostMapping("/library/{gameId}/enrich")
    public ResponseEntity<GameItem> enrich(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.enrichIgdbRating(gameId));
    }

    @PostMapping("/library/{gameId}/refresh")
    public ResponseEntity<GameItem> refresh(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.refreshRatings(gameId));
    }

    @GetMapping("/details/{gameId}")
    public ResponseEntity<GameSearchResult> getDetails(@PathVariable String gameId) {
        GameSearchResult result = gameService.getDetails(gameId);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/creator/{companyId}")
    public ResponseEntity<List<GameSearchResult>> getWorksByDeveloper(
            @PathVariable String companyId) {
        return ResponseEntity.ok(gameService.getWorksByDeveloper(companyId));
    }

    @GetMapping("/developer-search")
    public ResponseEntity<List<Map<String, String>>> searchDevelopers(@RequestParam String name) {
        return ResponseEntity.ok(gameService.searchDevelopers(name));
    }
}
