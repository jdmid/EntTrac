package com.enttrac.backend;

import com.enttrac.backend.model.MangaItem;
import com.enttrac.backend.model.MangaSearchResult;
import com.enttrac.backend.service.MangaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.enttrac.backend.controller.MangaController.class)
public class MangaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MangaService mangaService;

    @Test
    void search_ShouldReturnResults() throws Exception {
        MangaSearchResult result = MangaSearchResult.builder()
                .id("abc123")
                .title("One Piece")
                .status("ongoing")
                .build();

        when(mangaService.search("One Piece")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/manga/search")
                        .param("q", "One Piece"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("One Piece"))
                .andExpect(jsonPath("$[0].id").value("abc123"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setStatus("READING");
        item.setChaptersRead(100);

        when(mangaService.getLibrary()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/manga/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("One Piece"))
                .andExpect(jsonPath("$[0].chaptersRead").value(100));
    }

    @Test
    void getManga_ShouldReturn404WhenNotFound() throws Exception {
        when(mangaService.getManga("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/manga/library/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setStatus("READING");
        item.setChaptersRead(0);

        when(mangaService.addToLibrary(any(MangaItem.class))).thenReturn(item);

        mockMvc.perform(post("/api/manga/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("One Piece"));
    }

    @Test
    void updateProgress_ShouldReturnUpdatedItem() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setChaptersRead(150);

        when(mangaService.updateProgress("abc123", 150)).thenReturn(item);

        mockMvc.perform(patch("/api/manga/library/abc123/progress")
                        .param("chaptersRead", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chaptersRead").value(150));
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        doNothing().when(mangaService).removeFromLibrary("abc123");

        mockMvc.perform(delete("/api/manga/library/abc123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setLatestChapter(1105);

        when(mangaService.refreshLatestChapter("abc123")).thenReturn(item);

        mockMvc.perform(post("/api/manga/library/abc123/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestChapter").value(1105));
    }

    @Test
    void getManga_ShouldReturnItemWhenFound() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setStatus("READING");

        when(mangaService.getManga("abc123")).thenReturn(item);

        mockMvc.perform(get("/api/manga/library/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("One Piece"));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setScore(9);

        when(mangaService.updateScore("abc123", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/manga/library/abc123/score")
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void addToLibrary_ShouldReturn400WhenStatusInvalid() throws Exception {
        MangaItem item = new MangaItem();
        item.setMangaId("abc123");
        item.setTitle("One Piece");
        item.setStatus("INVALID");
        item.setChaptersRead(0);

        mockMvc.perform(post("/api/manga/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnIllegalArgument() throws Exception {
        when(mangaService.getLibrary()).thenThrow(new IllegalArgumentException("Invalid argument"));

        mockMvc.perform(get("/api/manga/library"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid argument"));
    }

    @Test
    void shouldReturn500OnRuntimeException() throws Exception {
        when(mangaService.getLibrary()).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/api/manga/library"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Something went wrong"));
    }
}
