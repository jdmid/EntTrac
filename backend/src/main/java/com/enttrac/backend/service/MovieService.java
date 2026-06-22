package com.enttrac.backend.service;

import com.enttrac.backend.client.OmdbClient;
import com.enttrac.backend.client.TmdbMovieClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.MovieItem;
import com.enttrac.backend.model.result.MovieSearchResult;
import com.enttrac.backend.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final TmdbMovieClient tmdbMovieClient;
    private final OmdbClient omdbClient;

    public MovieService(MovieRepository movieRepository,
                        @Qualifier("tmdbMovieClient") TmdbMovieClient tmdbMovieClient,
                        OmdbClient omdbClient) {
        this.movieRepository = movieRepository;
        this.tmdbMovieClient = tmdbMovieClient;
        this.omdbClient = omdbClient;
    }

    public List<MovieSearchResult> search(String query) {
        log.info("Searching movies: {}", query);
        return tmdbMovieClient.search(query);
    }

    public MovieSearchResult getDetails(String id) {
        log.info("Fetching movie details: {}", id);
        MovieSearchResult result = tmdbMovieClient.getDetails(id);
        if (result != null && result.getImdbId() != null) {
            omdbClient.enrichWithRatings(result, result.getImdbId());
        }
        return result;
    }

    public List<MovieItem> getLibrary() {
        return movieRepository.findAll();
    }

    public MovieItem getMovie(String movieId) {
        return movieRepository.findById(movieId);
    }

    public MovieItem addToLibrary(MovieItem item) {
        MovieItem existing = movieRepository.findById(item.getMovieId());
        if (existing != null) {
            log.info("Movie already in library, skipping add: {}", item.getMovieId());
            return existing;
        }
        item.setPk("USER#default");
        item.setSk("MOVIE#TMDB#" + item.getMovieId());
        String now = Instant.now().toString();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        movieRepository.save(item);
        log.info("Added movie to library: {}", item.getMovieId());
        return item;
    }

    public MovieItem updateScore(String movieId, int score) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) {
            throw new NotFoundException("Movie not found: " + movieId);
        }
        item.setScore(score);
        item.setUpdatedAt(Instant.now().toString());
        movieRepository.save(item);
        log.info("Updated movie score: {} -> {}", movieId, score);
        return item;
    }

    public MovieItem updateStatus(String movieId, String status) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) {
            throw new NotFoundException("Movie not found: " + movieId);
        }
        item.setStatus(status);
        item.setUpdatedAt(Instant.now().toString());
        movieRepository.save(item);
        log.info("Updated movie status: {} -> {}", movieId, status);
        return item;
    }

    public MovieItem updateNotes(String movieId, String notes) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) {
            throw new NotFoundException("Movie not found: " + movieId);
        }
        item.setNotes(notes);
        item.setUpdatedAt(Instant.now().toString());
        movieRepository.save(item);
        log.info("Updated movie notes: {}", movieId);
        return item;
    }

    public MovieItem refreshRatings(String movieId) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) {
            throw new NotFoundException("Movie not found: " + movieId);
        }

        // Refresh TMDB metadata
        MovieSearchResult details = tmdbMovieClient.getDetails(movieId);
        if (details != null) {
            // Refresh OMDB ratings using imdbId from fresh TMDB call
            if (details.getImdbId() != null) {
                omdbClient.enrichWithRatings(details, details.getImdbId());
                item.setImdbRating(details.getImdbRating());
                item.setRottenTomatoesRating(details.getRottenTomatoesRating());
                item.setMetacriticRating(details.getMetacriticRating());
            }
            item.setSeriesStatus(normalizeMovieStatus(details.getStatus()));
            item.setLastRefreshed(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
            movieRepository.save(item);
            log.info("Refreshed ratings for movie: {}", movieId);
        }
        return item;
    }

    public MovieItem enrichFromCache(String movieId) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) throw new NotFoundException("Movie not found: " + movieId);

        boolean hasCachedRatings = item.getImdbRating() != null
                || item.getRottenTomatoesRating() != null
                || item.getMetacriticRating() != null;
        boolean hasTmdbRating = item.getTmdbRating() != null;

        if (!hasCachedRatings || !hasTmdbRating) {
            MovieSearchResult details = tmdbMovieClient.getDetails(movieId);
            if (details != null) {
                if (!hasCachedRatings && details.getImdbId() != null) {
                    omdbClient.enrichWithRatings(details, details.getImdbId());
                    item.setImdbRating(details.getImdbRating());
                    item.setRottenTomatoesRating(details.getRottenTomatoesRating());
                    item.setMetacriticRating(details.getMetacriticRating());
                }
                if (!hasTmdbRating && details.getCommunityRating() != null) {
                    item.setTmdbRating(details.getCommunityRating());
                }
                item.setUpdatedAt(Instant.now().toString());
                movieRepository.save(item);
                log.info("Enriched movie {} from cache (imdb/rt/metacritic: {}, tmdb: {})",
                        movieId, !hasCachedRatings, !hasTmdbRating);            }
        }

        return item;
    }

    public void removeFromLibrary(String movieId) {
        movieRepository.delete(movieId);
    }

    private String normalizeMovieStatus(String rawStatus) {
        if (rawStatus == null) return null;
        String status = rawStatus.toLowerCase().trim();
        if (status.equals("released")) return "released";
        if (status.equals("in production")) return "in production";
        if (status.equals("post production")) return "in production";
        if (status.equals("planned")) return "upcoming";
        if (status.equals("rumored")) return "upcoming";
        if (status.equals("canceled")) return "cancelled";
        return null;
    }

    public List<MovieSearchResult> getWorksByPerson(String personId) {
        return tmdbMovieClient.getWorksByCreator(personId);
    }

    public List<Map<String, String>> searchPeople(String name) {
        return tmdbMovieClient.searchCreators(name);
    }

    public MovieItem enrichWatchProviders(String movieId, String region) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) throw new NotFoundException("Movie not found: " + movieId);

        boolean needsRefresh = item.getWatchProvidersRefreshedAt() == null
                || Instant.now().isAfter(
                Instant.parse(item.getWatchProvidersRefreshedAt())
                        .plus(Duration.ofDays(7)));

        if (needsRefresh) {
            List<String> providers =
                    tmdbMovieClient.getWatchProviders(movieId, region);
            item.setWatchProviders(providers);
            item.setWatchProvidersRefreshedAt(Instant.now().toString());
            item.setUpdatedAt(Instant.now().toString());
            movieRepository.save(item);
            log.info("Enriched movie {} with {} watch providers for region {}",
                    movieId, providers.size(), region);
        }

        return item;
    }
}