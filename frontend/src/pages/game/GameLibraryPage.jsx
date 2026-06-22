import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LibraryPageLayout from '../../components/LibraryPageLayout'
import LibraryMediaCard from '../../components/LibraryMediaCard'
import { getGameLibrary } from '../../api/gameApi'
import { themes } from '../../theme/themes'
import { SERIES_STATUS_FILTERS, SORT_OPTIONS } from '../../utils/statusMapping'
import { sortGames } from '../../utils/sortUtils'

const GAME_STATUS_FILTERS = [
  { value: 'ALL',       label: 'All' },
  { value: 'CONSUMING', label: 'Playing' },
  { value: 'PLANNED',   label: 'Plan to Play' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function GameLibraryPage() {
  const navigate = useNavigate()

  const [library, setLibrary] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [statusFilter, setStatusFilter] = useState('ALL')
  const [seriesStatusFilter, setSeriesStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('RECENTLY_ADDED')

  useEffect(() => {
    setLoading(true)
    getGameLibrary()
      .then((res) => {
        setLibrary(res.data)
        setLoading(false)
      })
      .catch((err) => {
        console.error('Failed to load library:', err)
        setError('Failed to load library.')
        setLoading(false)
      })
  }, [])

  return (
    <LibraryPageLayout
      activeMedia="game"
      pageTitle="My Game Library"
      library={library}
      loading={loading}
      error={error}
      emptyMessage="Your library is empty. Search for games to add!"
      emptyFilterMessage="No games match the current filters."
      statusFilters={GAME_STATUS_FILTERS}
      statusFilter={statusFilter}
      onStatusChange={setStatusFilter}
      seriesStatusFilters={SERIES_STATUS_FILTERS.game}
      seriesStatusFilter={seriesStatusFilter}
      onSeriesStatusChange={setSeriesStatusFilter}
      sortOptions={SORT_OPTIONS.game}
      sortBy={sortBy}
      onSortChange={setSortBy}
      sortFn={sortGames}
      renderCard={(game) => (
        <LibraryMediaCard
          key={game.gameId}
          title={game.title}
          creator={[game.developer, game.releaseYear]
            .filter(Boolean)
            .join(' · ')}
          score={game.score}
          status={game.status}
          seriesStatus={game.seriesStatus}
          coverUrl={game.coverUrl}
          theme={themes.game}
          icon="🎮"
          medium="game"
          progressLabel="Hrs."
          progress={game.hoursPlayed}
          total={null}
          onClick={() => navigate(`/game/library/${game.gameId}`)}
        />
      )}
    />
  )
}

export default GameLibraryPage