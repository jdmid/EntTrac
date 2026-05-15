import client from './client'

export const searchManga = (query) =>
  client.get(`/manga/search`, { params: { q: query } })

export const getLibrary = () =>
  client.get('/manga/library')

export const getManga = (mangaId) =>
  client.get(`/manga/library/${mangaId}`)

export const addToLibrary = (mangaItem) =>
  client.post('/manga/library', mangaItem)

export const updateProgress = (mangaId, chaptersRead) =>
  client.patch(`/manga/library/${mangaId}/progress`, null, {
    params: { chaptersRead },
  })

export const updateStatus = (mangaId, status) =>
  client.patch(`/manga/library/${mangaId}/status`, null, {
    params: { status },
  })

export const updateScore = (mangaId, score) =>
  client.patch(`/manga/library/${mangaId}/score`, null, {
    params: { score },
  })

export const refreshLatestChapter = (mangaId) =>
  client.post(`/manga/library/${mangaId}/refresh`)

export const removeFromLibrary = (mangaId) =>
  client.delete(`/manga/library/${mangaId}`)