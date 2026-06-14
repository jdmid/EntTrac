package com.enttrac.backend.service;

import com.enttrac.backend.client.IgdbClient;
import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.GameItem;
import com.enttrac.backend.model.result.GameSearchResult;
import com.enttrac.backend.repository.GameRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GameService extends MediaService<GameItem, GameSearchResult> {

    private final MediaMetadataClient<GameSearchResult> gameMetadataClient;

    public GameService(GameRepository gameRepository,
                       @Qualifier("igdbClient") MediaMetadataClient<GameSearchResult> gameMetadataClient) {
        super(gameRepository);
        this.gameMetadataClient = gameMetadataClient;
    }

    @Override
    protected String getEntityId(GameItem item) { return item.getGameId(); }

    @Override
    protected String buildSortKey(GameItem item) { return "GAME#IGDB#" + item.getGameId(); }

    @Override
    protected String getNotFoundMessage(String id) { return "Game not found: " + id; }

    @Override
    protected void beforeSave(GameItem item) {
        if (item.getPlatforms() != null) {
            item.getPlatforms().removeIf(p -> p == null);
        }
        if (item.getOwnedDlcIds() != null) {
            item.getOwnedDlcIds().removeIf(id -> id == null);
        }
    }

    public List<GameSearchResult> search(String query) {
        return gameMetadataClient.search(query);
    }

    public GameSearchResult getDetails(String id) {
        return gameMetadataClient.getDetails(id);
    }

    public GameItem getGame(String gameId) {
        return repository.findById(gameId);
    }

    public GameItem updateProgress(String gameId, int hoursPlayed) {
        GameItem item = repository.findById(gameId);
        if (item == null) throw new NotFoundException("Game not found: " + gameId);
        item.setHoursPlayed(hoursPlayed);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public GameItem updateUserPlatform(String gameId, String userPlatform) {
        GameItem item = repository.findById(gameId);
        if (item == null) throw new NotFoundException("Game not found: " + gameId);
        item.setUserPlatform(userPlatform);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public GameItem updateOwnedDlc(String gameId, List<String> ownedDlcIds) {
        GameItem item = repository.findById(gameId);
        if (item == null) throw new NotFoundException("Game not found: " + gameId);
        List<String> cleaned = new ArrayList<>(ownedDlcIds);
        cleaned.removeIf(id -> id == null || id.isBlank());
        item.setOwnedDlcIds(cleaned);
        item.setUpdatedAt(Instant.now().toString());
        repository.save(item);
        return item;
    }

    public GameItem enrichIgdbRating(String gameId) {
        GameItem item = repository.findById(gameId);
        if (item == null) throw new NotFoundException("Game not found: " + gameId);

        if (item.getIgdbRating() == null) {
            Double rating = gameMetadataClient.getCommunityRating(gameId);
            if (rating != null) {
                item.setIgdbRating(rating);
                item.setUpdatedAt(Instant.now().toString());
                repository.save(item);
            }
        }
        return item;
    }

    public List<GameSearchResult> getWorksByDeveloper(String companyId) {
        return ((IgdbClient) gameMetadataClient).getWorksByCreator(companyId);
    }

    public List<Map<String, String>> searchDevelopers(String name) {
        return gameMetadataClient.searchCreators(name);
    }

    public GameItem refreshRatings(String gameId) {
        GameItem item = repository.findById(gameId);
        if (item == null) throw new NotFoundException("Game not found: " + gameId);

        GameSearchResult details = gameMetadataClient.getDetails(gameId);
        if (details != null) {
            if (details.getIgdbRating() != null) {
                item.setIgdbRating(details.getIgdbRating());
            }
            if (details.getIgdbCriticRating() != null) {
                item.setIgdbCriticRating(details.getIgdbCriticRating());
            }
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
            repository.save(item);
        }
        return item;
    }
}
