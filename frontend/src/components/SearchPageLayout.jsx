import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from './Navbar'
import { themes } from '../theme/themes'
import AttributionFooter from './AttributionFooter'

function SearchPageLayout({
  // Page
  activeMedia,
  pageTitle,
  pageSubtitle,

  // Tabs
  creatorTabLabel,
  studioTabLabel,

  // Placeholders
  titlePlaceholder,
  creatorPlaceholder,
  studioPlaceholder,

  // Title search
  results,
  loading,
  error,
  query,
  onQueryChange,
  onTitleSearch,

  // Creator
  creatorTab,
  onCreatorTabChange,
  creatorName,
  onCreatorNameChange,
  creatorResults,
  creatorLoading,
  creatorMatches,
  onCreatorSearch,
  onCreatorMatchSelect,
  disambiguationLabel,

  // Studio (optional third tab — only TV uses this today)
  studioTab,
  onStudioTabChange,
  studioName,
  onStudioNameChange,
  studioResults,
  studioLoading,
  studioMatches,
  onStudioSearch,
  onStudioMatchSelect,
  studioDisambiguationLabel,

  // Pagination
  creatorHasNextPage,
  onLoadMore,

  // Cards
  renderCard,

  // Footer
  addedCount,
  onGoToLibrary,
}) {
  const theme = themes[activeMedia]
  const hasStudioTab = Boolean(studioTabLabel)

  function selectTitleTab() {
    onCreatorTabChange(false)
    if (onStudioTabChange) onStudioTabChange(false)
  }

  function selectCreatorTab() {
    onCreatorTabChange(true)
    if (onStudioTabChange) onStudioTabChange(false)
  }

  function selectStudioTab() {
    onStudioTabChange(true)
    onCreatorTabChange(false)
  }

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia={activeMedia} />

      <div className="p-5">
        <div className="flex items-baseline gap-2.5 mb-4">
          <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0">
            {pageTitle}
          </h1>
          <p className="text-[13px] text-[#555566] m-0">
            {pageSubtitle}
          </p>
        </div>

        {/* Tab row */}
        <div className="flex gap-0 mb-4" style={{ borderBottom: `0.5px solid ${theme.cardBorder}` }}>
          <button
            onClick={selectTitleTab}
            className="px-3 py-1.5 text-[12px] border-b-2 transition-colors"
            style={{
              color: !creatorTab && !studioTab ? '#e2e2f0' : '#555566',
              borderColor: !creatorTab && !studioTab ? theme.accent : 'transparent',
              background: 'none',
              marginBottom: '-0.5px',
            }}
          >
            By title
          </button>
          <button
            onClick={selectCreatorTab}
            className="px-3 py-1.5 text-[12px] border-b-2 transition-colors"
            style={{
              color: creatorTab ? '#e2e2f0' : '#555566',
              borderColor: creatorTab ? theme.accent : 'transparent',
              background: 'none',
              marginBottom: '-0.5px',
            }}
          >
            {creatorTabLabel}
          </button>
          {hasStudioTab && (
            <button
              onClick={selectStudioTab}
              className="px-3 py-1.5 text-[12px] border-b-2 transition-colors"
              style={{
                color: studioTab ? '#e2e2f0' : '#555566',
                borderColor: studioTab ? theme.accent : 'transparent',
                background: 'none',
                marginBottom: '-0.5px',
              }}
            >
              {studioTabLabel}
            </button>
          )}
        </div>

        {/* Title search form */}
        {!creatorTab && !studioTab && (
          <form onSubmit={onTitleSearch} className="flex gap-2 mb-6">
            <input
              type="text"
              value={query}
              onChange={(e) => onQueryChange(e.target.value)}
              placeholder={titlePlaceholder}
              className="flex-1 px-3 py-2 text-[13px] text-[#e2e2f0] rounded-lg outline-none"
              style={{
                background: theme.topBar,
                border: `0.5px solid ${theme.cardBorder}`,
              }}
            />
            <button
              type="submit"
              className="px-4 py-2 text-[13px] font-medium rounded-lg transition-colors"
              style={{ background: theme.accent, color: '#ffffff' }}
            >
              Search
            </button>
          </form>
        )}

        {/* Creator search form */}
        {creatorTab && (
          <div className="mb-6">
            <form onSubmit={onCreatorSearch} className="flex gap-2">
              <input
                type="text"
                value={creatorName}
                onChange={(e) => onCreatorNameChange(e.target.value)}
                placeholder={creatorPlaceholder}
                className="flex-1 px-3 py-2 text-[13px] text-[#e2e2f0] rounded-lg outline-none"
                style={{
                  background: theme.topBar,
                  border: `0.5px solid ${theme.cardBorder}`,
                }}
              />
              <button
                type="submit"
                className="px-4 py-2 text-[13px] font-medium rounded-lg transition-colors"
                style={{ background: theme.accent, color: '#ffffff' }}
              >
                Search
              </button>
            </form>
          </div>
        )}

        {/* Studio search form */}
        {studioTab && (
          <div className="mb-6">
            <form onSubmit={onStudioSearch} className="flex gap-2">
              <input
                type="text"
                value={studioName}
                onChange={(e) => onStudioNameChange(e.target.value)}
                placeholder={studioPlaceholder}
                className="flex-1 px-3 py-2 text-[13px] text-[#e2e2f0] rounded-lg outline-none"
                style={{
                  background: theme.topBar,
                  border: `0.5px solid ${theme.cardBorder}`,
                }}
              />
              <button
                type="submit"
                className="px-4 py-2 text-[13px] font-medium rounded-lg transition-colors"
                style={{ background: theme.accent, color: '#ffffff' }}
              >
                Search
              </button>
            </form>
          </div>
        )}

        {/* Creator disambiguation list */}
        {creatorTab && creatorMatches.length > 1 && (
          <div
            className="mb-4 rounded-lg overflow-hidden"
            style={{ border: `0.5px solid ${theme.cardBorder}` }}
          >
            <p
              className="text-[11px] text-[#555566] px-3 py-2"
              style={{ background: theme.topBar, borderBottom: `0.5px solid ${theme.cardBorder}` }}
            >
              {disambiguationLabel ?? 'Multiple results found — select one'}
            </p>
            {creatorMatches.map((match) => (
              <button
                key={match.id}
                onClick={() => onCreatorMatchSelect(match)}
                className="w-full text-left px-3 py-2 text-[13px] transition-colors"
                style={{
                  color: '#e2e2f0',
                  background: 'transparent',
                  borderBottom: `0.5px solid ${theme.cardBorder}`,
                  display: 'block',
                }}
              >
                {match.name}
              </button>
            ))}
          </div>
        )}

        {/* Studio disambiguation list */}
        {studioTab && studioMatches.length > 1 && (
          <div
            className="mb-4 rounded-lg overflow-hidden"
            style={{ border: `0.5px solid ${theme.cardBorder}` }}
          >
            <p
              className="text-[11px] text-[#555566] px-3 py-2"
              style={{ background: theme.topBar, borderBottom: `0.5px solid ${theme.cardBorder}` }}
            >
              {studioDisambiguationLabel ?? 'Multiple results found — select one'}
            </p>
            {studioMatches.map((match) => (
              <button
                key={match.id}
                onClick={() => onStudioMatchSelect(match)}
                className="w-full text-left px-3 py-2 text-[13px] transition-colors"
                style={{
                  color: '#e2e2f0',
                  background: 'transparent',
                  borderBottom: `0.5px solid ${theme.cardBorder}`,
                  display: 'block',
                }}
              >
                {match.name}
              </button>
            ))}
          </div>
        )}

        {/* Title tab states */}
        {!creatorTab && !studioTab && loading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Searching…
          </div>
        )}

        {!creatorTab && !studioTab && !loading && error && (
          <div className="text-[13px] text-[#f87171] text-center py-12">
            {error}
          </div>
        )}

        {!creatorTab && !studioTab && !loading && !error && results.length === 0 && query && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            No results found for "{query}"
          </div>
        )}

        {!creatorTab && !studioTab && !loading && results.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 items-start">
            {results.map((item) => renderCard(item))}
          </div>
        )}

        {/* Creator tab states */}
        {creatorTab && creatorLoading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Searching…
          </div>
        )}

        {creatorTab && !creatorLoading && creatorResults.length === 0 && creatorName && creatorMatches.length === 0 && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            No works found for "{creatorName}"
          </div>
        )}

        {creatorTab && !creatorLoading && creatorResults.length > 0 && (
          <>
            <p className="text-[13px] text-[#555566] mb-3">
              {creatorResults.length} work{creatorResults.length !== 1 ? 's' : ''} by {creatorName}
            </p>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 items-start">
              {creatorResults.map((item) => renderCard(item))}
            </div>
            {creatorHasNextPage && onLoadMore && (
              <button
                onClick={onLoadMore}
                className="w-full mt-4 py-2 text-[12px] rounded-lg transition-colors"
                style={{
                  background: theme.accentBg,
                  border: `0.5px solid ${theme.accentBorder}`,
                  color: theme.accent,
                }}
              >
                Load more
              </button>
            )}
          </>
        )}

        {/* Studio tab states */}
        {studioTab && studioLoading && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            Searching…
          </div>
        )}

        {studioTab && !studioLoading && studioResults.length === 0 && studioName && studioMatches.length === 0 && (
          <div className="text-[13px] text-[#555566] text-center py-12">
            No works found for "{studioName}"
          </div>
        )}

        {studioTab && !studioLoading && studioResults.length > 0 && (
          <>
            <p className="text-[13px] text-[#555566] mb-3">
              {studioResults.length} work{studioResults.length !== 1 ? 's' : ''} by {studioName}
            </p>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 items-start">
              {studioResults.map((item) => renderCard(item))}
            </div>
          </>
        )}

        {/* Added indicator */}
        {addedCount > 0 && (
          <p className="text-[11px] text-[#555566] text-center mt-4">
            {addedCount} title{addedCount !== 1 ? 's' : ''} added —{' '}
            <span
              className="cursor-pointer"
              style={{ color: theme.accent }}
              onClick={onGoToLibrary}
            >
              go to library
            </span>
          </p>
        )}

        <AttributionFooter />
      </div>
    </div>
  )
}

export default SearchPageLayout