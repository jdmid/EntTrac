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
import { normalizeSeriesStatus, DETAIL_STATUS_OPTIONS } from '../../utils/statusMapping'

function NextEpisodeBanner({ episode, airingAt, theme }) {
    const now = Math.floor(Date.now() / 1000)
    const secondsUntil = airingAt - now

    let label
    if (secondsUntil <= 0) {
        label = `Ep ${episode} has aired`
    } else if (secondsUntil < 3600) {
        const mins = Math.floor(secondsUntil / 60)
        label = `Ep ${episode} · in ${mins} minute${mins !== 1 ? 's' : ''}`
    } else if (secondsUntil < 86400) {
        const hours = Math.floor(secondsUntil / 3600)
        label = `Ep ${episode} · in ${hours} hour${hours !== 1 ? 's' : ''}`
    } else if (secondsUntil < 172800) {
        label = `Ep ${episode} · tomorrow`
    } else {
        const days = Math.floor(secondsUntil / 86400)
        const date = new Date(airingAt * 1000).toLocaleDateString('en-US', {
            month: 'short', day: 'numeric'
        })
        label = `Ep ${episode} · ${date} (in ${days} days)`
    }

    return (
        <div
            className="mt-2 rounded-md px-3 py-2"
            style={{
                background: `${theme.accent}11`,
                border: `0.5px solid ${theme.accent}33`,
                width: 'fit-content',
            }}
        >
            <p className="text-[10px] m-0 mb-1" style={{ color: theme.accent }}>
                Next episode
            </p>
            <p className="text-[11px] text-[#e2e2f0] m-0">
                {label}
            </p>
        </div>
    )
}

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
          .catch((err) => {
            console.error('Failed to load anime details:', err)
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
      statusOptions={DETAIL_STATUS_OPTIONS.anime}
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
              nextEpisodeContent={
                  anime.nextAiringAt ? (
                      <NextEpisodeBanner
                          episode={anime.nextAiringEpisode}
                          airingAt={anime.nextAiringAt}
                          theme={theme}
                      />
                  ) : null
              }
          />
      }
      notesProgressLabel="Ep."
      refreshing={refreshing}
      ratingsSection={
        <>
          <RatingCard
            value={anime.malRating}
            label="MAL"
            color="#2e51a2"
            theme={theme}
          />
          <RatingCard
            value={anime.anilistRating}
            label="AniList"
            color="#02a9ff"
            theme={theme}
          />
        </>
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
          nextAiringEpisode: anime.nextAiringEpisode ?? null,
          nextAiringAt: anime.nextAiringAt ?? null,
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
