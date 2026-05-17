package com.enttrac.backend.controller;

import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.service.TvService;
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

@WebMvcTest(TvController.class)
public class TvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TvService tvService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void search_ShouldReturnResults() throws Exception {
        TvSearchResult result = TvSearchResult.builder()
                .id("1396")
                .title("Breaking Bad")
                .build();

        when(tvService.search("breaking bad")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/tv/search").param("q", "breaking bad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Breaking Bad"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.getLibrary()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/tv/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tvId").value("1396"));
    }

    @Test
    void getTvShow_ShouldReturnItemWhenFound() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.getTvShow("1396")).thenReturn(item);

        mockMvc.perform(get("/api/tv/library/1396"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tvId").value("1396"));
    }

    @Test
    void getTvShow_ShouldReturn404WhenNotFound() throws Exception {
        when(tvService.getTvShow("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/tv/library/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");
        item.setStatus("PLANNED");

        when(tvService.addToLibrary(any())).thenReturn(item);

        mockMvc.perform(post("/api/tv/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tvId").value("1396"));
    }

    @Test
    void updateProgress_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setEpisodesWatched(5);
        item.setCurrentSeason(1);

        when(tvService.updateProgress("1396", 5, 1)).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/progress")
                        .param("episodesWatched", "5")
                        .param("currentSeason", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episodesWatched").value(5))
                .andExpect(jsonPath("$.currentSeason").value(1));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setScore(9);

        when(tvService.updateScore("1396", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/score")
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/tv/library/1396/score")
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setStatus("CONSUMING");

        when(tvService.updateStatus("1396", "CONSUMING")).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/status")
                        .param("status", "CONSUMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONSUMING"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/tv/library/1396/status")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");

        when(tvService.updateNotes("1396", "Great show")).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/notes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great show"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");

        when(tvService.updateNotes("1396", "")).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/notes"))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTotalEpisodes(62);

        when(tvService.refreshLatestEpisodes("1396")).thenReturn(item);

        mockMvc.perform(post("/api/tv/library/1396/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEpisodes").value(62));
    }

    @Test
    void getDetails_ShouldReturnResultWhenFound() throws Exception {
        TvSearchResult result = TvSearchResult.builder()
                .id("1396")
                .title("Breaking Bad")
                .build();

        when(tvService.getDetails("1396")).thenReturn(result);

        mockMvc.perform(get("/api/tv/details/1396"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Breaking Bad"));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(tvService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/tv/details/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommunityRating_ShouldReturnRatingWhenAvailable() throws Exception {
        when(tvService.getCommunityRating("1396")).thenReturn(9.5);

        mockMvc.perform(get("/api/tv/library/1396/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(9.5));
    }

    @Test
    void getCommunityRating_ShouldReturn204WhenUnavailable() throws Exception {
        when(tvService.getCommunityRating("1396")).thenReturn(null);

        mockMvc.perform(get("/api/tv/library/1396/rating"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/tv/library/1396"))
                .andExpect(status().isNoContent());

        verify(tvService, times(1)).removeFromLibrary("1396");
    }

    @Test
    void refreshAll_ShouldReturnUpdatedLibrary() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.refreshAll()).thenReturn(List.of(item));

        mockMvc.perform(post("/api/tv/library/refresh-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tvId").value("1396"));
    }
}
