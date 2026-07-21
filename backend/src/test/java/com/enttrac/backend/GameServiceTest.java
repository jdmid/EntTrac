package com.enttrac.backend;

import com.enttrac.backend.client.IgdbClient;
import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.model.item.GameItem;
import com.enttrac.backend.model.result.GameSearchResult;
import com.enttrac.backend.repository.GameRepository;
import com.enttrac.backend.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.enttrac.backend.auth.AuthTestSupport.TEST_USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private IgdbClient gameMetadataClient;

    @InjectMocks
    private GameService gameService;

    private GameItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new GameItem();
        testItem.setGameId("1942");
        testItem.setTitle("The Elder Scrolls V: Skyrim");
        testItem.setStatus("CONSUMING");
        testItem.setHoursPlayed(0);
        testItem.setDeveloperId("1234");
        testItem.setPlatforms(new ArrayList<>(List.of("PC", "Xbox Series X")));
    }

    @Test
    void search_ShouldDelegateToClient() {
        GameSearchResult result = GameSearchResult.builder()
                .id("1942").title("Skyrim").build();
        when(gameMetadataClient.search("skyrim")).thenReturn(List.of(result));

        List<GameSearchResult> results = gameService.search("skyrim");

        assertEquals(1, results.size());
        verify(gameMetadataClient, times(1)).search("skyrim");
    }

    @Test
    void getDetails_ShouldDelegateToClient() {
        GameSearchResult result = GameSearchResult.builder()
                .id("1942").title("Skyrim").build();
        when(gameMetadataClient.getDetails("1942")).thenReturn(result);

        GameSearchResult details = gameService.getDetails("1942");

        assertEquals("Skyrim", details.getTitle());
        verify(gameMetadataClient, times(1)).getDetails("1942");
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(gameRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));

        List<GameItem> library = gameService.getLibrary(TEST_USER_ID);

        assertEquals(1, library.size());
        verify(gameRepository, times(1)).findAll(TEST_USER_ID);
    }

    @Test
    void getGame_ShouldReturnItemWhenFound() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.getGame(TEST_USER_ID,"1942");

        assertEquals("The Elder Scrolls V: Skyrim", result.getTitle());
    }

    @Test
    void getGame_ShouldReturnNullWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        GameItem result = gameService.getGame(TEST_USER_ID,"notreal");

        assertNull(result);
    }

    @Test
    void addToLibrary_ShouldSetPkAndSk() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(null);

        GameItem result = gameService.addToLibrary(TEST_USER_ID,testItem);

        verify(gameRepository, times(1)).save(testItem);
        assertEquals(TEST_USER_ID, result.getPk());
        assertEquals("GAME#IGDB#1942", result.getSk());
        assertEquals("1234", result.getDeveloperId());
    }

    @Test
    void addToLibrary_ShouldReturnExistingWhenAlreadyInLibrary() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.addToLibrary(TEST_USER_ID,testItem);

        assertEquals(testItem, result);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void addToLibrary_ShouldStripNullsFromPlatforms() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(null);
        testItem.setPlatforms(new ArrayList<>(List.of("PC", "Xbox Series X")));

        GameItem result = gameService.addToLibrary(TEST_USER_ID,testItem);

        assertNotNull(result.getPlatforms());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void addToLibrary_ShouldHandleNullPlatforms() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(null);
        testItem.setPlatforms(null);

        assertDoesNotThrow(() -> gameService.addToLibrary(TEST_USER_ID,testItem));
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void addToLibrary_ShouldHandleNullOwnedDlcIds() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(null);
        testItem.setOwnedDlcIds(null);

        assertDoesNotThrow(() -> gameService.addToLibrary(TEST_USER_ID,testItem));
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldUpdateHoursPlayed() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.updateProgress(TEST_USER_ID,"1942", 42);

        assertEquals(42, result.getHoursPlayed());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.updateProgress(TEST_USER_ID,"notreal", 42));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void updateScore_ShouldUpdateScore() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.updateScore(TEST_USER_ID,"1942", 9);

        assertEquals(9, result.getScore());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateScore_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.updateScore(TEST_USER_ID,"notreal", 9));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.updateStatus(TEST_USER_ID,"1942", "FINISHED");

        assertEquals("FINISHED", result.getStatus());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateStatus_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.updateStatus(TEST_USER_ID,"notreal", "FINISHED"));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void updateUserPlatform_ShouldUpdatePlatform() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.updateUserPlatform(TEST_USER_ID,"1942", "PC");

        assertEquals("PC", result.getUserPlatform());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateUserPlatform_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.updateUserPlatform(TEST_USER_ID,"notreal", "PC"));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void updateOwnedDlc_ShouldUpdateDlcList() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);
        List<String> dlcIds = List.of("dlc1", "dlc2");

        GameItem result = gameService.updateOwnedDlc(TEST_USER_ID,"1942", dlcIds);

        assertEquals(2, result.getOwnedDlcIds().size());
        assertTrue(result.getOwnedDlcIds().contains("dlc1"));
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateOwnedDlc_ShouldStripBlankIds() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);
        List<String> dlcIds = new ArrayList<>(List.of("dlc1", "", "dlc2"));

        GameItem result = gameService.updateOwnedDlc(TEST_USER_ID,"1942", dlcIds);

        assertEquals(2, result.getOwnedDlcIds().size());
        assertFalse(result.getOwnedDlcIds().contains(""));
    }

    @Test
    void updateOwnedDlc_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.updateOwnedDlc(TEST_USER_ID,"notreal", List.of("dlc1")));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void updateNotes_ShouldUpdateNotes() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.updateNotes(TEST_USER_ID,"1942", "Great game");

        assertEquals("Great game", result.getNotes());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void updateNotes_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.updateNotes(TEST_USER_ID,"notreal", "notes"));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void enrichIgdbRating_ShouldFetchAndCacheWhenNull() {
        testItem.setIgdbRating(null);
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);
        when(gameMetadataClient.getIgdbRating("1942")).thenReturn(87.5);

        GameItem result = gameService.enrichIgdbRating(TEST_USER_ID,"1942");

        assertEquals(87.5, result.getIgdbRating());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void enrichIgdbRating_ShouldSkipWhenAlreadyCached() {
        testItem.setIgdbRating(87.5);
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);

        GameItem result = gameService.enrichIgdbRating(TEST_USER_ID,"1942");

        assertEquals(87.5, result.getIgdbRating());
        verify(gameMetadataClient, never()).getIgdbRating(any());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void enrichIgdbRating_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.enrichIgdbRating(TEST_USER_ID,"notreal"));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void refreshRatings_ShouldUpdateRatingsAndSave() {
        GameSearchResult details = GameSearchResult.builder()
                .id("1942")
                .igdbRating(87.5)
                .igdbCriticRating(92.0)
                .build();

        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);
        when(gameMetadataClient.getDetails("1942")).thenReturn(details);

        GameItem result = gameService.refreshRatings(TEST_USER_ID,"1942");

        assertEquals(87.5, result.getIgdbRating());
        assertEquals(92.0, result.getIgdbCriticRating());
        assertNotNull(result.getLastRefreshed());
        verify(gameRepository, times(1)).save(testItem);
    }

    @Test
    void refreshRatings_ShouldThrowWhenNotFound() {
        when(gameRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                gameService.refreshRatings(TEST_USER_ID,"notreal"));

        assertEquals("Game not found: notreal", ex.getMessage());
    }

    @Test
    void refreshRatings_ShouldNotSaveWhenDetailsNull() {
        when(gameRepository.findById(TEST_USER_ID,"1942")).thenReturn(testItem);
        when(gameMetadataClient.getDetails("1942")).thenReturn(null);

        gameService.refreshRatings(TEST_USER_ID,"1942");

        verify(gameRepository, never()).save(any());
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        gameService.removeFromLibrary(TEST_USER_ID,"1942");

        verify(gameRepository, times(1)).delete(TEST_USER_ID,"1942");
    }

    @Test
    void getWorksByDeveloper_ShouldDelegateToClient() {
        GameSearchResult result = GameSearchResult.builder()
                .id("1942")
                .title("Skyrim")
                .build();

        GameRepository gameRepository = mock(GameRepository.class);
        IgdbClient igdbClient = mock(IgdbClient.class);
        when(igdbClient.getWorksByCreator("1234")).thenReturn(List.of(result));

        GameService serviceWithIgdb = new GameService(gameRepository, igdbClient);
        List<GameSearchResult> results = serviceWithIgdb.getWorksByDeveloper("1234");

        assertEquals(1, results.size());
        assertEquals("Skyrim", results.get(0).getTitle());
        verify(igdbClient, times(1)).getWorksByCreator("1234");
    }
}