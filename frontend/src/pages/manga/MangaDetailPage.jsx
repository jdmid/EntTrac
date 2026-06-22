import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import SimpleProgressBar from '../../components/SimpleProgressBar'
import RatingCard from '../../components/RatingCard'
import {
  getManga, getMangaDetails, updateProgress, updateScore,
  updateStatus, refreshLatestChapter, removeFromLibrary,
  addToLibrary, enrichMangaFromCache, updateMangaNotes,
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
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    getManga(mangaId)
      .then((res) => {
        setManga(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
        enrichMangaFromCache(mangaId)
          .then((enriched) => setManga(enriched.data))
          .catch((err) => console.error('Enrich failed:', err))
      })
      .catch(() => {
        getMangaDetails(mangaId)
          .then((res) => {
            setManga(res.data)
            setInLibrary(false)
            setLoading(false)
          })
          .catch((err) => {
            console.error('Failed to load manga details:', err)
            setError('Failed to load manga.')
            setLoading(false)
          })
      })
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
      score={score}
      theme={theme}
      icon="📖"
      refreshLabel="Refresh chapters"
      metaLine={
        <div>
          <p className="text-[12px] text-[#555566] m-0 mb-1 capitalize">
            {manga.seriesStatus ?? 'Unknown status'}
            {manga.latestChapter != null ? ` · Ch. ${manga.latestChapter}` : ''}
          </p>
          {(manga.author || manga.artist) && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              {manga.author && (
                <span>
                  By{' '}
                  <span
                    className="cursor-pointer inline-flex items-center gap-1"
                    style={{ color: theme.accent }}
                    onClick={() => navigate(
                      `/manga/search?tab=creator&creatorId=${manga.authorId}&creatorName=${encodeURIComponent(manga.author)}`
                    )}
                  >
                    {manga.author}
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                      strokeLinejoin="round" aria-hidden="true">
                      <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
                      <polyline points="15 3 21 3 21 9"/>
                      <line x1="10" y1="14" x2="21" y2="3"/>
                    </svg>
                  </span>
                </span>
              )}
              {manga.author && manga.artist && <span> · </span>}
              {manga.artist && (
                <span>
                  Art by{' '}
                  <span
                    className="cursor-pointer inline-flex items-center gap-1"
                    style={{ color: theme.accent }}
                    onClick={() => navigate(
                      `/manga/search?tab=creator&creatorId=${manga.artistId}&creatorName=${encodeURIComponent(manga.artist)}`
                    )}
                  >
                    {manga.artist}
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                      strokeLinejoin="round" aria-hidden="true">
                      <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
                      <polyline points="15 3 21 3 21 9"/>
                      <line x1="10" y1="14" x2="21" y2="3"/>
                    </svg>
                  </span>
                </span>
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
      refreshing={refreshing}
      ratingsSection={
          <RatingCard
            value={manga.mangadexRating}
            label="MangaDex"
            color="#f87c23"
            theme={theme}
          />
        }
      onRefresh={() => {
        setRefreshing(true)
        refreshLatestChapter(mangaId)
          .then((res) => setManga(res.data))
          .finally(() => setRefreshing(false))
        }
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
          authorId: manga.authorId ?? null,
          artistId: manga.artistId ?? null,
        }).then((res) => {
          console.log('=== ADD RESPONSE ===', res.data)
          setManga(res.data)
          setInLibrary(true)
        })
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