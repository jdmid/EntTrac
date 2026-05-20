package com.enttrac.backend.service;

import com.enttrac.backend.client.OmdbClient;
import com.enttrac.backend.client.TmdbMovieClient;
import com.enttrac.backend.config.NotFoundException;
import com.enttrac.backend.model.item.MovieItem;
import com.enttrac.backend.model.result.MovieSearchResult;
import com.enttrac.backend.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
        return tmdbMovieClient.search(query);
    }

    public MovieSearchResult getDetails(String id) {
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
            return existing;
        }
        item.setPk("USER#default");
        item.setSk("MOVIE#TMDB#" + item.getMovieId());
        String now = Instant.now().toString();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        movieRepository.save(item);
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
            // Refresh OMDB scores using imdbId from fresh TMDB call
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
        }
        return item;
    }

    public MovieItem enrichFromCache(String movieId) {
        MovieItem item = movieRepository.findById(movieId);
        if (item == null) {
            throw new NotFoundException("Movie not found: " + movieId);
        }

        // Only call OMDB if scores are not already cached
        boolean hasCachedRatings = item.getImdbRating() != null
                || item.getRottenTomatoesRating() != null
                || item.getMetacriticRating() != null;

        if (!hasCachedRatings) {
            MovieSearchResult details = tmdbMovieClient.getDetails(movieId);
            if (details != null && details.getImdbId() != null) {
                omdbClient.enrichWithRatings(details, details.getImdbId());
                item.setImdbRating(details.getImdbRating());
                item.setRottenTomatoesRating(details.getRottenTomatoesRating());
                item.setMetacriticRating(details.getMetacriticRating());
                item.setUpdatedAt(Instant.now().toString());
                movieRepository.save(item);
            }
        }

        return item;
    }

    public void removeFromLibrary(String movieId) {
        movieRepository.delete(movieId);
    }

    public Double getCommunityRating(String movieId) {
        return tmdbMovieClient.getCommunityRating(movieId);
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
}