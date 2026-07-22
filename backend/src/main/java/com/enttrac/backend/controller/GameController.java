package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.model.item.GameItem;
import com.enttrac.backend.model.MediaType;
import com.enttrac.backend.model.result.GameSearchResult;
import com.enttrac.backend.validation.ValidStatus;
import com.enttrac.backend.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
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
    public ResponseEntity<List<GameItem>> getLibrary(@CurrentUserId String userId) {
        return ResponseEntity.ok(gameService.getLibrary(userId));
    }

    @GetMapping("/library/{gameId}")
    public ResponseEntity<GameItem> getGame(@CurrentUserId String userId, @PathVariable String gameId) {
        GameItem item = gameService.getGame(userId, gameId);
        if (item == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(item);
    }

    @PostMapping("/library")
    public ResponseEntity<GameItem> addToLibrary(@CurrentUserId String userId, @Valid @RequestBody GameItem item) {
        return ResponseEntity.ok(gameService.addToLibrary(userId, item));
    }

    @DeleteMapping("/library/{gameId}")
    public ResponseEntity<Void> removeFromLibrary(@CurrentUserId String userId, @PathVariable String gameId) {
        gameService.removeFromLibrary(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/library/{gameId}/progress")
    public ResponseEntity<GameItem> updateProgress(
            @CurrentUserId String userId,
            @PathVariable String gameId,
            @RequestParam int hoursPlayed) {
        return ResponseEntity.ok(gameService.updateProgress(userId, gameId, hoursPlayed));
    }

    @PatchMapping("/library/{gameId}/score")
    public ResponseEntity<GameItem> updateScore(
            @CurrentUserId String userId,
            @PathVariable String gameId,
            @RequestParam @Min(1) @Max(10) int score) {
        return ResponseEntity.ok(gameService.updateScore(userId, gameId, score));
    }

    @PatchMapping("/library/{gameId}/status")
    public ResponseEntity<GameItem> updateStatus(
            @CurrentUserId String userId,
            @PathVariable String gameId,
            @RequestParam @ValidStatus(MediaType.GAME) String status) {
        return ResponseEntity.ok(gameService.updateStatus(userId, gameId, status));
    }

    @PatchMapping("/library/{gameId}/platform")
    public ResponseEntity<GameItem> updateUserPlatform(
            @CurrentUserId String userId,
            @PathVariable String gameId,
            @RequestParam String userPlatform) {
        return ResponseEntity.ok(gameService.updateUserPlatform(userId, gameId, userPlatform));
    }

    @PatchMapping("/library/{gameId}/dlc")
    public ResponseEntity<GameItem> updateOwnedDlc(
            @CurrentUserId String userId,
            @PathVariable String gameId,
            @RequestBody List<String> ownedDlcIds) {
        return ResponseEntity.ok(gameService.updateOwnedDlc(userId, gameId, ownedDlcIds));
    }

    @PatchMapping("/library/{gameId}/notes")
    public ResponseEntity<GameItem> updateNotes(
            @CurrentUserId String userId,
            @PathVariable String gameId,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(gameService.updateNotes(userId, gameId, notes != null ? notes : ""));
    }

    @PostMapping("/library/{gameId}/enrich")
    public ResponseEntity<GameItem> enrich(@CurrentUserId String userId, @PathVariable String gameId) {
        return ResponseEntity.ok(gameService.enrichIgdbRating(userId, gameId));
    }

    @PostMapping("/library/{gameId}/refresh")
    public ResponseEntity<GameItem> refresh(@CurrentUserId String userId, @PathVariable String gameId) {
        return ResponseEntity.ok(gameService.refreshRatings(userId, gameId));
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
