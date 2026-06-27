package com.enttrac.backend;

import com.enttrac.backend.client.OmdbClient;
import com.enttrac.backend.client.TmdbMovieClient;
import com.enttrac.backend.model.item.MovieItem;
import com.enttrac.backend.model.result.MovieSearchResult;
import com.enttrac.backend.repository.MovieRepository;
import com.enttrac.backend.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TmdbMovieClient tmdbMovieClient;

    @Mock
    private OmdbClient omdbClient;

    @InjectMocks
    private MovieService movieService;

    private MovieItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new MovieItem();
        testItem.setMovieId("550");
        testItem.setTitle("Fight Club");
        testItem.setStatus("PLANNED");
        testItem.setDirectorId("525");
    }

    @Test
    void search_ShouldDelegateToClient() {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("550").title("Fight Club").build();
        when(tmdbMovieClient.search("fight club")).thenReturn(List.of(result));

        List<MovieSearchResult> results = movieService.search("fight club");

        assertEquals(1, results.size());
        verify(tmdbMovieClient, times(1)).search("fight club");
    }

    @Test
    void getDetails_ShouldEnrichWithOmdbWhenImdbIdAvailable() {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("550").title("Fight Club").imdbId("tt0137523").build();
        when(tmdbMovieClient.getDetails("550")).thenReturn(result);

        MovieSearchResult details = movieService.getDetails("550");

        assertNotNull(details);
        verify(omdbClient, times(1)).enrichWithRatings(result, "tt0137523");
    }

    @Test
    void getDetails_ShouldSkipOmdbWhenNoImdbId() {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("550").title("Fight Club").build();
        when(tmdbMovieClient.getDetails("550")).thenReturn(result);

        movieService.getDetails("550");

        verify(omdbClient, never()).enrichWithRatings(any(), any());
    }

    @Test
    void getLibrary_ShouldReturnAllItems() {
        when(movieRepository.findAll()).thenReturn(List.of(testItem));

        List<MovieItem> library = movieService.getLibrary();

        assertEquals(1, library.size());
        verify(movieRepository, times(1)).findAll();
    }

    @Test
    void getMovie_ShouldReturnItemWhenFound() {
        when(movieRepository.findById("550")).thenReturn(testItem);

        MovieItem result = movieService.getMovie("550");

        assertEquals("Fight Club", result.getTitle());
    }

    @Test
    void getMovie_ShouldReturnNullWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        MovieItem result = movieService.getMovie("notreal");

        assertNull(result);
    }

    @Test
    void addToLibrary_ShouldSetPkAndSk() {
        when(movieRepository.findById("550")).thenReturn(null);

        MovieItem result = movieService.addToLibrary(testItem);

        verify(movieRepository, times(1)).save(testItem);
        assertEquals("USER#default", result.getPk());
        assertEquals("MOVIE#TMDB#550", result.getSk());
        assertEquals("525", result.getDirectorId());
    }

    @Test
    void addToLibrary_ShouldReturnExistingWhenAlreadyInLibrary() {
        when(movieRepository.findById("550")).thenReturn(testItem);

        MovieItem result = movieService.addToLibrary(testItem);

        assertEquals(testItem, result);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateScore_ShouldUpdateScore() {
        when(movieRepository.findById("550")).thenReturn(testItem);

        MovieItem result = movieService.updateScore("550", 9);

        assertEquals(9, result.getScore());
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void updateScore_ShouldThrowWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                movieService.updateScore("notreal", 9));

        assertEquals("Movie not found: notreal", ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateStatus() {
        when(movieRepository.findById("550")).thenReturn(testItem);

        MovieItem result = movieService.updateStatus("550", "FINISHED");

        assertEquals("FINISHED", result.getStatus());
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void updateStatus_ShouldThrowWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                movieService.updateStatus("notreal", "FINISHED"));

        assertEquals("Movie not found: notreal", ex.getMessage());
    }

    @Test
    void updateNotes_ShouldUpdateNotes() {
        when(movieRepository.findById("550")).thenReturn(testItem);

        MovieItem result = movieService.updateNotes("550", "Great movie");

        assertEquals("Great movie", result.getNotes());
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void updateNotes_ShouldThrowWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                movieService.updateNotes("notreal", "notes"));

        assertEquals("Movie not found: notreal", ex.getMessage());
    }

    @Test
    void refreshRatings_ShouldUpdateRatingsAndSave() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550")
                .imdbId("tt0137523")
                .imdbRating(8.8)
                .rottenTomatoesRating("89%")
                .metacriticRating("79/100")
                .build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        doAnswer(invocation -> {
            MovieSearchResult r = invocation.getArgument(0);
            r.setImdbRating(8.8);
            r.setRottenTomatoesRating("89%");
            r.setMetacriticRating("79/100");
            return null;
        }).when(omdbClient).enrichWithRatings(any(), any());

        MovieItem result = movieService.refreshRatings("550");

        verify(movieRepository, times(1)).save(testItem);
        assertNotNull(result.getLastRefreshed());
        assertEquals(8.8, result.getImdbRating());
        assertEquals("89%", result.getRottenTomatoesRating());
        assertEquals("79/100", result.getMetacriticRating());
    }

    @Test
    void refreshRatings_ShouldThrowWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                movieService.refreshRatings("notreal"));

        assertEquals("Movie not found: notreal", ex.getMessage());
    }

    @Test
    void enrichFromCache_ShouldCallOmdbWhenNoRatingsCached() {
        when(movieRepository.findById("550")).thenReturn(testItem);
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").imdbId("tt0137523").tmdbRating(7.8).build();
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        movieService.enrichFromCache("550");

        verify(omdbClient, times(1)).enrichWithRatings(any(), eq("tt0137523"));
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void enrichFromCache_ShouldCacheTmdbRatingWhenNull() {
        when(movieRepository.findById("550")).thenReturn(testItem);
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").imdbId("tt0137523").tmdbRating(7.8).build();
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.enrichFromCache("550");

        assertEquals(7.8, result.getTmdbRating());
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void enrichFromCache_ShouldSkipOmdbWhenRatingsCached() {
        testItem.setImdbRating(8.8);
        testItem.setTmdbRating(7.8);
        when(movieRepository.findById("550")).thenReturn(testItem);

        movieService.enrichFromCache("550");

        verify(omdbClient, never()).enrichWithRatings(any(), any());
        verify(tmdbMovieClient, never()).getDetails(any());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichFromCache_ShouldSkipTmdbRatingWhenAlreadyCached() {
        testItem.setTmdbRating(7.8);
        when(movieRepository.findById("550")).thenReturn(testItem);
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").imdbId("tt0137523").build();
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.enrichFromCache("550");

        assertEquals(7.8, result.getTmdbRating());
        verify(omdbClient, times(1)).enrichWithRatings(any(), eq("tt0137523"));
    }

    @Test
    void enrichFromCache_ShouldThrowWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                movieService.enrichFromCache("notreal"));

        assertEquals("Movie not found: notreal", ex.getMessage());
    }

    @Test
    void removeFromLibrary_ShouldCallDelete() {
        movieService.removeFromLibrary("550");

        verify(movieRepository, times(1)).delete("550");
    }

    @Test
    void refreshRatings_ShouldNormalizeReleasedStatus() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").status("released").imdbId("tt0137523").build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.refreshRatings("550");

        assertEquals("released", result.getSeriesStatus());
    }

    @Test
    void refreshRatings_ShouldNormalizeInProductionStatus() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").status("in production").imdbId("tt0137523").build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.refreshRatings("550");

        assertEquals("in production", result.getSeriesStatus());
    }

    @Test
    void refreshRatings_ShouldNormalizePostProductionStatus() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").status("post production").imdbId("tt0137523").build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.refreshRatings("550");

        assertEquals("in production", result.getSeriesStatus());
    }

    @Test
    void refreshRatings_ShouldNormalizePlannedStatus() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").status("planned").imdbId("tt0137523").build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.refreshRatings("550");

        assertEquals("upcoming", result.getSeriesStatus());
    }

    @Test
    void refreshRatings_ShouldNormalizeRumoredStatus() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").status("rumored").imdbId("tt0137523").build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.refreshRatings("550");

        assertEquals("upcoming", result.getSeriesStatus());
    }

    @Test
    void refreshRatings_ShouldNormalizeCanceledStatus() {
        MovieSearchResult details = MovieSearchResult.builder()
                .id("550").status("canceled").imdbId("tt0137523").build();

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getDetails("550")).thenReturn(details);

        MovieItem result = movieService.refreshRatings("550");

        assertEquals("cancelled", result.getSeriesStatus());
    }

    @Test
    void getWorksByPerson_ShouldDelegateToClient() {
        MovieSearchResult result = MovieSearchResult.builder()
                .id("490132")
                .title("The Grand Budapest Hotel")
                .build();
        when(tmdbMovieClient.getWorksByCreator("525")).thenReturn(List.of(result));

        List<MovieSearchResult> results = movieService.getWorksByPerson("525");

        assertEquals(1, results.size());
        assertEquals("The Grand Budapest Hotel", results.get(0).getTitle());
        verify(tmdbMovieClient, times(1)).getWorksByCreator("525");
    }

    @Test
    void enrichWatchProviders_ShouldFetchAndCacheWhenNull() {
        testItem.setWatchProviders(null);
        testItem.setWatchProvidersRefreshedAt(null);

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getWatchProviders("550", "US"))
                .thenReturn(List.of("Netflix", "Hulu"));

        MovieItem result = movieService.enrichWatchProviders("550", "US");

        assertEquals(2, result.getWatchProviders().size());
        assertTrue(result.getWatchProviders().contains("Netflix"));
        assertNotNull(result.getWatchProvidersRefreshedAt());
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void enrichWatchProviders_ShouldSkipWhenCacheStillValid() {
        testItem.setWatchProviders(List.of("Netflix"));
        testItem.setWatchProvidersRefreshedAt(
                Instant.now().minus(Duration.ofDays(1)).toString());

        when(movieRepository.findById("550")).thenReturn(testItem);

        MovieItem result = movieService.enrichWatchProviders("550", "US");

        assertEquals(1, result.getWatchProviders().size());
        verify(tmdbMovieClient, never()).getWatchProviders(any(), any());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichWatchProviders_ShouldRefetchWhenTtlExpired() {
        testItem.setWatchProviders(List.of("Netflix"));
        testItem.setWatchProvidersRefreshedAt(
                Instant.now().minus(Duration.ofDays(8)).toString());

        when(movieRepository.findById("550")).thenReturn(testItem);
        when(tmdbMovieClient.getWatchProviders("550", "US"))
                .thenReturn(List.of("Netflix", "Max"));

        MovieItem result = movieService.enrichWatchProviders("550", "US");

        assertEquals(2, result.getWatchProviders().size());
        assertTrue(result.getWatchProviders().contains("Max"));
        verify(movieRepository, times(1)).save(testItem);
    }

    @Test
    void enrichWatchProviders_ShouldThrowWhenNotFound() {
        when(movieRepository.findById("notreal")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                movieService.enrichWatchProviders("notreal", "US"));

        assertEquals("Movie not found: notreal", ex.getMessage());
    }
}