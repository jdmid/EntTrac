import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../../components/Navbar'
import { getManga, updateProgress, updateScore, updateStatus, refreshLatestChapter, removeFromLibrary } from '../../api/mangaApi'
import { themes, statusStyles } from '../../theme/themes'

const STATUS_OPTIONS = [
  { value: 'CONSUMING', label: 'Reading' },
  { value: 'PLANNED',   label: 'Plan to Read' },
  { value: 'FINISHED',  label: 'Finished' },
  { value: 'DROPPED',   label: 'Dropped' },
]

function MangaDetailPage() {
  const { mangaId } = useParams()
  const navigate = useNavigate()
  const theme = themes.manga

  const [manga, setManga] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Local editable state
  const [chaptersRead, setChaptersRead] = useState(0)
  const [score, setScore] = useState(null)
  const [descExpanded, setDescExpanded] = useState(false)

  useEffect(() => {
    getManga(mangaId)
      .then((res) => {
        setManga(res.data)
        setChaptersRead(res.data.chaptersRead ?? 0)
        setScore(res.data.score ?? null)
        setLoading(false)
      })
      .catch((err) => {
        console.error(err)
        setError('Failed to load manga.')
        setLoading(false)
      })
  }, [mangaId])

  function handleProgressSave() {
    updateProgress(mangaId, chaptersRead)
      .then((res) => setManga(res.data))
      .catch(console.error)
  }

  function handleScoreSave(newScore) {
    updateScore(mangaId, newScore)
      .then((res) => {
        setManga(res.data)
        setScore(newScore)
      })
      .catch(console.error)
  }

  function handleStatusChange(newStatus) {
    updateStatus(mangaId, newStatus)
      .then((res) => setManga(res.data))
      .catch(console.error)
  }

  function handleRefresh() {
    refreshLatestChapter(mangaId)
      .then((res) => setManga(res.data))
      .catch(console.error)
  }

  function handleRemove() {
    removeFromLibrary(mangaId)
      .then(() => navigate('/manga/library'))
      .catch(console.error)
  }

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <Navbar activeMedia="manga" />
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !manga) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <Navbar activeMedia="manga" />
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Manga not found.'}
        </div>
      </div>
    )
  }

  const style = statusStyles[manga.status] ?? statusStyles.FINISHED
  const unread = manga.latestChapter != null
    ? Math.max(0, manga.latestChapter - (manga.chaptersRead ?? 0))
    : null

  const description = manga.description ?? 'No description available.'
  const isLongDesc = description.length > 300

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="manga" />

      <div className="p-5">

        {/* Breadcrumb */}
        <div className="flex items-center gap-2 mb-5 text-[12px]">
          <span
            className="cursor-pointer transition-colors"
            style={{ color: theme.accent }}
            onClick={() => navigate('/manga/library')}
          >
            ← Library
          </span>
          <span style={{ color: '#333344' }}>/</span>
          <span style={{ color: '#777788' }}>{manga.title}</span>
        </div>

        {/* Detail layout */}
        <div className="flex gap-6">

          {/* Left col — cover */}
          <div className="shrink-0 w-[180px]">
            <div
              className="w-full aspect-[2/3] rounded-lg overflow-hidden flex items-center justify-center"
              style={{ background: theme.cardCover }}
            >
              {manga.coverUrl ? (
                <img
                  src={manga.coverUrl}
                  alt={manga.title}
                  className="w-full h-full object-cover object-top"
                />
              ) : (
                <span style={{ fontSize: 40, color: theme.cardIcon }}>📖</span>
              )}
            </div>

            {/* Refresh button */}
            <button
              onClick={handleRefresh}
              className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
              style={{
                background: theme.accentBg,
                border: `0.5px solid ${theme.accentBorder}`,
                color: theme.accent,
              }}
            >
              ↻ Refresh chapters
            </button>

            {/* Remove button */}
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

            {/* Score */}
            <div className="mt-3">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Your Score
              </p>
              <div className="flex gap-1 flex-wrap">
                {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                  <button
                    key={n}
                    onClick={() => handleScoreSave(n)}
                    className="w-7 h-7 rounded text-[11px] transition-colors"
                    style={{
                      background: score === n ? theme.accent : theme.topBar,
                      color: score === n ? '#fff' : '#555566',
                      border: `0.5px solid ${score === n ? theme.accent : '#2a2a3a'}`,
                    }}
                  >
                    {n}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Right col — info */}
          <div className="flex-1 min-w-0">

          {/* Title */}
          <h1 className="text-[22px] font-medium text-[#e2e2f0] m-0 mb-1">
            {manga.title}
          </h1>

          {/* Series status + chapters */}
          <p className="text-[12px] text-[#555566] m-0 mb-1 capitalize">
            {manga.seriesStatus ?? 'Unknown status'}
            {manga.latestChapter != null ? ` · Ch. ${manga.latestChapter}` : ''}
          </p>

          {/* Author and artist */}
          {(manga.author || manga.artist) && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              {manga.author && <span>By <span style={{ color: themes.manga.accent }}>{manga.author}</span></span>}
              {manga.author && manga.artist && <span> · </span>}
              {manga.artist && <span>Art by <span style={{ color: themes.manga.accent }}>{manga.artist}</span></span>}
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

            {/* Progress */}
            <div className="mb-4">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Progress
              </p>
              <div className="flex items-center gap-2">
                <span className="text-[12px] text-[#777788]">Ch.</span>
                <input
                  type="number"
                  min={0}
                  max={manga.latestChapter ?? 9999}
                  value={chaptersRead}
                  onChange={(e) => setChaptersRead(Number(e.target.value))}
                  className="w-16 px-2 py-1 rounded text-[12px] text-[#e2e2f0] outline-none text-center"
                  style={{
                    background: theme.topBar,
                    border: `0.5px solid ${theme.cardBorder}`,
                  }}
                />
                {manga.latestChapter != null && (
                  <span className="text-[12px] text-[#555566]">
                    / {manga.latestChapter}
                  </span>
                )}
                <button
                  onClick={handleProgressSave}
                  className="px-3 py-1 text-[11px] rounded transition-colors"
                  style={{
                    background: theme.accentBg,
                    border: `0.5px solid ${theme.accentBorder}`,
                    color: theme.accent,
                  }}
                >
                  Save
                </button>
                {unread != null && unread > 0 && (
                  <span
                    className="text-[11px] px-2 py-[3px] rounded-full"
                    style={{ background: theme.unreadBadge, color: theme.accent }}
                  >
                    +{unread} unread
                  </span>
                )}
              </div>
            </div>

            {/* Status selector */}
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
                      background: manga.status === opt.value
                        ? statusStyles[opt.value].badge.background
                        : theme.topBar,
                      color: manga.status === opt.value
                        ? statusStyles[opt.value].badge.color
                        : '#555566',
                      border: `0.5px solid ${manga.status === opt.value
                        ? statusStyles[opt.value].border
                        : '#2a2a3a'}`,
                    }}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  )
}

export default MangaDetailPage