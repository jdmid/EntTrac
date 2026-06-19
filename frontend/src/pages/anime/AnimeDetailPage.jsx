import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import SimpleProgressBar from '../../components/SimpleProgressBar'
import RatingCard from '../../components/RatingCard'
import {
  getAnime, getAnimeDetails, updateAnimeProgress, updateAnimeScore,
  updateAnimeStatus, refreshLatestEpisode, removeAnimeFromLibrary,
  addAnimeToLibrary, enrichAnimeFromCache, updateAnimeNotes,
} from '../../api/animeApi'
import { themes } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

function AnimeDetailPage() {
  const { animeId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.anime
  const fromSearch = location.state?.from === 'search'

  const [anime, setAnime] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [score, setScore] = useState(null)
  const [inLibrary, setInLibrary] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    getAnime(animeId)
      .then((res) => {
        setAnime(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
        enrichAnimeFromCache(animeId)
          .then((enriched) => setAnime(enriched.data))
          .catch((err) => console.error('Enrich failed:', err))
      })
      .catch(() => {
        setTimeout(() => {
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
        }, 300)
      })
  }, [animeId])

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !anime) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Anime not found.'}
        </div>
      </div>
    )
  }

  return (
    <DetailPageLayout
      activeMedia="anime"
      fromSearch={fromSearch}
      backPath={fromSearch ? '/anime/search' : '/anime/library'}
      title={anime.title}
      item={anime}
      inLibrary={inLibrary}
      score={score}
      theme={theme}
      icon="🎞️"
      refreshLabel="↻ Refresh episodes"
      metaLine={
        <div>
          <p className="text-[12px] text-[#555566] m-0 mb-1 capitalize">
            {anime.seriesStatus ?? 'Unknown status'}
            {anime.totalEpisodes != null ? ` · Ep. ${anime.totalEpisodes}` : ''}
          </p>
          {anime.studio && (
            <span>
              By{' '}
              <span
                className="cursor-pointer inline-flex items-center gap-1"
                style={{ color: theme.accent }}
                onClick={() => navigate(
                  `/anime/search?tab=creator&creatorId=${anime.studioId}&creatorName=${encodeURIComponent(anime.studio)}`
                )}
              >
                {anime.studio}
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
        </div>
      }
      progressSection={
        <SimpleProgressBar
          progress={anime.episodesWatched}
          total={anime.totalEpisodes}
          label="episode"
          theme={theme}
          onUpdate={(val) =>
            updateAnimeProgress(animeId, val).then((res) => setAnime(res.data))
          }
        />
      }
      notesProgressLabel="Ep."
      refreshing={refreshing}
      ratingsSection={
          <RatingCard
            value={anime.malRating}
            label="MAL"
            color="#2e51a2"
            theme={theme}
          />
      }
      onRefresh={() => {
        setRefreshing(true)
        refreshLatestEpisode(animeId)
          .then((res) => setAnime(res.data))
          .finally(() => setRefreshing(false))
        }
      }
      onRemove={() =>
        removeAnimeFromLibrary(animeId).then(() =>
          navigate(fromSearch ? '/anime/search' : '/anime/library')
        )
      }
      onAdd={() =>
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
          malScore: anime.malScore,
          studioId: anime.studioId ?? null,
        }).then((res) => {
          setAnime(res.data)
          setInLibrary(true)
        })
      }
      onScoreSave={(n) =>
        updateAnimeScore(animeId, n).then((res) => {
          setAnime(res.data)
          setScore(n)
        })
      }
      onStatusChange={(s) =>
        updateAnimeStatus(animeId, s).then((res) => setAnime(res.data))
      }
      onNotesSave={(notes) =>
        updateAnimeNotes(animeId, notes).then((res) => setAnime(res.data))
      }
    />

  )
}

export default AnimeDetailPage
