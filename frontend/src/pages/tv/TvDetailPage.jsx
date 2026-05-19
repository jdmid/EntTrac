import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import TvProgressBar from '../../components/TvProgressBar'
import {
  getTvShow, getTvDetails, updateTvProgress, updateTvScore,
  updateTvStatus, refreshTvEpisodes, removeFromTvLibrary,
  addTvToLibrary, getTvCommunityRating, updateTvNotes,
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
  const [communityRating, setCommunityRating] = useState(null)

  useEffect(() => {
    getTvShow(tvId)
      .then((res) => {
        setShow(res.data)
        setScore(res.data.score ?? null)
        setLoading(false)
        getTvCommunityRating(tvId)
          .then((res) => setCommunityRating(res.data))
          .catch(() => setCommunityRating(null))
      })
      .catch(() => {
        getTvDetails(tvId)
            .then((res) => {
                const data = res.data
                data.seriesStatus = normalizeSeriesStatus(data.status, 'tv')
                setShow(data)
                setInLibrary(false)
                setCommunityRating(data.communityRating ?? null)
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
      communityRating={communityRating}
      communityRatingLabel="TMDB score"
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
          {show.network && (
            <p className="text-[12px] text-[#555566] m-0 mb-3">
              <span style={{ color: theme.accent }}>{show.network}</span>
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
      onRefresh={() =>
        refreshTvEpisodes(tvId).then((res) => setShow(res.data))
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
          communityRating: show.communityRating,
          nextEpisodeDate: show.nextEpisodeDate,
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