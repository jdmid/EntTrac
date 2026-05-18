import { useState, useEffect } from 'react'

function TvProgressBar({ item, theme, onUpdate }) {
  const [selectedSeasonIndex, setSelectedSeasonIndex] = useState(0)
  const [episodeInput, setEpisodeInput] = useState(0)
  const [updateLabel, setUpdateLabel] = useState('Update')

  const seasonEpisodes = item.seasonEpisodes ?? []

  // Derive the current season index from stored currentSeason
  useEffect(() => {
    if (item.currentSeason && item.currentSeason > 0) {
      setSelectedSeasonIndex(item.currentSeason - 1)
    }
  }, [item.currentSeason])

  // Derive episode within season from cumulative episodesWatched
  useEffect(() => {
    if (seasonEpisodes.length === 0) return
    const idx = item.currentSeason ? item.currentSeason - 1 : 0
    const prevEps = seasonEpisodes
      .slice(0, idx)
      .reduce((sum, n) => sum + n, 0)
    const epInSeason = (item.episodesWatched ?? 0) - prevEps
    setEpisodeInput(Math.max(0, epInSeason))
  }, [item.episodesWatched, item.currentSeason, seasonEpisodes])

  const currentSeasonEps = seasonEpisodes[selectedSeasonIndex] ?? 0

  const prevEps = seasonEpisodes
    .slice(0, selectedSeasonIndex)
    .reduce((sum, n) => sum + n, 0)

  const cumulativeWatched = prevEps + (parseInt(episodeInput) || 0)

  const progressPct = currentSeasonEps > 0
    ? Math.min(100, Math.round(((parseInt(episodeInput) || 0) / currentSeasonEps) * 100))
    : 0

  const totalEpisodes = item.totalEpisodes ?? 0
  const totalUnwatched = Math.max(0, totalEpisodes - (item.episodesWatched ?? 0))
  const isOverMax = (parseInt(episodeInput) || 0) > currentSeasonEps

  function handleSeasonChange(e) {
    const idx = parseInt(e.target.value)
    setSelectedSeasonIndex(idx)
    setEpisodeInput(0)
  }

  function handleUpdate() {
    if (isOverMax) return
    const newCumulative = prevEps + (parseInt(episodeInput) || 0)
    const newSeason = selectedSeasonIndex + 1
    onUpdate(newCumulative, newSeason)
      .then(() => {
        setUpdateLabel('✓ Updated')
        setTimeout(() => setUpdateLabel('Update'), 2000)
      })
      .catch(console.error)
  }

  if (seasonEpisodes.length === 0) {
    return (
      <div
        className="rounded-lg p-3"
        style={{
          background: theme.topBar,
          border: `0.5px solid ${theme.cardBorder}`,
        }}
      >
        <p className="text-[12px] text-[#555566] m-0">
          No episode data available. Try refreshing.
        </p>
      </div>
    )
  }

  return (
    <div
      className="rounded-lg p-3"
      style={{
        background: theme.topBar,
        border: `0.5px solid ${theme.cardBorder}`,
      }}
    >
      {/* Header */}
      <div className="flex items-baseline justify-between mb-2">
        <div className="flex items-baseline gap-1 flex-wrap">
          <span className="text-[15px] font-medium text-[#e2e2f0]">
            Season {selectedSeasonIndex + 1} · Ep {parseInt(episodeInput) || 0} out of {currentSeasonEps}
          </span>
          <span className="text-[11px] text-[#555566]">
            · Ep {cumulativeWatched} overall
          </span>
        </div>
        {totalUnwatched > 0 && (
          <span
            className="text-[10px] px-2 py-[2px] rounded-full flex-shrink-0 ml-2"
            style={{ background: `${theme.accent}33`, color: theme.accent }}
          >
            +{totalUnwatched} unwatched
          </span>
        )}
      </div>

      {/* Progress bar */}
      <div
        className="w-full h-[3px] rounded-full mb-3"
        style={{ background: theme.cardBorder }}
      >
        <div
          className="h-full rounded-full transition-all"
          style={{ width: `${progressPct}%`, background: theme.accent }}
        />
      </div>

      {/* Season selector */}
      <div className="flex items-center gap-2 mb-2">
        <span className="text-[11px] text-[#777788]">Season</span>
        <select
          value={selectedSeasonIndex}
          onChange={handleSeasonChange}
          className="text-[11px] rounded-md outline-none"
          style={{
            background: theme.background,
            border: `0.5px solid ${theme.cardBorder}`,
            color: '#9a9aaa',
            padding: '3px 30px 3px 6px',
            width: 'auto',
          }}
        >
          {seasonEpisodes.map((eps, idx) => (
            <option key={idx} value={idx}>
              {idx + 1} — {eps} eps
            </option>
          ))}
        </select>
      </div>

      {/* Episode input */}
      <div className="flex items-center gap-0 mt-2">
        <input
            type="number"
            min={0}
            max={currentSeasonEps}
            value={episodeInput}
            onChange={(e) => setEpisodeInput(e.target.value)}
            className="text-[12px] rounded-l-lg outline-none text-left"
            style={{
            background: theme.topBar,
            border: `0.5px solid ${theme.cardBorder}`,
            borderRight: 'none',
            color: '#e2e2f0',
            padding: '7px 12px',
            flex: 1,
            }}
        />
        <button
            onClick={handleUpdate}
            disabled={isOverMax}
            className="text-[12px] font-medium px-4 py-[7px] rounded-r-lg transition-colors whitespace-nowrap"
            style={{
            background: isOverMax
                ? theme.topBar
                : updateLabel === '✓ Updated'
                ? '#1f4a32'
                : theme.accent,
            border: `0.5px solid ${isOverMax
                ? theme.cardBorder
                : updateLabel === '✓ Updated'
                ? '#2a5a3a'
                : theme.accent}`,
            color: isOverMax
                ? '#3d3d4a'
                : updateLabel === '✓ Updated'
                ? '#4ade80'
                : '#000000',
            cursor: isOverMax ? 'not-allowed' : 'pointer',
            }}
        >
            {updateLabel}
        </button>
        </div>

      {/* Next episode */}
      {item.nextEpisodeDate && (
        <div
          className="mt-3 rounded-md px-3 py-2"
          style={{
            background: `${theme.accent}11`,
            border: `0.5px solid ${theme.accent}33`,
          }}
        >
          <p className="text-[10px] m-0 mb-1" style={{ color: theme.accent }}>
            Next episode
          </p>
          <p className="text-[11px] text-[#e2e2f0] m-0">
            {item.nextEpisodeDate}
          </p>
        </div>
      )}
    </div>
  )
}

export default TvProgressBar