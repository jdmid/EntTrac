package com.enttrac.backend;

import com.enttrac.backend.client.AniListClient;
import com.enttrac.backend.model.item.AnimeItem;
import com.enttrac.backend.model.result.AnimeSearchResult;
import com.enttrac.backend.repository.AnimeRepository;
import com.enttrac.backend.service.AnimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.enttrac.backend.auth.AuthTestSupport.TEST_USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private AniListClient aniListClient;

    @InjectMocks
    private AnimeService animeService;

    private AnimeItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new AnimeItem();
        testItem.setAnimeId("21");
        testItem.setTitle("One Piece");
        testItem.setStatus("PLANNED");
        testItem.setStudioId("1");
        lenient().when(aniListClient.getAnilistAnimeRating(any())).thenReturn(null);
    }

    @Test
    void search_ShouldDelegateToClient() {
        AnimeSearchResult result = AnimeSearchResult.builder()
                .id("21").title("One Piece").build();
        when(aniListClient.search("one piece")).thenReturn(List.of(result));

        List<AnimeSearchResult> results = animeService.search("one piece");

        assertEquals(1, results.size());
        verify(aniListClient, times(1)).search("one piece");
    }

    @Test
    void getDetails_ShouldDelegateToClient() {
        AnimeSearchResult result = AnimeSearchResult.builder()
                .id("21").title("One Piece").build();
        when(aniListClient.getDetails("21")).thenReturn(result);

        AnimeSearchResult details = animeService.getDetails("21");

        assertEquals("One Piece", details.getTitle());
        verify(aniListClient, times(1)).getDetails("21");
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));

        List<AnimeItem> library = animeService.getLibrary(TEST_USER_ID);

        assertEquals(1, library.size());
        verify(animeRepository, times(1)).findAll(TEST_USER_ID);
    }

    @Test
    void getAnime_ShouldReturnItemWhenFound() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.getAnime(TEST_USER_ID,"21");

        assertEquals("One Piece", result.getTitle());
    }

    @Test
    void getAnime_ShouldReturnNullWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        AnimeItem result = animeService.getAnime(TEST_USER_ID,"notreal");

        assertNull(result);
    }

    @Test
    void addToLibrary_ShouldSaveAndReturnNewItem() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(null);

        AnimeItem result = animeService.addToLibrary(TEST_USER_ID,testItem);

        verify(animeRepository, times(1)).save(testItem);
        assertEquals(TEST_USER_ID, result.getPk());
        assertEquals("ANIME#ANILIST#21", result.getSk());
        assertEquals("1", result.getStudioId());
    }

    @Test
    void addToLibrary_ShouldReturnExistingWhenAlreadyInLibrary() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.addToLibrary(TEST_USER_ID,testItem);

        assertEquals(testItem, result);
        verify(animeRepository, never()).save(any());
    }

    @Test
    void updateProgress_ShouldUpdateEpisodesWatched() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.updateProgress(TEST_USER_ID,"21", 50);

        assertEquals(50, result.getEpisodesWatched());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldThrowWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateProgress(TEST_USER_ID,"notreal", 50));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void updateScore_ShouldUpdateScore() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.updateScore(TEST_USER_ID,"21", 9);

        assertEquals(9, result.getScore());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateScore_ShouldThrowWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateScore(TEST_USER_ID,"notreal", 9));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.updateStatus(TEST_USER_ID,"21", "CONSUMING");

        assertEquals("CONSUMING", result.getStatus());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateStatus_ShouldThrowWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateStatus(TEST_USER_ID,"notreal", "CONSUMING"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void refreshLatestEpisode_ShouldUpdateTotalEpisodesWhenDetailsAvailable() {
        testItem.setTotalEpisodes(900);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);
        when(aniListClient.getDetails("21")).thenReturn(details);

        AnimeItem result = animeService.refreshLatestEpisode(TEST_USER_ID,"21");

        assertEquals(1000, result.getTotalEpisodes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestEpisode_ShouldThrowWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.refreshLatestEpisode(TEST_USER_ID,"notreal"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        animeService.removeFromLibrary(TEST_USER_ID,"21");

        verify(animeRepository, times(1)).delete(TEST_USER_ID,"21");
    }

    @Test
    void updateNotes_ShouldUpdateNotes() {
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.updateNotes(TEST_USER_ID,"21", "Great anime");

        assertEquals("Great anime", result.getNotes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateNotes_ShouldThrowWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateNotes(TEST_USER_ID,"notreal", "some notes"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void refreshAll_ShouldUpdateEpisodesWhenDetailsAvailable() {
        testItem.setTotalEpisodes(900);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshAll(TEST_USER_ID);

        assertEquals(1, result.size());
        assertEquals(1000, result.get(0).getTotalEpisodes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshAll_ShouldSkipUpdateWhenDetailsNull() {
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(null);

        List<AnimeItem> result = animeService.refreshAll(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshAll_ShouldSkipUpdateWhenTotalEpisodesNull() {
        testItem.setSeriesStatus("ongoing");
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21")
                .totalEpisodes(null)
                .status("Currently Airing") // normalizes to "ongoing" — matches existing
                .build();

        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshAll(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshAll_ShouldContinueWhenOneItemFails() {
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21"))
                .thenThrow(new RuntimeException("API down"));

        List<AnimeItem> result = animeService.refreshAll(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshAll_ShouldNormalizeSeriesStatus() {
        testItem.setSeriesStatus(null);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21")
                .status("Currently Airing")
                .totalEpisodes(1000)
                .build();

        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshAll(TEST_USER_ID);

        assertEquals("ongoing", result.get(0).getSeriesStatus());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldSkipCompletedItems() {
        testItem.setSeriesStatus("completed");
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));

        List<AnimeItem> result = animeService.refreshOngoing(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(aniListClient, never()).getDetails(any());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldSkipCancelledItems() {
        testItem.setSeriesStatus("cancelled");
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));

        List<AnimeItem> result = animeService.refreshOngoing(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(aniListClient, never()).getDetails(any());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldUpdateWhenDetailsAvailable() {
        testItem.setSeriesStatus("ongoing");
        testItem.setTotalEpisodes(900);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshOngoing(TEST_USER_ID);

        assertEquals(1, result.size());
        assertEquals(1000, result.get(0).getTotalEpisodes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldSkipWhenDetailsNull() {
        testItem.setSeriesStatus("ongoing");
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(null);

        List<AnimeItem> result = animeService.refreshOngoing(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldContinueWhenOneItemFails() {
        testItem.setSeriesStatus("ongoing");
        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21"))
                .thenThrow(new RuntimeException("API down"));

        List<AnimeItem> result = animeService.refreshOngoing(TEST_USER_ID);

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldUpdateLatestEpisodeWhenChanged() {
        testItem.setSeriesStatus("ongoing");
        testItem.setLatestEpisode(10);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").latestEpisode(11).build();

        when(animeRepository.findAll(TEST_USER_ID)).thenReturn(List.of(testItem));
        when(aniListClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshOngoing(TEST_USER_ID);

        assertEquals(11, result.get(0).getLatestEpisode());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void enrichAniListRating_ShouldFetchAndCacheWhenNull() {
        testItem.setAnilistRating(null);
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);
        when(aniListClient.getAnilistAnimeRating("21")).thenReturn(8.7);

        AnimeItem result = animeService.enrichAniListRating(TEST_USER_ID,"21");

        assertEquals(8.7, result.getAnilistRating());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void enrichAniListRating_ShouldSkipWhenAlreadyCached() {
        testItem.setAnilistRating(85.0);
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.enrichAniListRating(TEST_USER_ID,"21");

        assertEquals(85.0, result.getAnilistRating());
        verify(aniListClient, never()).getAnilistAnimeRating(any());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void enrichAniListRating_ShouldThrowWhenNotFound() {
        when(animeRepository.findById(TEST_USER_ID,"notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.enrichAniListRating(TEST_USER_ID,"notreal"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void refreshLatestEpisode_ShouldSaveEvenWhenDetailsNull() {
        testItem.setAnilistRating(null);
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);
        when(aniListClient.getDetails("21")).thenReturn(null);

        AnimeItem result = animeService.refreshLatestEpisode(TEST_USER_ID,"21");

        assertNotNull(result);
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void enrichAniListRating_ShouldFetchAnilistRatingWhenNull() {
        testItem.setAnilistRating(null);
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);
        when(aniListClient.getAnilistAnimeRating("21")).thenReturn(85.0);

        AnimeItem result = animeService.enrichAniListRating(TEST_USER_ID,"21");

        assertEquals(85.0, result.getAnilistRating());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void enrichAniListRating_ShouldSkipAnilistRatingWhenAlreadyCached() {
        testItem.setAnilistRating(85.0);
        when(animeRepository.findById(TEST_USER_ID,"21")).thenReturn(testItem);

        AnimeItem result = animeService.enrichAniListRating(TEST_USER_ID,"21");

        assertEquals(85.0, result.getAnilistRating());
        verify(aniListClient, never()).getAnilistAnimeRating(any());
        verify(animeRepository, never()).save(any());
    }
}