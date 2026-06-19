import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import DetailPageLayout from '../../components/DetailPageLayout'
import SimpleProgressBar from '../../components/SimpleProgressBar'
import RatingCard from '../../components/RatingCard'
import {
  getGame, getGameDetails, updateGameProgress, updateGameScore,
  updateGameStatus, refreshGameRatings, removeGameFromLibrary,
  addGameToLibrary, enrichGameFromCache, updateGameNotes,
  updateUserPlatform, updateOwnedDlc,
} from '../../api/gameApi'
import { themes } from '../../theme/themes'
import { normalizeSeriesStatus } from '../../utils/statusMapping'

function mergeGameUpdate(prev, updated) {
    return {
        ...updated,
        dlc: prev?.dlc ?? null,
        platforms: prev?.platforms ?? updated.platforms,
    }
  }
  
function DlcChecklist({ dlc, ownedDlcIds, theme, gameId, onUpdate, navigate }) {
  if (!dlc || dlc.length === 0) return null

  function handleToggle(dlcId) {
    const current = ownedDlcIds ?? []
    const updated = current.includes(dlcId)
      ? current.filter((id) => id !== dlcId)
      : [...current, dlcId]
    onUpdate(updated)
  }

  return (
    <div
      className="rounded-lg overflow-hidden"
      style={{
        background: theme.topBar,
        border: `0.5px solid ${theme.cardBorder}`,
      }}
    >
      <div
        className="flex items-center justify-between px-3 py-1.5"
        style={{ borderBottom: `0.5px solid ${theme.cardBorder}` }}
      >
        <span className="text-[10px] text-[#555566] uppercase tracking-[0.05em]">
          Title
        </span>
        <span className="text-[10px] text-[#555566] uppercase tracking-[0.05em]">
          Owned
        </span>
      </div>
      {dlc.map((item) => {
        const owned = (ownedDlcIds ?? []).includes(item.id)
        return (
          <div
            key={item.id}
            className="flex items-center justify-between px-3 py-2"
            style={{ borderBottom: `0.5px solid ${theme.cardBorder}33` }}
          >
            <span
              className="text-[11px] cursor-pointer"
              style={{ color: theme.accent }}
              onClick={() => 
                navigate(`/game/dlc/${item.id}`, 
                    { state: { from: 'game' } }
                )
              }
            >
              {item.title} ↗
            </span>
            <div
              onClick={() => handleToggle(item.id)}
              className="cursor-pointer flex items-center justify-center rounded"
              style={{
                width: '16px',
                height: '16px',
                background: owned ? theme.accent : 'transparent',
                border: `0.5px solid ${owned ? theme.accent : theme.accentBorder}`,
                flexShrink: 0,
              }}
            >
              {owned && (
                <span className="text-[10px] font-medium text-black">✓</span>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}

function GameDetailPage() {
  const params = useParams()
  const gameId = params.gameId ?? params.dlcId
  const navigate = useNavigate()
  const location = useLocation()
  const theme = themes.game
  const fromSearch = location.state?.from === 'search'
  const isDlcRoute = location.pathname.startsWith('/game/dlc/')

  const [game, setGame] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [score, setScore] = useState(null)
  const [inLibrary, setInLibrary] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    if (isDlcRoute) {
      getGameDetails(gameId)
        .then((res) => {
          setGame(res.data)
          setInLibrary(false)
          setLoading(false)
        })
        .catch(() => {
          setError('Failed to load DLC.')
          setLoading(false)
        })
      return
    }

    getGame(gameId)
    .then((res) => {
        const libraryItem = res.data
        setScore(libraryItem.score ?? null)
        // Fetch live details to get DLC and platform list
        getGameDetails(gameId)
        .then((details) => {
            console.log('=== GAME DETAILS ===', details.data)
            console.log('=== DLC ===', details.data.dlc)
            // Merge library item with live details
            // Library item has progress, score, status, ownedDlcIds, userPlatform
            // Details has dlc, platforms (live from IGDB)
            setGame({
            ...libraryItem,
            dlc: details.data.dlc ?? null,
            platforms: details.data.platforms ?? libraryItem.platforms,
            })
            setLoading(false)
            enrichGameFromCache(gameId)
            .then((enriched) => setGame((prev) => ({
                ...mergeGameUpdate(prev, enriched.data),
            })))
            .catch((err) => console.error('Enrich failed:', err))
        })
        .catch(() => {
            // Fall back to just the library item if details fail
            setGame(libraryItem)
            setLoading(false)
        })
    })
    .catch(() => {
        getGameDetails(gameId)
        .then((res) => {
            setGame(res.data)
            setInLibrary(false)
            setLoading(false)
        })
        .catch(() => {
            setError('Failed to load game.')
            setLoading(false)
        })
    })
  }, [gameId])

  if (loading) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#555566] text-center py-12">
          Loading…
        </div>
      </div>
    )
  }

  if (error || !game) {
    return (
      <div className="min-h-screen" style={{ background: theme.background }}>
        <div className="text-[13px] text-[#f87171] text-center py-12">
          {error ?? 'Game not found.'}
        </div>
      </div>
    )
  }

  // Platform dropdown — only shown when in library and platforms available
  const platformSection = inLibrary && game.platforms?.length > 0 ? (
    <div
      className="rounded-lg p-3"
      style={{
        background: theme.topBar,
        border: `0.5px solid ${theme.cardBorder}`,
      }}
    >
      <select
        value={game.userPlatform ?? ''}
        onChange={(e) =>
          updateUserPlatform(gameId, e.target.value).then((res) =>
            setGame((prev) => mergeGameUpdate(prev, res.data))
          )
        }
        className="w-full text-[12px] rounded-lg outline-none"
        style={{
          background: theme.background,
          border: `0.5px solid ${theme.cardBorder}`,
          color: game.userPlatform ? '#e2e2f0' : '#555566',
          padding: '7px 12px',
        }}
      >
        <option value="" disabled>Select your platform…</option>
        {game.platforms.map((p) => (
          <option key={p} value={p}>{p}</option>
        ))}
      </select>
    </div>
  ) : null

  // DLC section — only shown when in library and DLC exists
  const dlcSection = inLibrary && game.dlc?.length > 0 ? (
    <DlcChecklist
      dlc={game.dlc}
      ownedDlcIds={game.ownedDlcIds}
      theme={theme}
      gameId={gameId}
      onUpdate={(updated) =>
        updateOwnedDlc(gameId, updated).then((res) => 
            setGame((prev) => mergeGameUpdate(prev, res.data)))
      }
      navigate={navigate}
    />
  ) : null

  return (
    <DetailPageLayout
      activeMedia="game"
      fromSearch={fromSearch}
      backPath={isDlcRoute
        ? `/game/library/${gameId}`
        : fromSearch ? '/game/search' : '/game/library'
      }
      title={game.title}
      item={game}
      inLibrary={inLibrary}
      score={score}
      theme={theme}
      icon="🎮"
      refreshLabel="Refresh ratings"
      refreshing={refreshing}
      metaLine={
        <div>
          <p className="text-[12px] text-[#555566] m-0 mb-1 capitalize">
            {[
              game.seriesStatus,
              game.releaseYear,
              game.genres,
            ]
              .filter(Boolean)
              .join(' · ')}
          </p>
          {game.developer && (
            <p className="text-[12px] text-[#555566] m-0 mb-2">
              By{' '}
              <span
                className="cursor-pointer inline-flex items-center gap-1"
                style={{ color: theme.accent }}
                onClick={() =>
                  navigate(
                    `/game/search?tab=creator&creatorId=${game.developerId}&creatorName=${encodeURIComponent(game.developer)}`
                  )
                }
              >
                {game.developer}
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
          {game.platforms?.length > 0 && (
            <div className="flex gap-1.5 flex-wrap mb-3">
              {game.platforms.map((p) => (
                <span
                  key={p}
                  className="text-[10px] px-2 py-[2px] rounded"
                  style={{
                    background: theme.accentBg,
                    border: `0.5px solid ${theme.accentBorder}`,
                    color: theme.accent,
                  }}
                >
                  {p}
                </span>
              ))}
            </div>
          )}
        </div>
      }
      progressSection={
        inLibrary ? (
          <SimpleProgressBar
            progress={game.hoursPlayed}
            total={null}
            label="hour"
            theme={theme}
            onUpdate={(val) =>
              updateGameProgress(gameId, val).then((res) => 
                setGame((prev) => mergeGameUpdate(prev, res.data))
              )
            }
          />
        ) : null
      }
      ratingsSection={
        <>
          <RatingCard
            value={game.igdbRating}
            label="IGDB"
            color="#9147fe"
            theme={theme}
          />
          <RatingCard
            value={game.igdbCriticRating}
            label="Critic score"
            color="#66cc33"
            theme={theme}
          />
        </>
      }
      platformSection={platformSection}
      dlcSection={dlcSection}
      notesProgressLabel="Hrs."
      onRefresh={inLibrary ? () => {
        setRefreshing(true)
        refreshGameRatings(gameId)
        .then((res) => setGame((prev) => mergeGameUpdate(prev, res.data)))
        .finally(() => setRefreshing(false))
      } : null}
      onRemove={inLibrary ? () =>
      removeGameFromLibrary(gameId).then(() =>
        navigate(fromSearch ? '/game/search' : '/game/library')
      )
      : null}
      onAdd={!inLibrary && !isDlcRoute ? () =>
      addGameToLibrary({
        gameId: game.id,
        title: game.title,
        status: 'PLANNED',
        hoursPlayed: 0,
        coverUrl: game.coverUrl,
        description: game.description,
        seriesStatus: normalizeSeriesStatus(game.status, 'games'),
        releaseYear: game.releaseYear,
        genres: game.genres,
        developer: game.developer,
        developerId: game.developerId ?? null,
        platforms: game.platforms ?? [],
        igdbRating: game.igdbRating ?? null,
        igdbCriticRating: game.igdbCriticRating ?? null,
      }).then((res) => {
          setGame(res.data)
          setInLibrary(true)
        })
      : null}
      onScoreSave={inLibrary ? (n) =>
      updateGameScore(gameId, n).then((res) => {
        setGame((prev) => mergeGameUpdate(prev, res.data))
        setScore(n)
      })
      : null}
      onStatusChange={inLibrary ? (s) =>
      updateGameStatus(gameId, s).then((res) =>
        setGame((prev) => mergeGameUpdate(prev, res.data))
      )
      : null}
      onNotesSave={inLibrary ? (notes) =>
      updateGameNotes(gameId, notes).then((res) =>
        setGame((prev) => mergeGameUpdate(prev, res.data))
      )
      : null}
    />
  )
}

export default GameDetailPage