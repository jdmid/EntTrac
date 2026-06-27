package com.enttrac.backend;

import com.enttrac.backend.client.JikanClient;
import com.enttrac.backend.client.MediaMetadataClient;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private JikanClient animeMetadataClient;

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
    }

    @Test
    void search_ShouldDelegateToClient() {
        AnimeSearchResult result = AnimeSearchResult.builder()
                .id("21").title("One Piece").build();
        when(animeMetadataClient.search("one piece")).thenReturn(List.of(result));

        List<AnimeSearchResult> results = animeService.search("one piece");

        assertEquals(1, results.size());
        verify(animeMetadataClient, times(1)).search("one piece");
    }

    @Test
    void getDetails_ShouldDelegateToClient() {
        AnimeSearchResult result = AnimeSearchResult.builder()
                .id("21").title("One Piece").build();
        when(animeMetadataClient.getDetails("21")).thenReturn(result);

        AnimeSearchResult details = animeService.getDetails("21");

        assertEquals("One Piece", details.getTitle());
        verify(animeMetadataClient, times(1)).getDetails("21");
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(animeRepository.findAll()).thenReturn(List.of(testItem));

        List<AnimeItem> library = animeService.getLibrary();

        assertEquals(1, library.size());
        verify(animeRepository, times(1)).findAll();
    }

    @Test
    void getAnime_ShouldReturnItemWhenFound() {
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.getAnime("21");

        assertEquals("One Piece", result.getTitle());
    }

    @Test
    void getAnime_ShouldReturnNullWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        AnimeItem result = animeService.getAnime("notreal");

        assertNull(result);
    }

    @Test
    void addToLibrary_ShouldSaveAndReturnNewItem() {
        when(animeRepository.findById("21")).thenReturn(null);

        AnimeItem result = animeService.addToLibrary(testItem);

        verify(animeRepository, times(1)).save(testItem);
        assertEquals("USER#default", result.getPk());
        assertEquals("ANIME#ANILIST#21", result.getSk());
        assertEquals("1", result.getStudioId());
    }

    @Test
    void addToLibrary_ShouldReturnExistingWhenAlreadyInLibrary() {
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.addToLibrary(testItem);

        assertEquals(testItem, result);
        verify(animeRepository, never()).save(any());
    }

    @Test
    void updateProgress_ShouldUpdateEpisodesWatched() {
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.updateProgress("21", 50);

        assertEquals(50, result.getEpisodesWatched());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateProgress("notreal", 50));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void updateScore_ShouldUpdateScore() {
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.updateScore("21", 9);

        assertEquals(9, result.getScore());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateScore_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateScore("notreal", 9));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.updateStatus("21", "CONSUMING");

        assertEquals("CONSUMING", result.getStatus());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateStatus_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateStatus("notreal", "CONSUMING"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void refreshLatestEpisode_ShouldUpdateTotalEpisodesWhenDetailsAvailable() {
        testItem.setTotalEpisodes(900);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        AnimeItem result = animeService.refreshLatestEpisode("21");

        assertEquals(1000, result.getTotalEpisodes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestEpisode_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.refreshLatestEpisode("notreal"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        animeService.removeFromLibrary("21");

        verify(animeRepository, times(1)).delete("21");
    }

    @Test
    void updateNotes_ShouldUpdateNotes() {
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.updateNotes("21", "Great anime");

        assertEquals("Great anime", result.getNotes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void updateNotes_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.updateNotes("notreal", "some notes"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void refreshAll_ShouldUpdateEpisodesWhenDetailsAvailable() {
        testItem.setTotalEpisodes(900);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshAll();

        assertEquals(1, result.size());
        assertEquals(1000, result.get(0).getTotalEpisodes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshAll_ShouldSkipUpdateWhenDetailsNull() {
        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(null);

        List<AnimeItem> result = animeService.refreshAll();

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

        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshAll();

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshAll_ShouldContinueWhenOneItemFails() {
        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21"))
                .thenThrow(new RuntimeException("API down"));

        List<AnimeItem> result = animeService.refreshAll();

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

        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshAll();

        assertEquals("ongoing", result.get(0).getSeriesStatus());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldSkipCompletedItems() {
        testItem.setSeriesStatus("completed");
        when(animeRepository.findAll()).thenReturn(List.of(testItem));

        List<AnimeItem> result = animeService.refreshOngoing();

        assertEquals(1, result.size());
        verify(animeMetadataClient, never()).getDetails(any());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldSkipCancelledItems() {
        testItem.setSeriesStatus("cancelled");
        when(animeRepository.findAll()).thenReturn(List.of(testItem));

        List<AnimeItem> result = animeService.refreshOngoing();

        assertEquals(1, result.size());
        verify(animeMetadataClient, never()).getDetails(any());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldUpdateWhenDetailsAvailable() {
        testItem.setSeriesStatus("ongoing");
        testItem.setTotalEpisodes(900);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshOngoing();

        assertEquals(1, result.size());
        assertEquals(1000, result.get(0).getTotalEpisodes());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldSkipWhenDetailsNull() {
        testItem.setSeriesStatus("ongoing");
        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(null);

        List<AnimeItem> result = animeService.refreshOngoing();

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldContinueWhenOneItemFails() {
        testItem.setSeriesStatus("ongoing");
        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21"))
                .thenThrow(new RuntimeException("API down"));

        List<AnimeItem> result = animeService.refreshOngoing();

        assertEquals(1, result.size());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldUpdateLatestEpisodeWhenChanged() {
        testItem.setSeriesStatus("ongoing");
        testItem.setLatestEpisode(10);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").latestEpisode(11).build();

        when(animeRepository.findAll()).thenReturn(List.of(testItem));
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        List<AnimeItem> result = animeService.refreshOngoing();

        assertEquals(11, result.get(0).getLatestEpisode());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void enrichMalRating_ShouldFetchAndCacheWhenNull() {
        testItem.setMalRating(null);
        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getMalRating("21")).thenReturn(8.7);

        AnimeItem result = animeService.enrichMalRating("21");

        assertEquals(8.7, result.getMalRating());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void enrichMalRating_ShouldSkipWhenAlreadyCached() {
        testItem.setMalRating(8.7);
        when(animeRepository.findById("21")).thenReturn(testItem);

        AnimeItem result = animeService.enrichMalRating("21");

        assertEquals(8.7, result.getMalRating());
        verify(animeMetadataClient, never()).getMalRating(any());
        verify(animeRepository, never()).save(any());
    }

    @Test
    void enrichMalRating_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.enrichMalRating("notreal"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void refreshLatestEpisode_ShouldCacheMalRatingWhenNull() {
        testItem.setMalRating(null);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getDetails("21")).thenReturn(details);
        when(animeMetadataClient.getMalRating("21")).thenReturn(8.7);

        AnimeItem result = animeService.refreshLatestEpisode("21");

        assertEquals(1000, result.getTotalEpisodes());
        assertEquals(8.7, result.getMalRating());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestEpisode_ShouldSkipMalRatingWhenAlreadyCached() {
        testItem.setMalRating(8.7);
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(1000).build();

        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        AnimeItem result = animeService.refreshLatestEpisode("21");

        assertEquals(8.7, result.getMalRating());
        verify(animeMetadataClient, never()).getMalRating(any());
    }

    @Test
    void refreshLatestEpisode_ShouldSaveEvenWhenDetailsNull() {
        testItem.setMalRating(null);
        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getDetails("21")).thenReturn(null);
        when(animeMetadataClient.getMalRating("21")).thenReturn(8.7);

        AnimeItem result = animeService.refreshLatestEpisode("21");

        assertNotNull(result);
        assertEquals(8.7, result.getMalRating());
        verify(animeRepository, times(1)).save(testItem);
    }

    @Test
    void getWorksByProducer_ShouldDelegateToClient() {
        JikanClient.PagedResult<AnimeSearchResult> pagedResult =
                new JikanClient.PagedResult<>(
                        List.of(AnimeSearchResult.builder()
                                .id("21")
                                .title("One Piece")
                                .build()),
                        false
                );

        AnimeRepository animeRepository = mock(AnimeRepository.class);
        JikanClient jikanClient = mock(JikanClient.class);
        when(jikanClient.getWorksByProducer("1", 1, "name")).thenReturn(pagedResult);

        AnimeService serviceWithJikan = new AnimeService(animeRepository, jikanClient);
        JikanClient.PagedResult<AnimeSearchResult> result =
                serviceWithJikan.getWorksByProducer("1", 1, "name");

        assertEquals(1, result.items.size());
        assertEquals("One Piece", result.items.get(0).getTitle());
        assertFalse(result.hasNextPage);
        verify(jikanClient, times(1)).getWorksByProducer("1", 1, "name");
    }
}