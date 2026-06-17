import client from './client'

export const searchGames = (query) =>
  client.get('/games/search', { params: { q: query } })

export const getGameLibrary = () =>
  client.get('/games/library')

export const getGame = (gameId) =>
  client.get(`/games/library/${gameId}`)

export const addGameToLibrary = (gameItem) =>
  client.post('/games/library', gameItem)

export const updateGameProgress = (gameId, hoursPlayed) =>
  client.patch(`/games/library/${gameId}/progress`, null, {
    params: { hoursPlayed },
  })

export const updateGameScore = (gameId, score) =>
  client.patch(`/games/library/${gameId}/score`, null, {
    params: { score },
  })

export const updateGameStatus = (gameId, status) =>
  client.patch(`/games/library/${gameId}/status`, null, {
    params: { status },
  })

export const updateUserPlatform = (gameId, userPlatform) =>
  client.patch(`/games/library/${gameId}/platform`, null, {
    params: { userPlatform },
  })

export const updateOwnedDlc = (gameId, ownedDlcIds) =>
  client.patch(`/games/library/${gameId}/dlc`, ownedDlcIds)

export const updateGameNotes = (gameId, notes) =>
  client.patch(`/games/library/${gameId}/notes`, notes, {
    headers: { 'Content-Type': 'text/plain' },
  })

export const enrichGameFromCache = (gameId) =>
  client.post(`/games/library/${gameId}/enrich`)

export const refreshGameRatings = (gameId) =>
  client.post(`/games/library/${gameId}/refresh`)

export const removeGameFromLibrary = (gameId) =>
  client.delete(`/games/library/${gameId}`)

export const getGameDetails = (gameId) =>
  client.get(`/games/details/${gameId}`)

export const getWorksByDeveloper = (companyId) =>
  client.get(`/games/creator/${companyId}`)

export const searchDevelopers = (name) =>
  client.get('/games/developer-search', { params: { name } })