package com.enttrac.backend;

import com.enttrac.backend.auth.AuthFilter;
import com.enttrac.backend.auth.CurrentUserIdArgumentResolver;
import com.enttrac.backend.auth.JwtService;
import com.enttrac.backend.config.WebMvcConfig;
import com.enttrac.backend.controller.TvController;
import com.enttrac.backend.model.item.TvItem;
import com.enttrac.backend.model.result.TvSearchResult;
import com.enttrac.backend.service.TvService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.enttrac.backend.auth.AuthTestSupport.TEST_USER_ID;
import static com.enttrac.backend.auth.AuthTestSupport.accessTokenCookie;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TvController.class)
@Import({AuthFilter.class, CurrentUserIdArgumentResolver.class, WebMvcConfig.class, JwtService.class})
public class TvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
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

        mockMvc.perform(get("/api/tv/search").param("q", "breaking bad").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Breaking Bad"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.getLibrary(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/tv/library").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tvId").value("1396"));
    }

    @Test
    void getTvShow_ShouldReturnItemWhenFound() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.getTvShow(TEST_USER_ID,"1396")).thenReturn(item);

        mockMvc.perform(get("/api/tv/library/1396").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tvId").value("1396"));
    }

    @Test
    void getTvShow_ShouldReturn404WhenNotFound() throws Exception {
        when(tvService.getTvShow(TEST_USER_ID,"notreal")).thenReturn(null);

        mockMvc.perform(get("/api/tv/library/notreal").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");
        item.setStatus("PLANNED");

        when(tvService.addToLibrary(eq(TEST_USER_ID),any())).thenReturn(item);

        mockMvc.perform(post("/api/tv/library")
                        .cookie(accessTokenCookie(jwtService))
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

        when(tvService.updateProgress(TEST_USER_ID,"1396", 5, 1)).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/progress")
                        .cookie(accessTokenCookie(jwtService))
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

        when(tvService.updateScore(TEST_USER_ID,"1396", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/score")
                        .cookie(accessTokenCookie(jwtService))
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/tv/library/1396/score")
                        .cookie(accessTokenCookie(jwtService))
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setStatus("CONSUMING");

        when(tvService.updateStatus(TEST_USER_ID,"1396", "CONSUMING")).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/status")
                        .cookie(accessTokenCookie(jwtService))
                        .param("status", "CONSUMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONSUMING"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/tv/library/1396/status")
                        .cookie(accessTokenCookie(jwtService))
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");

        when(tvService.updateNotes(TEST_USER_ID,"1396", "Great show")).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/notes")
                        .cookie(accessTokenCookie(jwtService))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great show"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");

        when(tvService.updateNotes(TEST_USER_ID,"1396", "")).thenReturn(item);

        mockMvc.perform(patch("/api/tv/library/1396/notes").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTotalEpisodes(62);

        when(tvService.refreshLatestEpisodes(TEST_USER_ID,"1396")).thenReturn(item);

        mockMvc.perform(post("/api/tv/library/1396/refresh").cookie(accessTokenCookie(jwtService)))
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

        mockMvc.perform(get("/api/tv/details/1396").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Breaking Bad"));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(tvService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/tv/details/notreal").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/tv/library/1396").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNoContent());

        verify(tvService, times(1)).removeFromLibrary(TEST_USER_ID,"1396");
    }

    @Test
    void refreshAll_ShouldReturnUpdatedLibrary() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.refreshAll(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(post("/api/tv/library/refresh-all").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tvId").value("1396"));
    }

    @Test
    void refreshOngoing_ShouldReturnUpdatedLibrary() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTitle("Breaking Bad");

        when(tvService.refreshOngoing(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(post("/api/tv/library/refresh-ongoing").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tvId").value("1396"));
    }

    @Test
    void enrich_ShouldReturnEnrichedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setTmdbRating(9.5);

        when(tvService.enrichTmdbRating(TEST_USER_ID,"1396")).thenReturn(item);

        mockMvc.perform(post("/api/tv/library/1396/enrich").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tmdbRating").value(9.5));
    }

    @Test
    void getWorksByCreator_ShouldReturnResults() throws Exception {
        TvSearchResult result = TvSearchResult.builder()
                .id("1396")
                .title("Breaking Bad")
                .build();

        when(tvService.getWorksByCreator("66633")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/tv/creator/66633").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Breaking Bad"));
    }

    @Test
    void enrichWatchProviders_ShouldReturnEnrichedItem() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setWatchProviders(List.of("Netflix", "Hulu"));

        when(tvService.enrichWatchProviders(TEST_USER_ID,"1396", "US")).thenReturn(item);

        mockMvc.perform(post("/api/tv/library/1396/watch-providers")
                        .cookie(accessTokenCookie(jwtService))
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watchProviders[0]").value("Netflix"))
                .andExpect(jsonPath("$.watchProviders[1]").value("Hulu"));
    }

    @Test
    void enrichWatchProviders_ShouldUseDefaultRegionWhenNotProvided() throws Exception {
        TvItem item = new TvItem();
        item.setTvId("1396");
        item.setWatchProviders(List.of("Netflix"));

        when(tvService.enrichWatchProviders(TEST_USER_ID,"1396", "US")).thenReturn(item);

        mockMvc.perform(post("/api/tv/library/1396/watch-providers").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watchProviders[0]").value("Netflix"));
    }
}
