package com.enttrac.backend;

import com.enttrac.backend.client.MediaMetadataClient;
import com.enttrac.backend.client.TmdbTvClient;
import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.repository.TvRepository;
import com.enttrac.backend.service.TvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TvServiceTest {

    @Mock
    private TvRepository tvRepository;

    @Mock
    private TmdbTvClient tvMetadataClient;

    @InjectMocks
    private TvService tvService;

    private TvItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new TvItem();
        testItem.setTvId("1396");
        testItem.setTitle("Breaking Bad");
        testItem.setStatus("CONSUMING");
        testItem.setEpisodesWatched(10);
        testItem.setCurrentSeason(1);
        testItem.setTotalEpisodes(62);
        testItem.setSeasonEpisodes(null);
        testItem.setCreatorName("Vince Gilligan");
        testItem.setCreatorId("66633");
    }

    @Test
    void search_ShouldDelegateToClient() {
        TvSearchResult result = TvSearchResult.builder()
                .id("1396").title("Breaking Bad").build();
        when(tvMetadataClient.search("breaking bad")).thenReturn(List.of(result));

        List<TvSearchResult> results = tvService.search("breaking bad");

        assertEquals(1, results.size());
        verify(tvMetadataClient, times(1)).search("breaking bad");
    }

    @Test
    void getDetails_ShouldDelegateToClient() {
        TvSearchResult result = TvSearchResult.builder()
                .id("1396").title("Breaking Bad").build();
        when(tvMetadataClient.getDetails("1396")).thenReturn(result);

        TvSearchResult details = tvService.getDetails("1396");

        assertEquals("Breaking Bad", details.getTitle());
        verify(tvMetadataClient, times(1)).getDetails("1396");
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(tvRepository.findAll()).thenReturn(List.of(testItem));

        List<TvItem> library = tvService.getLibrary();

        assertEquals(1, library.size());
        verify(tvRepository, times(1)).findAll();
    }

    @Test
    void getTvShow_ShouldReturnItemWhenFound() {
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.getTvShow("1396");

        assertEquals("Breaking Bad", result.getTitle());
    }

    @Test
    void getTvShow_ShouldReturnNullWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        TvItem result = tvService.getTvShow("notreal");

        assertNull(result);
    }

    @Test
    void addToLibrary_ShouldSaveAndReturnNewItem() {
        when(tvRepository.findById("1396")).thenReturn(null);

        TvItem result = tvService.addToLibrary(testItem);

        verify(tvRepository, times(1)).save(testItem);
        assertEquals("USER#default", result.getPk());
        assertEquals("TV#TMDB#1396", result.getSk());
        assertEquals("Vince Gilligan", result.getCreatorName());
        assertEquals("66633", result.getCreatorId());
    }

    @Test
    void addToLibrary_ShouldReturnExistingWhenAlreadyInLibrary() {
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.addToLibrary(testItem);

        assertEquals(testItem, result);
        verify(tvRepository, never()).save(any());
    }

    @Test
    void updateProgress_ShouldUpdateEpisodesAndSeason() {
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.updateProgress("1396", 20, 2);

        assertEquals(20, result.getEpisodesWatched());
        assertEquals(2, result.getCurrentSeason());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.updateProgress("notreal", 5, 1));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }

    @Test
    void updateScore_ShouldUpdateScore() {
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.updateScore("1396", 9);

        assertEquals(9, result.getScore());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void updateScore_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.updateScore("notreal", 9));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.updateStatus("1396", "FINISHED");

        assertEquals("FINISHED", result.getStatus());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void updateStatus_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.updateStatus("notreal", "FINISHED"));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }

    @Test
    void updateNotes_ShouldUpdateNotes() {
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.updateNotes("1396", "Great show");

        assertEquals("Great show", result.getNotes());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void updateNotes_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.updateNotes("notreal", "notes"));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        tvService.removeFromLibrary("1396");

        verify(tvRepository, times(1)).delete("1396");
    }

    @Test
    void refreshAll_ShouldUpdateWhenDetailsAvailable() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .seasonEpisodes(List.of(7, 13, 13, 13, 16))
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshAll();

        assertEquals(1, result.size());
        assertEquals(62, result.get(0).getTotalEpisodes());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshAll_ShouldSkipWhenDetailsNull() {
        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(null);

        List<TvItem> result = tvService.refreshAll();

        assertEquals(1, result.size());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void refreshAll_ShouldContinueWhenOneItemFails() {
        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396"))
                .thenThrow(new RuntimeException("API down"));

        List<TvItem> result = tvService.refreshAll();

        assertEquals(1, result.size());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void refreshLatestEpisodes_ShouldUpdateAllFieldsWhenAvailable() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .seasonEpisodes(List.of(7, 13, 13, 13, 16))
                .nextEpisodeDate("S6 E1 · \"Pilot\" · 2025-01-01")
                .status("Returning Series")
                .numberOfSeasons(5)
                .build();

        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        TvItem result = tvService.refreshLatestEpisodes("1396");

        assertEquals(62, result.getTotalEpisodes());
        assertEquals("S6 E1 · \"Pilot\" · 2025-01-01", result.getNextEpisodeDate());
        assertEquals("ongoing", result.getSeriesStatus());
        assertEquals(5, result.getNumberOfSeasons());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestEpisodes_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.refreshLatestEpisodes("notreal"));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }

    @Test
    void refreshAll_ShouldUpdateAllFieldsWhenAvailable() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .seasonEpisodes(List.of(7, 13, 13, 13, 16))
                .nextEpisodeDate("S6 E1 · \"Pilot\" · 2025-01-01")
                .status("Returning Series")
                .numberOfSeasons(5)
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshAll();

        assertEquals(62, result.get(0).getTotalEpisodes());
        assertEquals("ongoing", result.get(0).getSeriesStatus());
        assertEquals(5, result.get(0).getNumberOfSeasons());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void addToLibrary_ShouldCleanNullsFromSeasonEpisodes() {
        when(tvRepository.findById("1396")).thenReturn(null);
        testItem.setSeasonEpisodes(new ArrayList<>(List.of(8, 10)));

        TvItem result = tvService.addToLibrary(testItem);

        verify(tvRepository, times(1)).save(testItem);
        assertNotNull(result.getSeasonEpisodes());
    }

    @Test
    void refreshLatestEpisodes_ShouldHandleNullSeasonEpisodes() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .status("Returning Series")
                .numberOfSeasons(5)
                .build();

        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        TvItem result = tvService.refreshLatestEpisodes("1396");

        assertEquals(62, result.getTotalEpisodes());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestEpisodes_ShouldHandleNullIndividualFields() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .seasonEpisodes(List.of(7, 13))
                .build();

        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        TvItem result = tvService.refreshLatestEpisodes("1396");

        assertNotNull(result);
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshAll_ShouldHandleNullSeasonEpisodes() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .status("Returning Series")
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshAll();

        assertEquals(1, result.size());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshAll_ShouldHandleNullIndividualFields() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .seasonEpisodes(List.of(7, 13))
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshAll();

        assertEquals(1, result.size());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshAll_ShouldHandleUnrecognizedSeriesStatus() {
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .status("Unknown Status")
                .totalEpisodes(62)
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        assertDoesNotThrow(() -> tvService.refreshAll());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldSkipCompletedItems() {
        testItem.setSeriesStatus("completed");
        when(tvRepository.findAll()).thenReturn(List.of(testItem));

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals(1, result.size());
        verify(tvMetadataClient, never()).getDetails(any());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldSkipCancelledItems() {
        testItem.setSeriesStatus("cancelled");
        when(tvRepository.findAll()).thenReturn(List.of(testItem));

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals(1, result.size());
        verify(tvMetadataClient, never()).getDetails(any());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldUpdateWhenDetailsAvailable() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .seasonEpisodes(List.of(7, 13, 13, 13, 16))
                .status("Returning Series")
                .numberOfSeasons(5)
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals(1, result.size());
        assertEquals(62, result.get(0).getTotalEpisodes());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldSkipWhenDetailsNull() {
        testItem.setSeriesStatus("ongoing");
        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(null);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals(1, result.size());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldContinueWhenOneItemFails() {
        testItem.setSeriesStatus("ongoing");
        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396"))
                .thenThrow(new RuntimeException("API down"));

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals(1, result.size());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void refreshOngoing_ShouldUpdateNextEpisodeDateWhenChanged() {
        testItem.setSeriesStatus("ongoing");
        testItem.setNextEpisodeDate(null);
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .nextEpisodeDate("S2 E1 · 2025-06-01")
                .build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals("S2 E1 · 2025-06-01", result.get(0).getNextEpisodeDate());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshOngoing_ShouldNormalizeInProductionStatus() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396").status("In Production").build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals("in production", result.get(0).getSeriesStatus());
    }

    @Test
    void refreshOngoing_ShouldNormalizePilotStatus() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396").status("Pilot").build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals("upcoming", result.get(0).getSeriesStatus());
    }

    @Test
    void refreshOngoing_ShouldNormalizePlannedStatus() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396").status("Planned").build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals("upcoming", result.get(0).getSeriesStatus());
    }

    @Test
    void refreshOngoing_ShouldNormalizeEndedStatus() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396").status("Ended").build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals("completed", result.get(0).getSeriesStatus());
    }

    @Test
    void refreshOngoing_ShouldNormalizeCanceledStatus() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396").status("Canceled").build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertEquals("cancelled", result.get(0).getSeriesStatus());
    }

    @Test
    void normalizeSeriesStatus_ShouldReturnNullForUnknownStatus() {
        testItem.setSeriesStatus("ongoing");
        TvSearchResult details = TvSearchResult.builder()
                .id("1396").status("Unknown Status").build();

        when(tvRepository.findAll()).thenReturn(List.of(testItem));
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        List<TvItem> result = tvService.refreshOngoing();

        assertNull(result.get(0).getSeriesStatus());
    }

    @Test
    void enrichTmdbRating_ShouldFetchAndCacheWhenNull() {
        testItem.setTmdbRating(null);
        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getTmdbRating("1396")).thenReturn(9.5);

        TvItem result = tvService.enrichTmdbRating("1396");

        assertEquals(9.5, result.getTmdbRating());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void enrichTmdbRating_ShouldSkipWhenAlreadyCached() {
        testItem.setTmdbRating(9.5);
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.enrichTmdbRating("1396");

        assertEquals(9.5, result.getTmdbRating());
        verify(tvMetadataClient, never()).getTmdbRating(any());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void enrichTmdbRating_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.enrichTmdbRating("notreal"));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }

    @Test
    void refreshLatestEpisodes_ShouldCacheTmdbRatingWhenNull() {
        testItem.setTmdbRating(null);
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .seasonEpisodes(List.of(7, 13, 13, 13, 16))
                .status("Returning Series")
                .numberOfSeasons(5)
                .build();

        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);
        when(tvMetadataClient.getTmdbRating("1396")).thenReturn(9.5);

        TvItem result = tvService.refreshLatestEpisodes("1396");

        assertEquals(62, result.getTotalEpisodes());
        assertEquals(9.5, result.getTmdbRating());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestEpisodes_ShouldSkipTmdbRatingWhenAlreadyCached() {
        testItem.setTmdbRating(9.5);
        TvSearchResult details = TvSearchResult.builder()
                .id("1396")
                .totalEpisodes(62)
                .status("Returning Series")
                .build();

        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getDetails("1396")).thenReturn(details);

        TvItem result = tvService.refreshLatestEpisodes("1396");

        assertEquals(9.5, result.getTmdbRating());
        verify(tvMetadataClient, never()).getTmdbRating(any());
    }

    @Test
    void refreshLatestEpisodes_ShouldSaveEvenWhenDetailsNull() {
        testItem.setTmdbRating(null);
        when(tvRepository.findById("1396")).thenReturn(testItem);
        when(tvMetadataClient.getDetails("1396")).thenReturn(null);
        when(tvMetadataClient.getTmdbRating("1396")).thenReturn(9.5);

        TvItem result = tvService.refreshLatestEpisodes("1396");

        assertNotNull(result);
        assertEquals(9.5, result.getTmdbRating());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void getWorksByCreator_ShouldDelegateToClient() {
        TvSearchResult result = TvSearchResult.builder()
                .id("1396")
                .title("Breaking Bad")
                .build();
        when(tvMetadataClient.getWorksByCreator("66633")).thenReturn(List.of(result));

        List<TvSearchResult> results = tvService.getWorksByCreator("66633");

        assertEquals(1, results.size());
        assertEquals("Breaking Bad", results.get(0).getTitle());
        verify(tvMetadataClient, times(1)).getWorksByCreator("66633");
    }

    @Test
    void enrichWatchProviders_ShouldFetchAndCacheWhenNull() {
        testItem.setWatchProviders(null);
        testItem.setWatchProvidersRefreshedAt(null);

        List<String> providers = List.of("Netflix", "Hulu");
        TmdbTvClient tmdbTvClient = mock(TmdbTvClient.class);
        when(tmdbTvClient.getWatchProviders("1396", "US")).thenReturn(providers);
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvService serviceWithTmdb = new TvService(tvRepository, tmdbTvClient);
        TvItem result = serviceWithTmdb.enrichWatchProviders("1396", "US");

        assertEquals(2, result.getWatchProviders().size());
        assertTrue(result.getWatchProviders().contains("Netflix"));
        assertNotNull(result.getWatchProvidersRefreshedAt());
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void enrichWatchProviders_ShouldSkipWhenCacheStillValid() {
        // Set refreshed timestamp to 1 day ago — within 7-day TTL
        testItem.setWatchProviders(List.of("Netflix"));
        testItem.setWatchProvidersRefreshedAt(
                Instant.now().minus(Duration.ofDays(1)).toString());

        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvItem result = tvService.enrichWatchProviders("1396", "US");

        assertEquals(1, result.getWatchProviders().size());
        verify(tvRepository, never()).save(any());
    }

    @Test
    void enrichWatchProviders_ShouldRefetchWhenTtlExpired() {
        // Set refreshed timestamp to 8 days ago — past 7-day TTL
        testItem.setWatchProviders(List.of("Netflix"));
        testItem.setWatchProvidersRefreshedAt(
                Instant.now().minus(Duration.ofDays(8)).toString());

        List<String> freshProviders = List.of("Netflix", "Max");
        TmdbTvClient tmdbTvClient = mock(TmdbTvClient.class);
        when(tmdbTvClient.getWatchProviders("1396", "US")).thenReturn(freshProviders);
        when(tvRepository.findById("1396")).thenReturn(testItem);

        TvService serviceWithTmdb = new TvService(tvRepository, tmdbTvClient);
        TvItem result = serviceWithTmdb.enrichWatchProviders("1396", "US");

        assertEquals(2, result.getWatchProviders().size());
        assertTrue(result.getWatchProviders().contains("Max"));
        verify(tvRepository, times(1)).save(testItem);
    }

    @Test
    void enrichWatchProviders_ShouldThrowWhenNotFound() {
        when(tvRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tvService.enrichWatchProviders("notreal", "US"));

        assertEquals("TV show not found: notreal", ex.getMessage());
    }
}
