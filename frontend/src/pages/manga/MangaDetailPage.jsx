import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation} from 'react-router-dom'
import Navbar from '../../components/Navbar'
import { getManga,getMangaDetails,updateProgress, updateScore, updateStatus, refreshLatestChapter, removeFromLibrary, addToLibrary, getCommunityRating } from '../../api/mangaApi'
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
  const [inLibrary, setInLibrary] = useState(true)
  const location = useLocation()
  const fromSearch = location.state?.from === 'search'
  const [communityRating, setCommunityRating] = useState(null)

  useEffect(() => {
    getManga(mangaId)
      .then((res) => {
        setManga(res.data)
        setChaptersRead(res.data.chaptersRead ?? 0)
        setScore(res.data.score ?? null)
        setLoading(false)
      })
      .catch(() => {
        // Not in library — fetch from MangaDex
        getMangaDetails(mangaId)
          .then((res) => {
            setManga(res.data)
            setInLibrary(false)
            setLoading(false)
          })
          .catch(() => {
            setError('Failed to load manga.')
            setLoading(false)
          })
      })

    // Fetch community rating separately — always from MangaDex
    getCommunityRating(mangaId)
      .then((res) => setCommunityRating(res.data))
      .catch(() => setCommunityRating(null))
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
      .then(() => navigate(fromSearch ? '/manga/search' : '/manga/library'))
      .catch(console.error)
  }

  function handleAdd() {
    addToLibrary({
      mangaId: manga.id,
      title: manga.title,
      status: 'PLANNED',
      chaptersRead: 0,
      latestChapter: manga.latestChapter,
      coverUrl: manga.coverUrl,
      author: manga.author,
      artist: manga.artist,
      description: manga.description,
      seriesStatus: manga.status,
    })
      .then(() => {
        setInLibrary(true)
      })
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
            onClick={() => navigate(fromSearch ? '/manga/search' : '/manga/library')}
          >
            ← {fromSearch ? 'Search' : 'Library'}
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

            {/* Refresh button — only if in library */}
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
                ↻ Refresh chapters
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
            
            {/* Score - only if in library */}
            {inLibrary && (
              <div className="mt-3 mb-4">
                <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                  Score
                </p>
                
                {/* Score cards row */}
                <div className="flex gap-2 w-fit">

                  {/* Your score card */}
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

                  {/* Community rating card */}
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
                      MangaDex score
                    </p>
                    <p className="text-[11px] text-[#333344] m-0 mt-1">
                      Community rating
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Progress - only if in library*/}
            {inLibrary && (
            <div className="mb-4">
              <p className="text-[11px] text-[#555566] uppercase tracking-[0.05em] mb-1.5">
                Progress
              </p>

              {/* Chapter count + unread */}
              <div className="flex items-center justify-between mb-1.5">
                <span className="text-[13px] font-medium text-[#e2e2f0]">
                  {chaptersRead}
                  {manga.latestChapter != null && (
                    <span className="text-[12px] text-[#555566]"> / {manga.latestChapter} chapters</span>
                  )}
                </span>
                {unread != null && unread > 0 && (
                  <span
                    className="text-[11px] px-2 py-[3px] rounded-full"
                    style={{ background: theme.unreadBadge, color: theme.accent }}
                  >
                    +{unread} unread
                  </span>
                )}
              </div>

              {/* Progress bar */}
              {manga.latestChapter != null && (
                <div
                  className="w-full h-[3px] rounded-full mb-2"
                  style={{ background: theme.cardBorder }}
                >
                  <div
                    className="h-full rounded-full transition-all"
                    style={{
                      width: `${Math.min(100, (chaptersRead / manga.latestChapter) * 100)}%`,
                      background: theme.accent,
                    }}
                  />
                </div>
              )}

              {/* Input row */}
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
              </div>
            </div>
            )}
            
            {/* Status selector - only if in library*/}
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
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default MangaDetailPage