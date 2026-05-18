import client from './client'

export const searchTv = (query) =>
  client.get('/tv/search', { params: { q: query } })

export const getTvLibrary = () =>
  client.get('/tv/library')

export const getTvShow = (tvId) =>
  client.get(`/tv/library/${tvId}`)

export const addTvToLibrary = (tvItem) =>
  client.post('/tv/library', tvItem)

export const updateTvProgress = (tvId, episodesWatched, currentSeason) =>
  client.patch(`/tv/library/${tvId}/progress`, null, {
    params: { episodesWatched, currentSeason },
  })

export const updateTvScore = (tvId, score) =>
  client.patch(`/tv/library/${tvId}/score`, null, {
    params: { score },
  })

export const updateTvStatus = (tvId, status) =>
  client.patch(`/tv/library/${tvId}/status`, null, {
    params: { status },
  })

export const updateTvNotes = (tvId, notes) =>
  client.patch(`/tv/library/${tvId}/notes`, notes, {
    headers: { 'Content-Type': 'text/plain' },
  })

export const refreshTvEpisodes = (tvId) =>
  client.post(`/tv/library/${tvId}/refresh`)

export const removeFromTvLibrary = (tvId) =>
  client.delete(`/tv/library/${tvId}`)

export const getTvDetails = (tvId) =>
  client.get(`/tv/details/${tvId}`)

export const getTvCommunityRating = (tvId) =>
  client.get(`/tv/library/${tvId}/rating`)

export const refreshAllTv = () =>
  client.post('/tv/library/refresh-all')