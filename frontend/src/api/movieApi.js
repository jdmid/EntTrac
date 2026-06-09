import client from './client'

export const searchMovies = (query) =>
  client.get('/movies/search', { params: { q: query } })

export const getMovieLibrary = () =>
  client.get('/movies/library')

export const getMovie = (movieId) =>
  client.get(`/movies/library/${movieId}`)

export const addMovieToLibrary = (movieItem) =>
  client.post('/movies/library', movieItem)

export const updateMovieScore = (movieId, score) =>
  client.patch(`/movies/library/${movieId}/score`, null, {
    params: { score },
  })

export const updateMovieStatus = (movieId, status) =>
  client.patch(`/movies/library/${movieId}/status`, null, {
    params: { status },
  })

export const updateMovieNotes = (movieId, notes) =>
  client.patch(`/movies/library/${movieId}/notes`, notes, {
    headers: { 'Content-Type': 'text/plain' },
  })

export const refreshMovieRatings = (movieId) =>
  client.post(`/movies/library/${movieId}/refresh`)

export const enrichMovieFromCache = (movieId) =>
  client.post(`/movies/library/${movieId}/enrich`)

export const removeMovieFromLibrary = (movieId) =>
  client.delete(`/movies/library/${movieId}`)

export const getMovieDetails = (movieId) =>
  client.get(`/movies/details/${movieId}`)

export const getWorksByDirector = (directorId) =>
  client.get(`/movies/creator/${directorId}`)

export const searchPeople = (name) =>
  client.get('/movies/person-search', { params: { name } })