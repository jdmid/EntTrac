import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import SimpleProgressBar from '../../components/SimpleProgressBar'
import {
  getManga, getMangaDetails, updateProgress, updateScore,
  updateStatus, refreshLatestChapter, removeFromLibrary,
  addToLibrary, getCommunityRating, updateMangaNotes,
} from '../../api/mangaApi'
import { themes } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

function MangaDetailPage() {
  const { mangaId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.manga
  const fromSearch = location.state?.from === 'search'

  const [manga, setManga] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [score, setScore] = useState(null)
  const [inLibrary, setInLibrary] = useState(true)
  const [communityRating, setCommunityRating] = useState(null)

  useEffect(() => {
    getManga(mangaId)
      .then((res) => {
        setManga(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
      })
      .catch(() => {
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

    getCommunityRating(mangaId)
      .then((res) => setCommunityRating(res.data))
      .catch(() => setCommunityRating(null))
  }, [mangaId])

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !manga) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Manga not found.'}
        </div>
      </div>
    )
  }

  return (
    <DetailPageLayout
      activeMedia="manga"
      fromSearch={fromSearch}
      backPath={fromSearch ? '/manga/search' : '/manga/library'}
      title={manga.title}
      item={manga}
      inLibrary={inLibrary}
      communityRating={communityRating}
      communityRatingLabel="MangaDex score"
      score={score}
      theme={theme}
      icon="📖"
      refreshLabel="↻ Refresh chapters"
      metaLine={
        <div>
          <p className="text-[12px] text-[#555566] m-0 mb-1 capitalize">
            {manga.seriesStatus ?? 'Unknown status'}
            {manga.latestChapter != null ? ` · Ch. ${manga.latestChapter}` : ''}
          </p>
          {(manga.author || manga.artist) && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              {manga.author && (
                <span>By <span style={{ color: theme.accent }}>{manga.author}</span></span>
              )}
              {manga.author && manga.artist && <span> · </span>}
              {manga.artist && (
                <span>Art by <span style={{ color: theme.accent }}>{manga.artist}</span></span>
              )}
            </p>
          )}
        </div>
      }
      progressSection={
        <SimpleProgressBar
          progress={manga.chaptersRead}
          total={manga.latestChapter}
          label="chapter"
          theme={theme}
          onUpdate={(val) =>
            updateProgress(mangaId, val).then((res) => setManga(res.data))
          }
        />
      }
      notesProgressLabel="Ch."
      onRefresh={() =>
        refreshLatestChapter(mangaId).then((res) => setManga(res.data))
      }
      onRemove={() =>
        removeFromLibrary(mangaId).then(() =>
          navigate(fromSearch ? '/manga/search' : '/manga/library')
        )
      }
      onAdd={() =>
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
          seriesStatus: normalizeSeriesStatus(manga.status, 'manga'),
        }).then(() => setInLibrary(true))
      }
      onScoreSave={(n) =>
        updateScore(mangaId, n).then((res) => {
          setManga(res.data)
          setScore(n)
        })
      }
      onStatusChange={(s) =>
        updateStatus(mangaId, s).then((res) => setManga(res.data))
      }
      onNotesSave={(notes) =>
        updateMangaNotes(mangaId, notes).then((res) => setManga(res.data))
      }
    />
  )
}

export default MangaDetailPage