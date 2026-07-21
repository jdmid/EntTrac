package com.enttrac.backend;

import com.enttrac.backend.auth.AuthFilter;
import com.enttrac.backend.auth.CurrentUserIdArgumentResolver;
import com.enttrac.backend.auth.JwtService;
import com.enttrac.backend.config.WebMvcConfig;
import com.enttrac.backend.controller.AnimeController;
import com.enttrac.backend.model.item.AnimeItem;
import com.enttrac.backend.model.result.AnimeSearchResult;
import com.enttrac.backend.service.AnimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.enttrac.backend.auth.AuthTestSupport.TEST_USER_ID;
import static com.enttrac.backend.auth.AuthTestSupport.accessTokenCookie;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimeController.class)
@Import({AuthFilter.class, CurrentUserIdArgumentResolver.class, WebMvcConfig.class, JwtService.class})
public class AnimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
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

        mockMvc.perform(get("/api/anime/search")
                        .cookie(accessTokenCookie(jwtService))
                        .param("q", "one piece"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("One Piece"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.getLibrary(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/anime/library").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animeId").value("21"));
    }

    @Test
    void getLibrary_ShouldReturn401_WhenNoCookiePresent() throws Exception {
        mockMvc.perform(get("/api/anime/library"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAnime_ShouldReturnItemWhenFound() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.getAnime(TEST_USER_ID, "21")).thenReturn(item);

        mockMvc.perform(get("/api/anime/library/21").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animeId").value("21"));
    }

    @Test
    void getAnime_ShouldReturn404WhenNotFound() throws Exception {
        when(animeService.getAnime(TEST_USER_ID, "notreal")).thenReturn(null);

        mockMvc.perform(get("/api/anime/library/notreal").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");
        item.setStatus("PLANNED");

        when(animeService.addToLibrary(eq(TEST_USER_ID), any())).thenReturn(item);

        mockMvc.perform(post("/api/anime/library")
                        .cookie(accessTokenCookie(jwtService))
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

        when(animeService.updateProgress(TEST_USER_ID, "21", 12)).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/progress")
                        .cookie(accessTokenCookie(jwtService))
                        .param("episodesWatched", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episodesWatched").value(12));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setScore(9);

        when(animeService.updateScore(TEST_USER_ID, "21", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/score")
                        .cookie(accessTokenCookie(jwtService))
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/anime/library/21/score")
                        .cookie(accessTokenCookie(jwtService))
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setStatus("CONSUMING");

        when(animeService.updateStatus(TEST_USER_ID, "21", "CONSUMING")).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/status")
                        .cookie(accessTokenCookie(jwtService))
                        .param("status", "CONSUMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONSUMING"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/anime/library/21/status")
                        .cookie(accessTokenCookie(jwtService))
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTotalEpisodes(1000);

        when(animeService.refreshLatestEpisode(TEST_USER_ID, "21")).thenReturn(item);

        mockMvc.perform(post("/api/anime/library/21/refresh").cookie(accessTokenCookie(jwtService)))
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

        mockMvc.perform(get("/api/anime/details/21").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("One Piece"));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(animeService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/anime/details/notreal").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/anime/library/21").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNoContent());

        verify(animeService, times(1)).removeFromLibrary(TEST_USER_ID, "21");
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");

        when(animeService.updateNotes(TEST_USER_ID, "21", "Great anime")).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/notes")
                        .cookie(accessTokenCookie(jwtService))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great anime"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");

        when(animeService.updateNotes(TEST_USER_ID, "21", "")).thenReturn(item);

        mockMvc.perform(patch("/api/anime/library/21/notes").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk());
    }

    @Test
    void refreshAll_ShouldReturnUpdatedLibrary() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.refreshAll(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(post("/api/anime/library/refresh-all").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animeId").value("21"));
    }

    @Test
    void refreshOngoing_ShouldReturnUpdatedLibrary() throws Exception {
        AnimeItem item = new AnimeItem();
        item.setAnimeId("21");
        item.setTitle("One Piece");

        when(animeService.refreshOngoing(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(post("/api/anime/library/refresh-ongoing").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animeId").value("21"));
    }
}
