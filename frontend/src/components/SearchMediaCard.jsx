import { statusStyles } from '../theme/themes'

function SearchMediaCard({
  title,
  creator,
  seriesStatus,
  coverUrl,
  theme,
  icon,
  isAdded,
  onAdd,
  onClick,
}) {
  return (
    <div
      onClick={onClick}
      className="rounded-lg overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
      style={{ border: '0.5px solid rgba(255,255,255,0.09)' }}
    >
      {/* Cover */}
      <div
        className="w-full aspect-[2/3] flex items-center justify-center"
        style={{ background: theme.cardCover }}
      >
        {coverUrl ? (
          <img src={coverUrl} alt={title} className="w-full h-full object-cover" />
        ) : (
          <span style={{ fontSize: 28, color: theme.cardIcon }}>{icon}</span>
        )}
      </div>

      {/* Body */}
      <div
        className="px-2.5 pt-2 pb-2.5 flex flex-col"
        style={{
            height: '110px',
            background: theme.cardBody,
            borderTop: '0.5px solid rgba(255,255,255,0.06)',
        }}
        >
        <p className="text-[12px] font-medium text-[#d0d0e0] m-0 mb-[3px] truncate">
            {title}
        </p>

        {creator && creator.trim() !== '' && (
            <p className="text-[10px] text-[#555566] m-0 mb-[3px] truncate">
            {creator}
            </p>
        )}

        {seriesStatus && seriesStatus.trim() !== '' && (
            <span
            className="inline-block text-[10px] px-[7px] py-[2px] rounded-full mb-[6px] self-start"
            style={{
                background: `${theme.accent}22`,
                border: `0.5px solid ${theme.accent}44`,
                color: theme.accent,
            }}
            >
            {seriesStatus}
            </span>
        )}

        {/* Button pinned to bottom */}
        <div className="mt-auto">
            {isAdded ? (
            <div
                className="w-full py-1.5 text-[11px] rounded text-center"
                style={{
                background: '#1f4a32',
                color: '#4ade80',
                border: '0.5px solid #2a5a3a',
                }}
            >
                ✓ In library
            </div>
            ) : (
            <button
                onClick={(e) => { e.stopPropagation(); onAdd?.() }}
                className="w-full py-1.5 text-[11px] rounded transition-colors"
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
    </div>
  )
}

export default SearchMediaCard