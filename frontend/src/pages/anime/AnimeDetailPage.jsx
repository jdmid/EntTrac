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
        }, 1000)
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
        <div className="flex gap-2 flex-wrap">
          <RatingCard
            value={anime.malRating}
            label="MAL"
            color="#2e51a2"
            theme={theme}
          />
        </div>
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
        }).then(() => setInLibrary(true))
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
