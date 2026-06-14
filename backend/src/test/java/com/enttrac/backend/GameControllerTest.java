package com.enttrac.backend;

import com.enttrac.backend.controller.GameController;
import com.enttrac.backend.model.item.GameItem;
import com.enttrac.backend.model.result.GameSearchResult;
import com.enttrac.backend.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void search_ShouldReturnResults() throws Exception {
        GameSearchResult result = GameSearchResult.builder()
                .id("1942")
                .title("Skyrim")
                .build();

        when(gameService.search("skyrim")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/games/search").param("q", "skyrim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Skyrim"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setTitle("Skyrim");

        when(gameService.getLibrary()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/games/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameId").value("1942"));
    }

    @Test
    void getGame_ShouldReturnItemWhenFound() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setTitle("Skyrim");

        when(gameService.getGame("1942")).thenReturn(item);

        mockMvc.perform(get("/api/games/library/1942"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("1942"));
    }

    @Test
    void getGame_ShouldReturn404WhenNotFound() throws Exception {
        when(gameService.getGame("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/games/library/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setTitle("Skyrim");
        item.setStatus("PLANNED");

        when(gameService.addToLibrary(any())).thenReturn(item);

        mockMvc.perform(post("/api/games/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("1942"));
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/games/library/1942"))
                .andExpect(status().isNoContent());

        verify(gameService, times(1)).removeFromLibrary("1942");
    }

    @Test
    void updateProgress_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setHoursPlayed(42);

        when(gameService.updateProgress("1942", 42)).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/progress")
                        .param("hoursPlayed", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hoursPlayed").value(42));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setScore(9);

        when(gameService.updateScore("1942", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/score")
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/games/library/1942/score")
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setStatus("FINISHED");

        when(gameService.updateStatus("1942", "FINISHED")).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/status")
                        .param("status", "FINISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/games/library/1942/status")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserPlatform_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setUserPlatform("PC");

        when(gameService.updateUserPlatform("1942", "PC")).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/platform")
                        .param("userPlatform", "PC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userPlatform").value("PC"));
    }

    @Test
    void updateOwnedDlc_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setOwnedDlcIds(List.of("dlc1", "dlc2"));

        when(gameService.updateOwnedDlc("1942", List.of("dlc1", "dlc2"))).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/dlc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("dlc1", "dlc2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedDlcIds[0]").value("dlc1"))
                .andExpect(jsonPath("$.ownedDlcIds[1]").value("dlc2"));
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");

        when(gameService.updateNotes("1942", "Great game")).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/notes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great game"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");

        when(gameService.updateNotes("1942", "")).thenReturn(item);

        mockMvc.perform(patch("/api/games/library/1942/notes"))
                .andExpect(status().isOk());
    }

    @Test
    void enrich_ShouldReturnEnrichedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setIgdbRating(87.5);

        when(gameService.enrichIgdbRating("1942")).thenReturn(item);

        mockMvc.perform(post("/api/games/library/1942/enrich"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.igdbRating").value(87.5));
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        GameItem item = new GameItem();
        item.setGameId("1942");
        item.setIgdbRating(87.5);
        item.setIgdbCriticRating(92.0);

        when(gameService.refreshRatings("1942")).thenReturn(item);

        mockMvc.perform(post("/api/games/library/1942/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.igdbRating").value(87.5))
                .andExpect(jsonPath("$.igdbCriticRating").value(92.0));
    }

    @Test
    void getDetails_ShouldReturnResultWhenFound() throws Exception {
        GameSearchResult result = GameSearchResult.builder()
                .id("1942")
                .title("Skyrim")
                .build();

        when(gameService.getDetails("1942")).thenReturn(result);

        mockMvc.perform(get("/api/games/details/1942"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Skyrim"));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(gameService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/games/details/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWorksByDeveloper_ShouldReturnResults() throws Exception {
        GameSearchResult result = GameSearchResult.builder()
                .id("1942")
                .title("Skyrim")
                .build();

        when(gameService.getWorksByDeveloper("1234")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/games/creator/1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Skyrim"));
    }
}
