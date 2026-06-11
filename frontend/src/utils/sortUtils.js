export function sortManga(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'MOST_UNREAD':
      return arr.sort((a, b) => {
        const unreadA = (a.latestChapter ?? 0) - (a.chaptersRead ?? 0)
        const unreadB = (b.latestChapter ?? 0) - (b.chaptersRead ?? 0)
        return unreadB - unreadA
      })
    case 'ALPHA_AZ':
      return arr.sort((a, b) => a.title.localeCompare(b.title))
    case 'ALPHA_ZA':
      return arr.sort((a, b) => b.title.localeCompare(a.title))
    case 'SCORE_HIGH':
      return arr.sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    case 'SCORE_LOW':
      return arr.sort((a, b) => (a.score ?? 0) - (b.score ?? 0))
    case 'RECENTLY_UPDATED':
      return arr.sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
    default:
      return arr
  }
}

export function sortAnime(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'MOST_UNWATCHED':
      return arr.sort((a, b) => {
        const unwatchedA = (a.totalEpisodes ?? 0) - (a.episodesWatched ?? 0)
        const unwatchedB = (b.totalEpisodes ?? 0) - (b.episodesWatched ?? 0)
        return unwatchedB - unwatchedA
      })
    case 'ALPHA_AZ':
      return arr.sort((a, b) => a.title.localeCompare(b.title))
    case 'ALPHA_ZA':
      return arr.sort((a, b) => b.title.localeCompare(a.title))
    case 'SCORE_HIGH':
      return arr.sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    case 'SCORE_LOW':
      return arr.sort((a, b) => (a.score ?? 0) - (b.score ?? 0))
    case 'RECENTLY_UPDATED':
      return arr.sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
    default:
      return arr
  }
}

export function sortTv(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'MOST_UNREAD':
      return arr.sort((a, b) => {
        const unwatchedA = (a.totalEpisodes ?? 0) - (a.episodesWatched ?? 0)
        const unwatchedB = (b.totalEpisodes ?? 0) - (b.episodesWatched ?? 0)
        return unwatchedB - unwatchedA
      })
    case 'ALPHA_AZ':
      return arr.sort((a, b) => a.title.localeCompare(b.title))
    case 'ALPHA_ZA':
      return arr.sort((a, b) => b.title.localeCompare(a.title))
    case 'SCORE_HIGH':
      return arr.sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    case 'SCORE_LOW':
      return arr.sort((a, b) => (a.score ?? 0) - (b.score ?? 0))
    case 'RECENTLY_UPDATED':
      return arr.sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
    default:
      return arr
  }
}

export function sortMovies(items, sortBy) {
  const arr = [...items]
  switch (sortBy) {
    case 'ALPHA_AZ':
      return arr.sort((a, b) => a.title.localeCompare(b.title))
    case 'ALPHA_ZA':
      return arr.sort((a, b) => b.title.localeCompare(a.title))
    case 'SCORE_HIGH':
      return arr.sort((a, b) => (b.score ?? 0) - (a.score ?? 0))
    case 'SCORE_LOW':
      return arr.sort((a, b) => (a.score ?? 0) - (b.score ?? 0))
    case 'RECENTLY_UPDATED':
      return arr.sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''))
    case 'RECENTLY_ADDED':
      return arr.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
    default:
      return arr
  }
}