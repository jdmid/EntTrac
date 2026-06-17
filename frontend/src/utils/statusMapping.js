// Maps any API's series status to our normalized set
export const normalizeSeriesStatus = (apiStatus, medium) => {
  if (!apiStatus) return null
 
  const status = apiStatus.toLowerCase().trim()
 
  if (medium === 'manga') {
    // MangaDex values
    if (status === 'ongoing') return 'ongoing'
    if (status === 'completed') return 'completed'
    if (status === 'hiatus') return 'hiatus'
    if (status === 'cancelled') return 'cancelled'
  }
 
  if (medium === 'anime') {
    // Jikan/MAL values
    if (status === 'currently airing') return 'ongoing'
    if (status === 'finished airing') return 'completed'
    if (status === 'not yet aired') return 'upcoming'
  }
 
  if (medium === 'tv') {
    // TMDB TV values
    if (status === 'returning series') return 'ongoing'
    if (status === 'in production') return 'in production'
    if (status === 'pilot') return 'upcoming'
    if (status === 'planned') return 'upcoming'
    if (status === 'ended') return 'completed'
    if (status === 'canceled') return 'cancelled'
  }
 
  if (medium === 'movie') {
    // TMDB Movie values
    if (status === 'released') return 'released'
    if (status === 'in production') return 'in production'
    if (status === 'post production') return 'in production'
    if (status === 'planned') return 'upcoming'
    if (status === 'rumored') return 'upcoming'
    if (status === 'canceled') return 'cancelled'
  }
 
 // Books have no seriesStatus — normalizeSeriesStatus returns null for 'book' medium
 // Forthcoming titles may need mapping when Google Books is added (see issue #25)
 
  if (medium === 'game') {
    if (status === 'released') return 'released'
    if (status === 'upcoming') return 'upcoming'
    if (status === 'cancelled') return 'cancelled'
  }
  
    return null
  }
 
// Labels for the filter chips per medium
export const SERIES_STATUS_FILTERS = {
  manga: [
    { value: 'ALL',       label: 'All' },
    { value: 'ongoing',   label: 'Ongoing' },
    { value: 'completed',  label: 'Completed' },
    { value: 'hiatus',    label: 'Hiatus' },
    { value: 'cancelled', label: 'Cancelled' },
  ],
  anime: [
    { value: 'ALL',      label: 'All' },
    { value: 'ongoing',  label: 'Currently Airing' },
    { value: 'completed', label: 'Finished Airing' },
    { value: 'upcoming', label: 'Not Yet Aired' },
  ],
  tv: [
    { value: 'ALL',       label: 'All' },
    { value: 'ongoing',   label: 'Ongoing' },
    { value: 'completed',  label: 'Ended' },
    { value: 'upcoming',  label: 'Upcoming' },
    { value: 'in production',  label: 'In Production' },
    { value: 'cancelled', label: 'Cancelled' },
  ],
  movie: [
    { value: 'ALL',            label: 'All' },
    { value: 'released',       label: 'Released' },
    { value: 'in production',  label: 'In Production' },
    { value: 'upcoming',       label: 'Upcoming' },
    { value: 'cancelled',      label: 'Cancelled' },
  ],
  // Books have no seriesStatus — no additional filters needed
  // Forthcoming/announced titles may add filters when Google Books is added (see issue #25)
  book: [
    { value: 'ALL', label: 'All' },
  ],

  game: [
    { value: 'ALL',          label: 'All' },
    { value: 'released',     label: 'Released' },
    { value: 'upcoming',     label: 'Upcoming' },
    { value: 'cancelled',    label: 'Cancelled' },
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
  movie: [
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  // TODO: Book — refine once API is chosen
  book: [
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
  // TODO: Games — refine once API is chosen
  game: [
    { value: 'ALPHA_AZ',         label: 'Alphabetical A–Z' },
    { value: 'ALPHA_ZA',         label: 'Alphabetical Z–A' },
    { value: 'SCORE_HIGH',       label: 'Score (high to low)' },
    { value: 'SCORE_LOW',        label: 'Score (low to high)' },
    { value: 'RECENTLY_UPDATED', label: 'Recently updated' },
    { value: 'RECENTLY_ADDED',   label: 'Recently added' },
  ],
}