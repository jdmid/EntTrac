// Maps any API's series status to our normalized set
export const normalizeSeriesStatus = (apiStatus, medium) => {
  if (!apiStatus) return null

  const status = apiStatus.toLowerCase()

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

  return null
}

// Labels for the filter chips per medium
export const SERIES_STATUS_FILTERS = {
  manga: [
    { value: 'ALL',       label: 'All' },
    { value: 'ongoing',   label: 'Ongoing' },
    { value: 'completed', label: 'Completed' },
    { value: 'hiatus',    label: 'Hiatus' },
    { value: 'cancelled', label: 'Cancelled' },
  ],
  anime: [
    { value: 'ALL',      label: 'All' },
    { value: 'ongoing',  label: 'Currently Airing' },
    { value: 'finished', label: 'Finished Airing' },
    { value: 'upcoming', label: 'Not Yet Aired' },
  ],
}