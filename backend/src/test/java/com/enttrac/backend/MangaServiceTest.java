package com.enttrac.backend;

import com.enttrac.backend.client.MangaMetadataClient;
import com.enttrac.backend.model.MangaItem;
import com.enttrac.backend.model.MangaSearchResult;
import com.enttrac.backend.repository.MangaRepository;
import com.enttrac.backend.service.MangaService;
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
public class MangaServiceTest {

    @Mock
    private MangaRepository mangaRepository;

    @Mock
    private MangaMetadataClient mangaMetadataClient;

    @InjectMocks
    private MangaService mangaService;

    private MangaItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new MangaItem();
        testItem.setMangaId("abc123");
        testItem.setTitle("Test Manga");
        testItem.setStatus("READING");
        testItem.setChaptersRead(10);
        testItem.setLatestChapter(50);
    }

    @Test
    void addToLibrary_ShouldSetPkAndSk() {
        MangaItem result = mangaService.addToLibrary(testItem);

        assertEquals("USER#default", result.getPk());
        assertEquals("MANGA#abc123", result.getSk());
        verify(mangaRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldUpdateChaptersRead() {
        when(mangaRepository.findById("abc123")).thenReturn(testItem);

        MangaItem result = mangaService.updateProgress("abc123", 25);

        assertEquals(25, result.getChaptersRead());
        verify(mangaRepository, times(1)).save(testItem);
    }

    @Test
    void updateProgress_ShouldThrowWhenMangaNotFound() {
        when(mangaRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                mangaService.updateProgress("notreal", 25));

        assertEquals("Manga not found: notreal", ex.getMessage());
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(mangaRepository.findAll()).thenReturn(List.of(testItem));

        List<MangaItem> result = mangaService.getLibrary();

        assertEquals(1, result.size());
        assertEquals("Test Manga", result.get(0).getTitle());
    }

    @Test
    void search_ShouldDelegateToClient() {
        MangaSearchResult searchResult = MangaSearchResult.builder()
                .id("abc123")
                .title("Test Manga")
                .build();
        when(mangaMetadataClient.search("test")).thenReturn(List.of(searchResult));

        List<MangaSearchResult> results = mangaService.search("test");

        assertEquals(1, results.size());
        assertEquals("Test Manga", results.get(0).getTitle());
        verify(mangaMetadataClient, times(1)).search("test");
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        mangaService.removeFromLibrary("abc123");
        verify(mangaRepository, times(1)).delete("abc123");
    }

    @Test
    void getDetails_ShouldDelegateToClient() {
        MangaSearchResult searchResult = MangaSearchResult.builder()
                .id("abc123")
                .title("One Piece")
                .build();
        when(mangaMetadataClient.getDetails("abc123")).thenReturn(searchResult);

        MangaSearchResult result = mangaService.getDetails("abc123");

        assertEquals("One Piece", result.getTitle());
        verify(mangaMetadataClient, times(1)).getDetails("abc123");
    }

    @Test
    void getManga_ShouldReturnItemFromRepository() {
        when(mangaRepository.findById("abc123")).thenReturn(testItem);

        MangaItem result = mangaService.getManga("abc123");

        assertEquals("Test Manga", result.getTitle());
        verify(mangaRepository, times(1)).findById("abc123");
    }

    @Test
    void refreshLatestChapter_ShouldUpdateWhenDetailsAvailable() {
        MangaSearchResult details = MangaSearchResult.builder()
                .id("abc123")
                .latestChapter(75)
                .build();

        when(mangaRepository.findById("abc123")).thenReturn(testItem);
        when(mangaMetadataClient.getDetails("abc123")).thenReturn(details);

        MangaItem result = mangaService.refreshLatestChapter("abc123");

        assertEquals(75, result.getLatestChapter());
        verify(mangaRepository, times(1)).save(testItem);
    }

    @Test
    void refreshLatestChapter_ShouldThrowWhenNotFound() {
        when(mangaRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                mangaService.refreshLatestChapter("notreal"));

        assertEquals("Manga not found: notreal", ex.getMessage());
    }
}