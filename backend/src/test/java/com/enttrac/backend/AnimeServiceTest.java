package com.enttrac.backend.service;

import com.enttrac.backend.client.AnimeMetadataClient;
import com.enttrac.backend.model.AnimeItem;
import com.enttrac.backend.model.AnimeSearchResult;
import com.enttrac.backend.repository.AnimeRepository;
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
    private AnimeMetadataClient animeMetadataClient;

    @InjectMocks
    private AnimeService animeService;

    private AnimeItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new AnimeItem();
        testItem.setAnimeId("21");
        testItem.setTitle("One Piece");
        testItem.setStatus("PLANNED");
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
        assertEquals("ANIME#JIKAN#21", result.getSk());
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
    void refreshLatestEpisode_ShouldSkipSaveWhenDetailsNull() {
        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getDetails("21")).thenReturn(null);

        AnimeItem result = animeService.refreshLatestEpisode("21");

        assertNotNull(result);
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshLatestEpisode_ShouldSkipSaveWhenTotalEpisodesNull() {
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(null).build();

        when(animeRepository.findById("21")).thenReturn(testItem);
        when(animeMetadataClient.getDetails("21")).thenReturn(details);

        AnimeItem result = animeService.refreshLatestEpisode("21");

        assertNotNull(result);
        verify(animeRepository, never()).save(any());
    }

    @Test
    void refreshLatestEpisode_ShouldThrowWhenNotFound() {
        when(animeRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                animeService.refreshLatestEpisode("notreal"));

        assertEquals("Anime not found: notreal", ex.getMessage());
    }

    @Test
    void getCommunityRating_ShouldDelegateToClient() {
        when(animeMetadataClient.getCommunityRating("21")).thenReturn(8.7);

        Double result = animeService.getCommunityRating("21");

        assertEquals(8.7, result);
        verify(animeMetadataClient, times(1)).getCommunityRating("21");
    }

    @Test
    void getCommunityRating_ShouldReturnNullWhenUnavailable() {
        when(animeMetadataClient.getCommunityRating("21")).thenReturn(null);

        Double result = animeService.getCommunityRating("21");

        assertNull(result);
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
        AnimeSearchResult details = AnimeSearchResult.builder()
                .id("21").totalEpisodes(null).build();

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
}