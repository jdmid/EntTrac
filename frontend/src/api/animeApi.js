import client from './client'

export const searchAnime = (query) =>
  client.get('/anime/search', { params: { q: query } })

export const getAnimeLibrary = () =>
  client.get('/anime/library')

export const getAnime = (animeId) =>
  client.get(`/anime/library/${animeId}`)

export const addAnimeToLibrary = (animeItem) =>
  client.post('/anime/library', animeItem)

export const updateAnimeProgress = (animeId, episodesWatched) =>
  client.patch(`/anime/library/${animeId}/progress`, null, {
    params: { episodesWatched },
  })

export const updateAnimeScore = (animeId, score) =>
  client.patch(`/anime/library/${animeId}/score`, null, {
    params: { score },
  })

export const updateAnimeStatus = (animeId, status) =>
  client.patch(`/anime/library/${animeId}/status`, null, {
    params: { status },
  })

export const refreshLatestEpisode = (animeId) =>
  client.post(`/anime/library/${animeId}/refresh`)

export const removeAnimeFromLibrary = (animeId) =>
  client.delete(`/anime/library/${animeId}`)

export const getAnimeDetails = (animeId) =>
  client.get(`/anime/details/${animeId}`)

export const getAnimeCommunityRating = (animeId) =>
  client.get(`/anime/library/${animeId}/rating`)