import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import NotesDialog from '../../components/NotesDialog'
import TvProgressBar from '../../components/TvProgressBar'
import {
  getTvShow,
  getTvDetails,
  updateTvProgress,
  updateTvScore,
  updateTvStatus,
  refreshTvEpisodes,
  removeFromTvLibrary,
  addTvToLibrary,
  getTvCommunityRating,
  updateTvNotes,
} from '../../api/tvApi'
import { themes, statusStyles, statusLabels } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

const STATUS_OPTIONS = [
  { value: 'CONSUMING', label: 'Watching' },
  { value: 'PLANNED',   label: 'Plan to Watch' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function TvDetailPage() {
  const { tvId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.tv
  const fromSearch = location.state?.from === 'search'

  const [show, setShow] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [score, setScore] = useState(null)
  const [descExpanded, setDescExpanded] = useState(false)
  const [inLibrary, setInLibrary] = useState(true)
  const [communityRating, setCommunityRating] = useState(null)
  const [notesOpen, setNotesOpen] = useState(false)

  useEffect(() => {
    getTvShow(tvId)
      .then((res) => {
        setShow(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
      })
      .catch(() => {
        getTvDetails(tvId)
          .then((res) => {
            setShow(res.data)
            setInLibrary(false)
            setCommunityRating(res.data.communityRating ?? null)
            setLoading(false)
          })
          .catch(() => {
            setError('Failed to load TV show.')
            setLoading(false)
          })
      })

    getTvCommunityRating(tvId)
      .then((res) => setCommunityRating(res.data))
      .catch(() => setCommunityRating(null))
  }, [tvId])

  function handleProgressUpdate(episodesWatched, currentSeason) {
    return updateTvProgress(tvId, episodesWatched, currentSeason)
      .then((res) => setShow(res.data))
  }

  function handleScoreSave(newScore) {
    updateTvScore(tvId, newScore)
      .then((res) => {
        setShow(res.data)
        setScore(newScore)
      })
      .catch(console.error)
  }

  function handleStatusChange(newStatus) {
    updateTvStatus(tvId, newStatus)
      .then((res) => setShow(res.data))
      .catch(console.error)
  }

  function handleRefresh() {
    refreshTvEpisodes(tvId)
      .then((res) => setShow(res.data))
      .catch(console.error)
  }

  function handleRemove() {
    removeFromTvLibrary(tvId)
      .then(() => navigate(fromSearch ? '/tv/search' : '/tv/library'))
      .catch(console.error)
  }

  function handleAdd() {
    addTvToLibrary({
      tvId: show.id,
      title: show.title,
      status: 'PLANNED',
      episodesWatched: 0,
      currentSeason: 1,
      totalEpisodes: show.totalEpisodes,
      numberOfSeasons: show.numberOfSeasons,
      seasonEpisodes: show.seasonEpisodes,
      coverUrl: show.coverUrl,
      description: show.description,
      seriesStatus: normalizeSeriesStatus(show.status, 'tv'),
      network: show.network,
      genres: show.genres,
      firstAirYear: show.firstAirYear,
      seriesType: show.seriesType,
      communityRating: show.communityRating,
      nextEpisodeDate: show.nextEpisodeDate,
    })
      .then(() => setInLibrary(true))
      .catch(console.error)
  }

  function handleNotesSave(notes) {
    return updateTvNotes(tvId, notes)
      .then((res) => setShow(res.data))
  }

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <Navbar activeMedia="tv" />
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !show) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <Navbar activeMedia="tv" />
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'TV show not found.'}
        </div>
      </div>
    )
  }

  const description = show.description ?? 'No description available.'
  const isLongDesc = description.length > 300

  // Build badge text
  const badgeParts = []
  if (show.seriesType) badgeParts.push(show.seriesType)
  if (show.seriesStatus) badgeParts.push(
    show.seriesStatus.charAt(0).toUpperCase() + show.seriesStatus.slice(1)
  )
  const badgeText = badgeParts.join(' · ')

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="tv" />

      <div className="p-5">

        {/* Breadcrumb + Notes */}
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2 text-[12px]">
            <span
              className="cursor-pointer transition-colors"
              style={{ color: theme.accent }}
              onClick={() => navigate(fromSearch ? '/tv/search' : '/tv/library')}
            >
              ← {fromSearch ? 'Search' : 'Library'}
            </span>
            <span style={{ color: '#333344' }}>/</span>
            <span style={{ color: '#777788' }}>{show.title}</span>
          </div>

          {inLibrary && (
            <button
              onClick={() => setNotesOpen(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 text-[11px] rounded-lg transition-colors"
              style={{
                background: theme.accentBg,
                border: `0.5px solid ${theme.accentBorder}`,
                color: theme.accent,
              }}
            >
              📝 Notes
            </button>
          )}
        </div>

        <div className="flex flex-col md:flex-row gap-6">

          {/* Left col — cover */}
          <div className="shrink-0 w-full md:w-[180px]">
            <div
              className="w-full rounded-lg overflow-hidden flex items-center justify-center"
              style={{ background: theme.cardCover }}
            >
              {show.coverUrl ? (
                <img
                  src={show.coverUrl}
                  alt={show.title}
                  className="w-full object-contain"
                />
              ) : (
                <span style={{ fontSize: 40, color: theme.cardIcon }}>📺</span>
              )}
            </div>

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

            {/* Badge */}
            {badgeText && (
              <span
                className="inline-block text-[10px] px-2 py-[2px] rounded-full mb-2"
                style={{
                  background: `${theme.accent}22`,
                  border: `0.5px solid ${theme.accent}55`,
                  color: theme.accent,
                }}
              >
                {badgeText}
              </span>
            )}

            {/* Title */}
            <h1 className="text-[22px] font-medium text-[#e2e2f0] m-0 mb-1">
              {show.title}
            </h1>

            {/* Meta */}
            <p className="text-[12px] text-[#555566] m-0 mb-1">
              {[
                show.firstAirYear,
                show.genres,
                show.numberOfSeasons
                  ? `${show.numberOfSeasons} season${show.numberOfSeasons !== 1 ? 's' : ''}`
                  : null,
                show.totalEpisodes
                  ? `${show.totalEpisodes} episodes aired`
                  : null,
              ]
                .filter(Boolean)
                .join(' · ')}
            </p>

            {/* Network */}
            {show.network && (
              <p className="text-[12px] text-[#555566] m-0 mb-3">
                <span style={{ color: theme.accent }}>{show.network}</span>
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
                <span
                  onClick={() => setDescExpanded((v) => !v)}
                  className="text-[12px] cursor-pointer"
                  style={{ color: theme.accent }}
                >
                  {descExpanded ? 'Show less' : 'Read more'}
                </span>
              )}
            </div>

            {/* Score */}
            <div className="mb-4">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Score
              </p>
              <div className="flex gap-2">
                {inLibrary && (
                  <div
                    className="rounded-lg p-3 text-center w-fit"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  >
                    <p className="text-[28px] font-medium m-0 mb-0.5"
                      style={{ color: '#fbbf24' }}>
                      {score ?? '—'}
                    </p>
                    <p className="text-[11px] text-[#555566] m-0">Your score</p>
                    <div className="flex justify-center gap-1 mt-2">
                      {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                        <span
                          key={n}
                          onClick={() => handleScoreSave(n)}
                          className="cursor-pointer text-[16px] transition-colors"
                          style={{
                            color: score != null && n <= score
                              ? '#fbbf24' : '#333344'
                          }}
                          title={`Score ${n}`}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {communityRating != null && (
                  <div
                    className="rounded-lg p-3 text-center w-fit"
                    style={{
                      background: theme.topBar,
                      border: `0.5px solid ${theme.cardBorder}`,
                    }}
                  >
                    <p className="text-[28px] font-medium m-0 mb-0.5"
                      style={{ color: '#fbbf24' }}>
                      {communityRating}
                    </p>
                    <p className="text-[11px] text-[#555566] m-0">TMDB score</p>
                    <p className="text-[11px] text-[#333344] m-0 mt-1">
                      Community rating
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Progress */}
            {inLibrary && (
              <div className="mb-4">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Progress
                </p>
                <TvProgressBar
                  item={show}
                  theme={theme}
                  onUpdate={handleProgressUpdate}
                />
              </div>
            )}

            {/* Status */}
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
                        background: show.status === opt.value
                          ? statusStyles[opt.value].badge.background
                          : theme.topBar,
                        color: show.status === opt.value
                          ? statusStyles[opt.value].badge.color
                          : '#555566',
                        border: `0.5px solid ${show.status === opt.value
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

      <NotesDialog
        isOpen={notesOpen}
        onClose={() => setNotesOpen(false)}
        title={show.title}
        initialNotes={show.notes}
        currentProgress={show.episodesWatched}
        progressLabel="Ep."
        onSave={handleNotesSave}
      />
    </div>
  )
}

export default TvDetailPage