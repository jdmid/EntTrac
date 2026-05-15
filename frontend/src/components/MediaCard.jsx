import { statusStyles } from '../theme/themes'

function MediaCard({ 
  title,
  author,
  score,
  status,
  progress,
  total,
  seriesStatus,
  coverUrl,
  theme,
  icon,
  progressLabel,
  onAdd,
  onClick,
}) {
  const style = status ? (statusStyles[status] ?? statusStyles.FINISHED) : null
  const unread = total != null ? Math.max(0, total - (progress ?? 0)) : null

  return (
    <div
      onClick={onAdd ? undefined : onClick}
      className="rounded-lg overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
      style={{ border: '0.5px solid rgba(255,255,255,0.09)' }}
    >
      {/* Cover */}
      <div
        className="w-full aspect-[2/3] flex items-center justify-center relative"
        style={{ background: theme.cardCover }}
      >
        {coverUrl ? (
          <img
            src={coverUrl}
            alt={title}
            className="w-full h-full object-cover"
          />
        ) : (
          <span style={{ fontSize: 28, color: theme.cardIcon }}>{icon}</span>
        )}

        {/* Score pill — only shown if score prop is passed */}
        {score !== undefined && (
          <div
            className="absolute top-1.5 right-1.5 rounded-full px-[7px] py-[2px] text-[11px] font-semibold"
            style={{
              background: 'rgba(0,0,0,0.75)',
              border: '0.5px solid rgba(255,255,255,0.15)',
              color: score != null ? '#fbbf24' : '#555566',
            }}
          >
            ★ {score != null ? score : '—'}
          </div>
        )}
      </div>

      {/* Card body */}
      <div
        className="px-2.5 pt-2 pb-2.5"
        style={{
          background: style ? style.bg : theme.cardBody,
          borderTop: style
            ? `0.5px solid ${style.border}`
            : '0.5px solid rgba(255,255,255,0.06)',
        }}
      >
        {/* Title */}
        <p className="text-[12px] font-medium text-[#d0d0e0] m-0 mb-[3px] truncate">
          {title}
        </p>

        {/* Author — only shown if passed */}
        {author && (
          <p className="text-[10px] text-[#555566] m-0 mb-[3px] truncate">
            {author}
          </p>
        )}

        {/* Series status — only shown if passed */}
        {seriesStatus && (
          <p className="text-[10px] text-[#555566] m-0 mb-[5px] capitalize">
            {seriesStatus}
          </p>
        )}

        {/* Status badge — only shown if status passed */}
        {style && (
          <span
            className="inline-block text-[10px] font-medium px-[7px] py-[2px] rounded-full mb-1.5"
            style={style.badge}
          >
            {style.label}
          </span>
        )}

        {/* Progress row — only shown if progressLabel passed */}
        {progressLabel && (
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-[#8a8a9a]">
              {progressLabel} {progress ?? 0}{total != null ? ` / ${total}` : ''}
            </span>

            {unread != null && unread > 0 && (
              <span
                className="text-[10px] font-medium px-1.5 py-[1px] rounded-full"
                style={{ background: theme.unreadBadge, color: theme.accent }}
              >
                +{unread}
              </span>
            )}
          </div>
        )}

        {/* Add to library button — only shown if onAdd passed */}
        {onAdd && (
          <button
            onClick={(e) => {
              e.stopPropagation()
              onAdd()
            }}
            className="w-full mt-2 py-1.5 text-[11px] rounded transition-colors"
            style={{
              background: theme.accentBg,
              border: `0.5px solid ${theme.accentBorder}`,
              color: theme.accent,
            }}
          >
            + Add to library
          </button>
        )}
      </div>
    </div>
  )
}

export default MediaCard