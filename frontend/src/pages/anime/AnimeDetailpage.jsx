import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import { getAnime, getAnimeDetails, updateAnimeProgress, updateAnimeScore, updateAnimeStatus, refreshLatestEpisode, removeAnimeFromLibrary, addAnimeToLibrary, getAnimeCommunityRating } from '../../api/animeApi'
import { themes, statusStyles } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

const STATUS_OPTIONS = [
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function AnimeDetailPage() {
  const { animeId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.anime

  const fromSearch = location.state?.from === 'search'

  const [anime, setAnime] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [episodesWatched, setEpisodesWatched] = useState(0)
  const [score, setScore] = useState(null)
  const [descExpanded, setDescExpanded] = useState(false)
  const [inLibrary, setInLibrary] = useState(true)
  const [communityRating, setCommunityRating] = useState(null)

  useEffect(() => {
    getAnime(animeId)
      .then((res) => {
        setAnime(res.data)
        setEpisodesWatched(res.data.episodesWatched ?? 0)
        setScore(res.data.score ?? null)
        setLoading(false)
      })
      .catch(() => {
        getAnimeDetails(animeId)
          .then((res) => {
            setAnime(res.data)
            setInLibrary(false)
            setLoading(false)
          })
          .catch(() => {
            setError('Failed to load anime.')
            setLoading(false)
          })
      })

    getAnimeCommunityRating(animeId)
      .then((res) => setCommunityRating(res.data))
      .catch(() => setCommunityRating(null))
  }, [animeId])

  function handleProgressSave() {
    updateAnimeProgress(animeId, episodesWatched)
      .then((res) => setAnime(res.data))
      .catch(console.error)
  }

  function handleScoreSave(newScore) {
    updateAnimeScore(animeId, newScore)
      .then((res) => {
        setAnime(res.data)
        setScore(newScore)
      })
      .catch(console.error)
  }

  function handleStatusChange(newStatus) {
    updateAnimeStatus(animeId, newStatus)
      .then((res) => setAnime(res.data))
      .catch(console.error)
  }

  function handleRefresh() {
    refreshLatestEpisode(animeId)
      .then((res) => setAnime(res.data))
      .catch(console.error)
  }

  function handleRemove() {
    removeAnimeFromLibrary(animeId)
      .then(() => navigate(fromSearch ? '/anime/search' : '/anime/library'))
      .catch(console.error)
  }

  function handleAdd() {
    addAnimeToLibrary({
      animeId: anime.id,
      title: anime.title,
      status: 'PLANNED',
      episodesWatched: 0,
      totalEpisodes: anime.totalEpisodes,
      coverUrl: anime.coverUrl,
      description: anime.description,
      seriesStatus: normalizeSeriesStatus(anime.status, 'anime'),
      studio: anime.studio,
      season: anime.season,
      communityRating: anime.communityRating,
    })
      .then(() => setInLibrary(true))
      .catch(console.error)
  }

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <Navbar activeMedia="anime" />
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !anime) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <Navbar activeMedia="anime" />
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Anime not found.'}
        </div>
      </div>
    )
  }

  const style = statusStyles[anime.status] ?? statusStyles.FINISHED
  const unwatched = anime.totalEpisodes != null
    ? Math.max(0, anime.totalEpisodes - (anime.episodesWatched ?? 0))
    : null

  const description = anime.description ?? 'No description available.'
  const isLongDesc = description.length > 300

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="anime" />

      <div className="p-5">

        {/* Breadcrumb */}
        <div className="flex items-center gap-2 mb-5 text-[12px]">
          <span
            className="cursor-pointer transition-colors"
            style={{ color: theme.accent }}
            onClick={() => navigate(fromSearch ? '/anime/search' : '/anime/library')}
          >
            ← {fromSearch ? 'Search' : 'Library'}
          </span>
          <span style={{ color: '#333344' }}>/</span>
          <span style={{ color: '#777788' }}>{anime.title}</span>
        </div>

        {/* Detail layout */}
        <div className="flex flex-col md:flex-row gap-6">

          {/* Left col — cover */}
          <div className="shrink-0 w-full md:w-[180px]">
            <div
              className="w-full rounded-lg overflow-hidden flex items-center justify-center"
              style={{ background: theme.cardCover }}
            >
              {anime.coverUrl ? (
                <img
                  src={anime.coverUrl}
                  alt={anime.title}
                  className="w-full object-contain"
                />
              ) : (
                <span style={{ fontSize: 40, color: theme.cardIcon }}>📺</span>
              )}
            </div>

            {/* Refresh button */}
            {inLibrary && (
              <button
                onClick={handleRefresh}
                className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
                style={{
                  background: theme.accentBg,
                  border: `0.5px solid ${theme.accentBorder}`,
                  color: theme.accent,
                }}
              >
                ↻ Refresh episodes
              </button>
            )}

            {/* Remove or Add button */}
            {inLibrary ? (
              <button
                onClick={handleRemove}
                className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
                style={{
                  background: '#2e1212',
                  border: '0.5px solid #501c1c',
                  color: '#f87171',
                }}
              >
                Remove from library
              </button>
            ) : (
              <button
                onClick={handleAdd}
                className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
                style={{
                  background: theme.accentBg,
                  border: `0.5px solid ${theme.accentBorder}`,
                  color: theme.accent,
                }}
              >
                + Add to library
              </button>
            )}
          </div>

          {/* Right col — info */}
          <div className="flex-1 min-w-0">

            {/* Title */}
            <h1 className="text-[22px] font-medium text-[#e2e2f0] m-0 mb-1">
              {anime.title}
            </h1>

            {/* Series status + episodes */}
            <p className="text-[12px] text-[#555566] m-0 mb-1 capitalize">
              {anime.seriesStatus ?? 'Unknown status'}
              {anime.totalEpisodes != null ? ` · Ep. ${anime.totalEpisodes}` : ''}
            </p>

            {/* Studio and season */}
            {(anime.studio || anime.season) && (
              <p className="text-[12px] text-[#555566] m-0 mb-3">
                {anime.studio && (
                  <span>
                    By{' '}
                    <span style={{ color: theme.accent }}>{anime.studio}</span>
                  </span>
                )}
                {anime.studio && anime.season && <span> · </span>}
                {anime.season && (
                  <span style={{ color: theme.accent }}>{anime.season}</span>
                )}
              </p>
            )}

            {/* Description */}
            <div className="mb-4">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Description
              </p>
              <p className="text-[13px] text-[#9a9aaa] m-0 leading-relaxed">
                {isLongDesc && !descExpanded
                  ? description.slice(0, 300) + '…'
                  : description}
              </p>
              {isLongDesc && (
                <button
                  onClick={() => setDescExpanded((v) => !v)}
                  className="text-[11px] mt-1 cursor-pointer"
                  style={{ color: theme.accent, background: 'none', border: 'none', padding: 0 }}
                >
                  {descExpanded ? 'Show less' : 'Read more'}
                </button>
              )}
            </div>

            {/* Score - only if in library */}
            {inLibrary && (
              <div className="mt-3 mb-4">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Score
                </p>
                <div className="flex gap-2">
                  <div
                    className="flex-1 rounded-lg p-3 text-center"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  >
                    <p
                      className="text-[28px] font-medium m-0 mb-0.5"
                      style={{ color: '#fbbf24' }}
                    >
                      {score ?? '—'}
                    </p>
                    <p className="text-[11px] text-[#555566] m-0">
                      Your score
                    </p>
                    <div className="flex justify-center gap-1 mt-2">
                      {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                        <span
                          key={n}
                          onClick={() => handleScoreSave(n)}
                          className="cursor-pointer text-[16px] transition-colors"
                          style={{ color: score != null && n <= score ? '#fbbf24' : '#333344' }}
                          title={`Score ${n}`}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                  </div>

                  <div
                    className="flex-1 rounded-lg p-3 text-center"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  >
                    <p
                      className="text-[28px] font-medium m-0 mb-0.5"
                      style={{ color: '#fbbf24' }}
                    >
                      {communityRating ?? '—'}
                    </p>
                    <p className="text-[11px] text-[#555566] m-0">
                      MAL score
                    </p>
                    <p className="text-[11px] text-[#333344] m-0 mt-1">
                      Community rating
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Progress - only if in library */}
            {inLibrary && (
              <div className="mb-4">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Progress
                </p>

                <div className="flex items-center justify-between mb-1.5">
                  <span className="text-[13px] font-medium text-[#e2e2f0]">
                    {episodesWatched}
                    {anime.totalEpisodes != null && (
                      <span className="text-[12px] text-[#555566]">
                        {' '}/ {anime.totalEpisodes} episodes
                      </span>
                    )}
                  </span>
                  {unwatched != null && unwatched > 0 && (
                    <span
                      className="text-[11px] px-2 py-[3px] rounded-full"
                      style={{ background: theme.unreadBadge, color: theme.accent }}
                    >
                      +{unwatched} unwatched
                    </span>
                  )}
                </div>

                {anime.totalEpisodes != null && (
                  <div
                    className="w-full h-[3px] rounded-full mb-2"
                    style={{ background: theme.cardBorder }}
                  >
                    <div
                      className="h-full rounded-full transition-all"
                      style={{
                        width: `${Math.min(100, (episodesWatched / anime.totalEpisodes) * 100)}%`,
                        background: theme.accent,
                      }}
                    />
                  </div>
                )}

                <div className="flex items-center gap-2">
                  <span className="text-[12px] text-[#777788]">Ep.</span>
                  <input
                    type="number"
                    min={0}
                    max={anime.totalEpisodes ?? 9999}
                    value={episodesWatched}
                    onChange={(e) => setEpisodesWatched(Number(e.target.value))}
                    onBlur={handleProgressSave}
                    className="w-16 px-2 py-1 rounded text-[12px] text-[#e2e2f0] outline-none text-center"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  />
                  {anime.totalEpisodes != null && (
                    <span className="text-[12px] text-[#555566]">
                      / {anime.totalEpisodes}
                    </span>
                  )}
                </div>
              </div>
            )}

            {/* Status selector - only if in library */}
            {inLibrary && (
              <div className="mb-6">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Status
                </p>
                <div className="flex gap-2 flex-wrap">
                  {STATUS_OPTIONS.map((opt) => (
                    <button
                      key={opt.value}
                      onClick={() => handleStatusChange(opt.value)}
                      className="px-3 py-1 text-[11px] rounded-full transition-colors"
                      style={{
                        background: anime.status === opt.value
                          ? statusStyles[opt.value].badge.background
                          : theme.topBar,
                        color: anime.status === opt.value
                          ? statusStyles[opt.value].badge.color
                          : '#555566',
                        border: `0.5px solid ${anime.status === opt.value
                          ? statusStyles[opt.value].border
                          : '#2a2a3a'}`,
                      }}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  )
}

export default AnimeDetailPage