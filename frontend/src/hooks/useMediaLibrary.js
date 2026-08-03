// src/hooks/useMediaLibrary.js
import { useState, useEffect } from 'react'

export function useMediaLibrary({ getLibrary, refreshAll, refreshOngoing = null, refreshDelay = 0 }) {
  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    getLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
        if (refreshOngoing) {
          if (refreshDelay > 0) {
            setTimeout(() => backgroundRefreshOngoing(res.data), refreshDelay)
          } else {
            backgroundRefreshOngoing(res.data)
          }
        }
      })
      .catch((err) => {
        console.error('Failed to load library:', err)
        setError('Failed to load library.')
        setLoading(false)
      })
  }, [])

  function backgroundRefreshOngoing(items) {
    const ongoing = items.filter(
      (item) => (item.seriesStatus === 'ongoing' || item.seriesStatus === 'hiatus') && item.status !== 'DROPPED'
    )
    if (ongoing.length === 0) return
    refreshOngoing()
      .then((res) => setLibrary(res.data))
      .catch((err) => console.warn('Background refresh failed:', err))
  }

  async function handleRefreshAll() {
    const res = await refreshAll()
    setLibrary(res.data)
  }

  return { library, loading, error, handleRefreshAll }
}