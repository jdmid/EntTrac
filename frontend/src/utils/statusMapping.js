// Maps any API's series status to our normalized set
export const normalizeSeriesStatus = (apiStatus, medium) => {
  if (!apiStatus) return null
 
  const status = apiStatus.toLowerCase().trim()
 
  if (medium === 'manga') {
    // MangaDex values
    if (status === 'ongoing') return 'ongoing'
    if (status === 'completed') return 'finished'
    if (status === 'hiatus') return 'hiatus'
    if (status === 'cancelled') return 'cancelled'
  }
 
  if (medium === 'anime') {
    // Jikan/MAL values
    if (status === 'currently airing') return 'ongoing'
    if (status === 'finished airing') return 'finished'
    if (status === 'not yet aired') return 'upcoming'
  }
 
  if (medium === 'tv') {
    // TMDB TV values
    if (status === 'returning series') return 'ongoing'
    if (status === 'in production') return 'in production'
    if (status === 'pilot') return 'upcoming'
    if (status === 'planned') return 'upcoming'
    if (status === 'ended') return 'finished'
    if (status === 'canceled') return 'cancelled'
  }
 
  if (medium === 'movies') {
    // TMDB Movie values
    if (status === 'released') return 'released'
    if (status === 'in production') return 'in production'
    if (status === 'post production') return 'in production'
    if (status === 'planned') return 'upcoming'
    if (status === 'rumored') return 'upcoming'
    if (status === 'canceled') return 'cancelled'
  }
 
  // TODO: Books — add mapping once API is chosen (Open Library / Google Books)
  // if (medium === 'books') { ... }
 
  // TODO: Games — add mapping once API is chosen (IGDB / RAWG)
  // if (medium === 'games') { ... }
 
  return null
}
 
// Labels for the filter chips per medium
export const SERIES_STATUS_FILTERS = {
  manga: [
    { value: 'ALL',       label: 'All' },
    { value: 'ongoing',   label: 'Ongoing' },
    { value: 'finished',  label: 'Completed' },
    { value: 'hiatus',    label: 'Hiatus' },
    { value: 'cancelled', label: 'Cancelled' },
  ],
  anime: [
    { value: 'ALL',      label: 'All' },
    { value: 'ongoing',  label: 'Currently Airing' },
    { value: 'finished', label: 'Finished Airing' },
    { value: 'upcoming', label: 'Not Yet Aired' },
  ],
  tv: [
    { value: 'ALL',       label: 'All' },
    { value: 'ongoing',   label: 'Ongoing' },
    { value: 'finished',  label: 'Ended' },
    { value: 'upcoming',  label: 'Upcoming' },
    { value: 'in production',  label: 'In Production' },
    { value: 'cancelled', label: 'Cancelled' },
  ],
  movies: [
    { value: 'ALL',            label: 'All' },
    { value: 'released',       label: 'Released' },
    { value: 'in production',  label: 'In Production' },
    { value: 'upcoming',       label: 'Upcoming' },
    { value: 'cancelled',      label: 'Cancelled' },
  ],
  // TODO: Books — add filters once API is chosen
  books: [
    { value: 'ALL', label: 'All' },
  ],
  // TODO: Games — add filters once API is chosen
  games: [
    { value: 'ALL', label: 'All' },
  ],
}
 
export const SORT_OPTIONS = {
  manga: [
    { value: 'MOST_UNREAD',      label: 'Most unread' },
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  anime: [
    { value: 'MOST_UNREAD',      label: 'Most unwatched' },
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  tv: [
    { value: 'MOST_UNREAD',      label: 'Most unwatched' },
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  movies: [
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  // TODO: Books — refine once API is chosen
  books: [
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  // TODO: Games — refine once API is chosen
  games: [
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
}