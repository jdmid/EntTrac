package com.enttrac.backend;

import com.enttrac.backend.controller.MovieController;
import com.enttrac.backend.model.item.MovieItem;
import com.enttrac.backend.model.result.MovieSearchResult;
import com.enttrac.backend.service.MovieService;
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

@WebMvcTest(MovieController.class)
public class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void search_ShouldReturnResults() throws Exception {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("550")
                .title("Fight Club")
                .build();

        when(movieService.search("fight club")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/movies/search").param("q", "fight club"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Fight Club"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setTitle("Fight Club");

        when(movieService.getLibrary()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/movies/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieId").value("550"));
    }

    @Test
    void getMovie_ShouldReturnItemWhenFound() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setTitle("Fight Club");

        when(movieService.getMovie("550")).thenReturn(item);

        mockMvc.perform(get("/api/movies/library/550"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId").value("550"));
    }

    @Test
    void getMovie_ShouldReturn404WhenNotFound() throws Exception {
        when(movieService.getMovie("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/movies/library/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setTitle("Fight Club");
        item.setStatus("PLANNED");

        when(movieService.addToLibrary(any())).thenReturn(item);

        mockMvc.perform(post("/api/movies/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId").value("550"));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setScore(9);

        when(movieService.updateScore("550", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/movies/library/550/score")
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/movies/library/550/score")
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setStatus("FINISHED");

        when(movieService.updateStatus("550", "FINISHED")).thenReturn(item);

        mockMvc.perform(patch("/api/movies/library/550/status")
                        .param("status", "FINISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/movies/library/550/status")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");

        when(movieService.updateNotes("550", "Great movie")).thenReturn(item);

        mockMvc.perform(patch("/api/movies/library/550/notes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great movie"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");

        when(movieService.updateNotes("550", "")).thenReturn(item);

        mockMvc.perform(patch("/api/movies/library/550/notes"))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_ShouldReturnUpdatedItem() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setImdbRating(8.8);
        item.setRottenTomatoesRating("89%");

        when(movieService.refreshRatings("550")).thenReturn(item);

        mockMvc.perform(post("/api/movies/library/550/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imdbRating").value(8.8))
                .andExpect(jsonPath("$.rottenTomatoesRating").value("89%"));
    }

    @Test
    void enrich_ShouldReturnEnrichedItem() throws Exception {
        MovieItem item = new MovieItem();
        item.setMovieId("550");
        item.setImdbRating(8.8);
        item.setTmdbRating(7.8);

        when(movieService.enrichFromCache("550")).thenReturn(item);

        mockMvc.perform(post("/api/movies/library/550/enrich"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imdbRating").value(8.8))
                .andExpect(jsonPath("$.tmdbRating").value(7.8));
    }

    @Test
    void getDetails_ShouldReturnResultWhenFound() throws Exception {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("550")
                .title("Fight Club")
                .imdbRating(8.8)
                .rottenTomatoesRating("89%")
                .build();

        when(movieService.getDetails("550")).thenReturn(result);

        mockMvc.perform(get("/api/movies/details/550"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Fight Club"))
                .andExpect(jsonPath("$.imdbRating").value(8.8))
                .andExpect(jsonPath("$.rottenTomatoesRating").value("89%"));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(movieService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/movies/details/notreal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/movies/library/550"))
                .andExpect(status().isNoContent());

        verify(movieService, times(1)).removeFromLibrary("550");
    }

    @Test
    void getWorksByPerson_ShouldReturnResults() throws Exception {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("490132")
                .title("The Grand Budapest Hotel")
                .build();

        when(movieService.getWorksByPerson("525")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/movies/creator/525"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Grand Budapest Hotel"));
    }
}