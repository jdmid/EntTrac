import { useState, useEffect } from 'react'

function SimpleProgressBar({
  progress,
  total,
  label,
  theme,
  onUpdate,
}) {
  const [inputValue, setInputValue] = useState(progress ?? 0)
  const [updateLabel, setUpdateLabel] = useState('Update')

  useEffect(() => {
    setInputValue(progress ?? 0)
  }, [progress])

  const pct = total > 0
    ? Math.min(100, Math.round(((progress ?? 0) / total) * 100))
    : 0

  const unread = total != null ? Math.max(0, total - (progress ?? 0)) : null
  const isOverMax = total != null && parseInt(inputValue) > total

  function handleUpdate() {
    if (isOverMax) return
    onUpdate(parseInt(inputValue) || 0)
      .then(() => {
        setUpdateLabel('✓ Updated')
        setTimeout(() => setUpdateLabel('Update'), 2000)
      })
      .catch(console.error)
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
        <span className="text-[15px] font-medium text-[#e2e2f0]">
          {progress ?? 0}
          {total != null && (
            <span className="text-[12px] text-[#555566]">
              {' '}/ {total} {label}s
            </span>
          )}
        </span>
        {unread != null && unread > 0 && (
          <span
            className="text-[10px] px-2 py-[2px] rounded-full flex-shrink-0 ml-2"
            style={{ background: `${theme.accent}33`, color: theme.accent }}
          >
            +{unread} unread
          </span>
        )}
      </div>

      {/* Progress bar */}
      {total != null && (
        <div
          className="w-full h-[3px] rounded-full mb-3"
          style={{ background: theme.cardBorder }}
        >
          <div
            className="h-full rounded-full transition-all"
            style={{ width: `${pct}%`, background: theme.accent }}
          />
        </div>
      )}

      {/* Input row */}
      <div className="flex items-center gap-0">
        <input
          type="number"
          min={0}
          max={total ?? 99999}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') handleUpdate() }}
          className="text-[12px] rounded-l-lg outline-none text-left"
          style={{
            background: theme.background,
            border: `0.5px solid ${theme.cardBorder}`,
            MozAppearance: 'textfield',
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
    </div>
  )
}

export default SimpleProgressBar