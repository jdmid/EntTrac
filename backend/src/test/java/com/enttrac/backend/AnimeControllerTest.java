package com.enttrac.backend.controller;

import com.enttrac.backend.model.item.AnimeItem;
import com.enttrac.backend.model.result.AnimeSearchResult;
import com.enttrac.backend.service.AnimeService;
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

@WebMvcTest(AnimeController.class)
public class AnimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnimeService animeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void search_ShouldReturnResults() throws Exception {
        AnimeSearchResult result = AnimeSearchResult.builder()
                .id("21")
                .title("One Piece")
                .build();

        when(animeService.search("one piece")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/anime/search").param("q", "one piece"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("One Piece"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.getLibrary()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/anime/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animeId").value("21"));
    }

    @Test
    void getAnime_ShouldReturnItemWhenFound() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.getAnime("21")).thenReturn(item);

        mockMvc.perform(get("/api/anime/library/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animeId").value("21"));
    }

    @Test
    void getAnime_ShouldReturn404WhenNotFound() throws Exception {
        when(animeService.getAnime("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/anime/library/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");
        item.setStatus("PLANNED");

        when(animeService.addToLibrary(any())).thenReturn(item);

        mockMvc.perform(post("/api/anime/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animeId").value("21"));
    }

    @Test
    void updateProgress_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setEpisodesWatched(12);

        when(animeService.updateProgress("21", 12)).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/progress")
                        .param("episodesWatched", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episodesWatched").value(12));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setScore(9);

        when(animeService.updateScore("21", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/score")
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/anime/library/21/score")
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setStatus("CONSUMING");

        when(animeService.updateStatus("21", "CONSUMING")).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/status")
                        .param("status", "CONSUMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONSUMING"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/anime/library/21/status")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTotalEpisodes(1000);

        when(animeService.refreshLatestEpisode("21")).thenReturn(item);

        mockMvc.perform(post("/api/anime/library/21/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEpisodes").value(1000));
    }

    @Test
    void getDetails_ShouldReturnResultWhenFound() throws Exception {
        AnimeSearchResult result = AnimeSearchResult.builder()
                .id("21")
                .title("One Piece")
                .build();

        when(animeService.getDetails("21")).thenReturn(result);

        mockMvc.perform(get("/api/anime/details/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("One Piece"));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(animeService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/anime/details/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommunityRating_ShouldReturnRatingWhenAvailable() throws Exception {
        when(animeService.getCommunityRating("21")).thenReturn(8.7);

        mockMvc.perform(get("/api/anime/library/21/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(8.7));
    }

    @Test
    void getCommunityRating_ShouldReturn204WhenUnavailable() throws Exception {
        when(animeService.getCommunityRating("21")).thenReturn(null);

        mockMvc.perform(get("/api/anime/library/21/rating"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/anime/library/21"))
                .andExpect(status().isNoContent());

        verify(animeService, times(1)).removeFromLibrary("21");
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");

        when(animeService.updateNotes("21", "Great anime")).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/notes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great anime"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");

        when(animeService.updateNotes("21", "")).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/notes"))
                .andExpect(status().isOk());
    }

    @Test
    void refreshAll_ShouldReturnUpdatedLibrary() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.refreshAll()).thenReturn(List.of(item));

        mockMvc.perform(post("/api/anime/library/refresh-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animeId").value("21"));
    }
}
