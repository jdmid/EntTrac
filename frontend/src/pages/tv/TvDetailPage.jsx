import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import TvProgressBar from '../../components/TvProgressBar'
import RatingCard from '../../components/RatingCard'
import {
  getTvShow, getTvDetails, updateTvProgress, updateTvScore,
  updateTvStatus, refreshTvEpisodes, removeFromTvLibrary,
  addTvToLibrary, enrichTvFromCache, updateTvNotes,
} from '../../api/tvApi'
import { themes } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

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
  const [inLibrary, setInLibrary] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    getTvShow(tvId)
      .then((res) => {
        setShow(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
        enrichTvFromCache(tvId)
          .then((enriched) => setShow(enriched.data))
          .catch((err) => console.error('Enrich failed:', err))
      })
      .catch(() => {
        getTvDetails(tvId)
            .then((res) => {
                const data = res.data
                data.seriesStatus = normalizeSeriesStatus(data.status, 'tv')
                setShow(data)
                setInLibrary(false)
                setLoading(false)
            })
          .catch(() => {
            setError('Failed to load TV show.')
            setLoading(false)
          })
      })
  }, [tvId])

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !show) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'TV show not found.'}
        </div>
      </div>
    )
  }

  const badgeParts = []
  if (show.seriesType) badgeParts.push(show.seriesType)
  if (show.seriesStatus) badgeParts.push(
    show.seriesStatus.charAt(0).toUpperCase() + show.seriesStatus.slice(1)
  )
  const badgeText = badgeParts.join(' · ')

  return (
    <DetailPageLayout
      activeMedia="tv"
      fromSearch={fromSearch}
      backPath={fromSearch ? '/tv/search' : '/tv/library'}
      title={show.title}
      item={show}
      inLibrary={inLibrary}
      score={score}
      theme={theme}
      icon="📺"
      refreshLabel="↻ Refresh episodes"
      metaLine={
        <div>
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
          {show.creatorName && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              Created by{' '}
              <span
                className="cursor-pointer inline-flex items-center gap-1"
                style={{ color: theme.accent }}
                onClick={() => navigate(
                  `/tv/search?tab=creator&creatorId=${show.creatorId}&creatorName=${encodeURIComponent(show.creatorName)}`
                )}
              >
                {show.creatorName}
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                  strokeLinejoin="round" aria-hidden="true">
                  <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
                  <polyline points="15 3 21 3 21 9"/>
                  <line x1="10" y1="14" x2="21" y2="3"/>
                </svg>
              </span>
            </p>
          )}
        </div>
      }
      progressSection={
        <TvProgressBar
          item={show}
          theme={theme}
          onUpdate={(episodesWatched, currentSeason) =>
            updateTvProgress(tvId, episodesWatched, currentSeason)
              .then((res) => setShow(res.data))
          }
        />
      }
      notesProgressLabel="Ep."
      refreshing={refreshing}
      ratingsSection={
        <div className="flex gap-2 flex-wrap">
          <RatingCard
            value={show.tmdbRating}
            label="TMDB"
            color="#01b4e4"
            theme={theme}
          />
        </div>
      }
      onRefresh={() => {
        setRefreshing(true)
        refreshTvEpisodes(tvId)
            .then((res) => setShow(res.data))
            .finally(() => setRefreshing(false))
        }
      }
      onRemove={() =>
        removeFromTvLibrary(tvId).then(() =>
          navigate(fromSearch ? '/tv/search' : '/tv/library')
        )
      }
      onAdd={() =>
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
          tmdbScore: show.tmdbScore,
          nextEpisodeDate: show.nextEpisodeDate,
          creatorName: show.creatorName ?? null,
          creatorId: show.creatorId ?? null,
        }).then(() => setInLibrary(true))
      }
      onScoreSave={(n) =>
        updateTvScore(tvId, n).then((res) => {
          setShow(res.data)
          setScore(n)
        })
      }
      onStatusChange={(s) =>
        updateTvStatus(tvId, s).then((res) => setShow(res.data))
      }
      onNotesSave={(notes) =>
        updateTvNotes(tvId, notes).then((res) => setShow(res.data))
      }
    />
  )
}

export default TvDetailPage